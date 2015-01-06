package anh.trinh.ble_demo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import anh.trinh.ble_demo.data.BTMessageType;
import anh.trinh.ble_demo.data.BluetoothMessage;
import anh.trinh.ble_demo.data.CommandID;
import anh.trinh.ble_demo.data.DataConversion;
import anh.trinh.ble_demo.data.DeviceInfo;
import anh.trinh.ble_demo.data.ProcessBTMsg;
import anh.trinh.ble_demo.list_view.Device_c;
import anh.trinh.ble_demo.list_view.Scene_c;
import anh.trinh.ble_demo.list_view.Zone_c;
import anh.trinh.ble_demo.thread_sync.ThreadSignal;

public class HomeActivity extends FragmentActivity implements TabListener {

	private final static String TAG = HomeActivity.class.getSimpleName();

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	public static final int TIMEOUT_REC_BLE_MSG = 2000;
	private String mDeviceName;
	private String mDeviceAddress;
	private ViewPager viewPager;
	private TabsPagerAdapter mPagerAdapter;
	private ActionBar actionBar;
	// Tab titles
	private String[] actionTabs = { "Device Control", "Scenes" };

	public ProgressDialog mProgDialog;
	public BluetoothLeService mBluetoothLeService;

	private boolean mConnected = false;
	public volatile boolean mWriteBLESuccess = false;
	public CountDownTimer mCountDown;
	public ThreadSignal mBLEThreadSignal = new ThreadSignal();
	public ArrayList<BluetoothGattCharacteristic> mWriteCharacteristic,
			mNotifyCharateristic;
	public short mBTMsgIndex = 0;
	public int mNumOfDev;
	public short mNumOfRule;
	public ArrayList<DeviceInfo> mDevInfoList = new ArrayList<DeviceInfo>();
	public ArrayList<Zone_c> mZoneList = new ArrayList<Zone_c>();
	public int mNumOfActScene;
	public int mNumOfInactScene;
	public ArrayList<Scene_c> mSceneList = new ArrayList<Scene_c>();
	public ArrayList<Scene_c> mSceneListUpdate = new ArrayList<Scene_c>();

	public boolean[] mDeviceIndexMng;
	public boolean[] mRuleIndexMng;
	public boolean[] mZoneIndexMng;
	public boolean[] mActSceneIndexMng;
	public boolean[] mInactSceneIndexMng;
	public ProcessBTMsg mProcessMsg = new ProcessBTMsg(HomeActivity.this);
	public BLESendThread mBLEThreadSend;
	private BLERecThread mBLEThreadRec;
	public Object mMonitorObj = new Object();
	// private BlockingQueue<Message> mMsgQueue = new
	// LinkedBlockingQueue<Message>();

	private Handler mHandler = new Handler(Looper.getMainLooper());
	public boolean requestServerBusy = true;
	// Code to manage Service life cycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
	// result of read
	// or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				invalidateOptionsMenu();
				Log.i(TAG, "BLE Connected");
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				mConnected = false;
				invalidateOptionsMenu();
				Log.i(TAG, "BLE Disconnected");
				showDialog("Server Device Disconnected");
				finish();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				// get Server's data
				getServerDeviceData();
			} else if (BluetoothLeService.ACTION_DATA_NOTIFY.equals(action)) {
				Log.i(TAG, "new data indicate");
				mBluetoothLeService.readCharacteristic(mNotifyCharateristic
						.get(0));
				// receiveBTMessage(intent);
			} else if (BluetoothLeService.ACTION_DATA_READ.equals(action)) {
				// receive message from CC
				Log.i(TAG, "new data to read");
				receiveBTMessage(intent);
			} else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)) {
				Log.i(TAG, "write successfully");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

		// Initilization
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		actionBar.setTitle(mDeviceName);

		mPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());

		viewPager.setAdapter(mPagerAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Adding tabs
		for (String tab_name : actionTabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));
		}

		/**
		 * on swiping the viewpager make respective tab select
		 */
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int pos) {
				actionBar.setSelectedNavigationItem(pos);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

		// start ble send thread
		mBLEThreadSend = new BLESendThread();
		mBLEThreadSend.bleThreadInit(HomeActivity.this);
		mBLEThreadSend.start();

		// start ble rec thread
		mBLEThreadRec = new BLERecThread();
		mBLEThreadRec.bleThreadInit(HomeActivity.this);
		mBLEThreadRec.start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		mBluetoothLeService = null;
		mBLEThreadSend.getLooper().quit();
		mBLEThreadRec.getLooper().quit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gatt_services, menu);
		if (mConnected) {
			menu.findItem(R.id.menu_connect).setVisible(false);
			menu.findItem(R.id.menu_disconnect).setVisible(true);
		} else {
			menu.findItem(R.id.menu_connect).setVisible(true);
			menu.findItem(R.id.menu_disconnect).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_connect:
			mBluetoothLeService.connect(mDeviceAddress);
			return true;
		case R.id.menu_disconnect:
			mBluetoothLeService.disconnect();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Add action to intentfilter
	 * 
	 * @return
	 */
	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_NOTIFY);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_INDICATE);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_READ);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITE);
		return intentFilter;
	}

	/***
	 * Get writable characteristic of BLE device
	 * 
	 * @param gattServices
	 * @return
	 */
	private ArrayList<BluetoothGattCharacteristic> getWriteCharacteristic(
			List<BluetoothGattService> gattServices) {
		ArrayList<BluetoothGattCharacteristic> mListChar = new ArrayList<BluetoothGattCharacteristic>();
		if (gattServices == null) {
			return null;
		}

		List<BluetoothGattCharacteristic> gattCharacteristics;
		int charaProp;

		for (BluetoothGattService gattService : gattServices) {
			gattCharacteristics = gattService.getCharacteristics();
			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				charaProp = gattCharacteristic.getProperties();
				if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
					mListChar.add(gattCharacteristic);
				}
			}
		}

		return mListChar;
	}

	/**
	 * Get notification Characteristic
	 * 
	 * @param gattServices
	 * @return
	 */
	private ArrayList<BluetoothGattCharacteristic> getReadCharacteristic(
			List<BluetoothGattService> gattServices) {
		ArrayList<BluetoothGattCharacteristic> mListChar = new ArrayList<BluetoothGattCharacteristic>();
		if (gattServices == null) {
			return null;
		}

		List<BluetoothGattCharacteristic> gattCharacteristics;
		int charaProp;

		for (BluetoothGattService gattService : gattServices) {
			gattCharacteristics = gattService.getCharacteristics();
			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				charaProp = gattCharacteristic.getProperties();
				if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0
						| (charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
					mListChar.add(gattCharacteristic);
				}
			}
		}

		return mListChar;
	}

	private void getServerDeviceData() {
		mProgDialog = ProgressDialog.show(HomeActivity.this, null,
				"loading data", false);
		// Get Characteristic support to write
		mWriteCharacteristic = getWriteCharacteristic(mBluetoothLeService
				.getSupportedGattServices());
		// Get notification Characteristic
		mNotifyCharateristic = getReadCharacteristic(mBluetoothLeService
				.getSupportedGattServices());
		if (mWriteCharacteristic == null) {
			showDialog("BLE device don't support to write :(");
			finish();
		}
		if (mNotifyCharateristic != null) {
			mBluetoothLeService.setCharacteristicNotification(
					mNotifyCharateristic.get(0), true);

		}

		/* step by step */
		requestNumOfDev(200);
		requestZoneName(11000, false);
		showDeviceListUI(14000);
		requestNumOfScene(16000);
		requestNumOfRule(20000);
		requestInactiveScene(31000);
		showSceneListUI(45000);
		/* request data 2nd */
		// Request num of scene again
		requestZoneName(33000, true);
		// showDeviceListUI(startTime + 35000);
//		requestAllDeviceIndex(37000);
		//
		// showDeviceListUI(startTime + 28000);
		// requestNumOfScene(startTime + 30000);
//		requestRuleWithIndexAgain(37000);

	}

	private void requestAllDeviceIndex(int iTime) {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (mNumOfDev > mDevInfoList.size()) {
					BluetoothMessage mMsg = new BluetoothMessage();
					mMsg.setType(BTMessageType.BLE_DATA);
					mMsg.setIndex(mBTMsgIndex);
					mMsg.setLength((byte) 4);
					mMsg.setCmdIdH((byte) CommandID.GET);
					mMsg.setCmdIdL((byte) CommandID.DEV_WITH_INDEX);
					mMsg.setPayload(DataConversion.int2ByteArr(0xFFFFFFFF));
					mProcessMsg.putBLEMessage(mMsg);
				}
			}
		}, iTime);
	}

	private void requestRuleWithIndexAgain(int iTime) {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				BluetoothMessage mMsg = new BluetoothMessage();
				mMsg.setType(BTMessageType.BLE_DATA);
				mMsg.setIndex(mBTMsgIndex);
				mMsg.setLength((byte) 10);
				mMsg.setCmdIdH((byte) CommandID.GET);
				mMsg.setCmdIdL((byte) CommandID.RULE_WITH_INDEX);

				ByteBuffer payloadBuf = ByteBuffer.allocate(10);
				payloadBuf.put(mSceneList.get(0).getName().getBytes());
				payloadBuf.put((byte) 0xFF);
				payloadBuf.put((byte) 0xFF);
				mMsg.setPayload(payloadBuf.array());
				payloadBuf.clear();
				mProcessMsg.putBLEMessage(mMsg);
			}
		}, iTime);

	}

	private void requestInactiveScene(int iTime) {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				Log.i(TAG, "request inactive scene");
				BluetoothMessage mMsg = new BluetoothMessage();
				mMsg.setType(BTMessageType.BLE_DATA);
				mMsg.setIndex(mBTMsgIndex);
				mMsg.setLength((byte) 1);
				mMsg.setCmdIdH((byte) CommandID.GET);
				mMsg.setCmdIdL((byte) CommandID.INACT_SCENE_WITH_INDEX);
				mMsg.setPayload(new byte[] { (byte) 0xFF });
				mProcessMsg.putBLEMessage(mMsg);

			}
		}, iTime);

	}

	private void requestNumOfRule(int iTime) {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (int i = 0; i < mSceneList.size(); i++) {
					// send request to get list rules of scene;
					if (mSceneList.get(i).getActived()) {
						BluetoothMessage msg = new BluetoothMessage();
						msg.setType(BTMessageType.BLE_DATA);
						msg.setIndex(mBTMsgIndex);
						msg.setLength((byte) 8);
						msg.setCmdIdH((byte) CommandID.GET);
						msg.setCmdIdL((byte) CommandID.NUM_OF_RULES);
						msg.setPayload(mSceneList.get(i).getName().getBytes());
						mProcessMsg.putBLEMessage(msg);
					}
				}
			}
		}, iTime);

	}

	private void requestNumOfScene(int iTime) {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Log.i(TAG, "request number of scene");
				BluetoothMessage msg = new BluetoothMessage();
				msg.setType(BTMessageType.BLE_DATA);
				msg.setIndex(mBTMsgIndex);
				msg.setLength((byte) 0);
				msg.setCmdIdH((byte) CommandID.GET);
				msg.setCmdIdL((byte) CommandID.NUM_OF_SCENES);
				mProcessMsg.putBLEMessage(msg);
			}
		}, iTime);
	}

	private void requestZoneName(int iTime, final boolean requestAgain) {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (!requestAgain) {
					mZoneList = getListOfZone(mDevInfoList);
				}
				BluetoothMessage btMsg = new BluetoothMessage();
				Log.i(TAG, "request zone's name ");
				btMsg.setType(BTMessageType.BLE_DATA);
				btMsg.setIndex(mBTMsgIndex);
				btMsg.setLength((byte) 1);
				btMsg.setCmdIdH((byte) CommandID.GET);
				btMsg.setCmdIdL((byte) CommandID.ZONE_NAME);
				btMsg.setPayload(new byte[] { (byte) 0xFF });
				mProcessMsg.putBLEMessage(btMsg);

			}
		}, iTime);

	}

	private void requestNumOfDev(int iTime) {
		// TODO Auto-generated method stub
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				BluetoothMessage msg = new BluetoothMessage();
				msg.setType(BTMessageType.BLE_DATA);
				msg.setIndex(mBTMsgIndex);
				msg.setLength((byte) 0);
				msg.setCmdIdH((byte) CommandID.GET);
				msg.setCmdIdL((byte) CommandID.NUM_OF_DEVS);
				mProcessMsg.putBLEMessage(msg);
				Log.i(TAG, "Get num of dev");
			}
		}, iTime);

	}

	/**
	 * Receive and process Bluetooth Message
	 * 
	 * @param intentsce
	 */
	private void receiveBTMessage(final Intent intent) {
		// BluetoothMessage mBTMsg = mProcessMsg.getBLEMessage(intent);
		// mProcessMsg.processBTMessage(mBTMsg);
		mBLEThreadRec.sendMessage(BTMessageType.BLE_READ, intent);
	}

	/**
	 * Show pop-up message
	 * 
	 * @param msg
	 */
	public void showDialog(CharSequence msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	// Precess ActionBar Tabs
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {

		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

	}

	private void showDeviceListUI(int timeout) {
		// display device list after 3s
		new CountDownTimer(timeout, timeout) {

			@Override
			public void onTick(long arg0) {

			}

			@Override
			public void onFinish() {

				if (!mDevInfoList.isEmpty()) {

					// mZoneList = getListOfZone(mDevInfoList);
					DeviceControlFragment mDeviceFrag = (DeviceControlFragment) getSupportFragmentManager()
							.getFragments().get(0);
					// mZoneList = getListOfZone(mDevInfoList);
					mDeviceFrag.updateUI(mZoneList);
				}
			}
		}.start();
	}

	private void showSceneListUI(int timeout) {
		new CountDownTimer(timeout, timeout) {

			@Override
			public void onTick(long arg0) {

			}

			@Override
			public void onFinish() {
				mProgDialog.dismiss();
				requestServerBusy = false;
				if (mDevInfoList.size() == 0) {
					Toast.makeText(getApplicationContext(),
							"have no any device", Toast.LENGTH_SHORT).show();
					finish();
					return;
				}
				ScenesFragment mSceneFrag = (ScenesFragment) getSupportFragmentManager()
						.getFragments().get(1);
				mSceneFrag.updateSceneUI(mSceneList);

			}
		}.start();
	}

	/**
	 * 
	 * @param timeout
	 */
	public void ble_timeout_send(int timeout) {
		new CountDownTimer(timeout, timeout) {

			@Override
			public void onTick(long millisUntilFinished) {
				if (mWriteBLESuccess == true) {
					cancel();
				}

			}

			@Override
			public void onFinish() {
				mWriteBLESuccess = true;

			}
		}.start();
	}

	/**
	 * Get zone list from device list
	 * 
	 * @param deviceList
	 */
	private ArrayList<Zone_c> getListOfZone(ArrayList<DeviceInfo> deviceList) {
		ArrayList<DeviceInfo> mDevList = new ArrayList<DeviceInfo>();
		ArrayList<Zone_c> mZoneList = new ArrayList<Zone_c>();
		mDevList = deviceList;
		int mZoneId;
		int mDevId;
		Zone_c zone = null;
		for (int i = 0; i < mDevList.size(); i++) {
			mZoneId = DataConversion.byte2Unsigned((byte) (mDevList.get(i)
					.getDevID() >> 24));
			mDevId = mDevList.get(i).getDevID();
			if (!searchZone(mZoneList, mZoneId)) {
				zone = new Zone_c();
				zone.setName(mZoneId);
				mZoneList.add(zone);
			}

			Device_c device = new Device_c();
			device.setName(mDevId);
			device.setVal(mDevList.get(i).getDevVal());
			mZoneList.get(getZoneIndex(mZoneList, mZoneId)).addChildListItem(
					device);
		}
		return mZoneList;
	}

	/**
	 * Search zone by ID
	 * 
	 * @param parentList
	 * @param item
	 * @return
	 */
	private boolean searchZone(ArrayList<Zone_c> parentList, int mZoneId) {
		if (parentList.isEmpty()) {
			return false;
		}
		for (Zone_c parent : parentList) {
			if (parent.getID() == mZoneId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get zone index in List
	 * 
	 * @param zoneList
	 * @param mZoneId
	 * @return
	 */
	private int getZoneIndex(ArrayList<Zone_c> zoneList, int mZoneId) {

		for (int i = 0; i < zoneList.size(); i++) {
			if (zoneList.get(i).getID() == mZoneId) {
				return i;
			}
		}
		return -1;
	}

}

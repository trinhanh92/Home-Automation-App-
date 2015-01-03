package anh.trinh.ble_demo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import anh.trinh.ble_demo.data.BTMessageType;
import anh.trinh.ble_demo.data.BluetoothMessage;
import anh.trinh.ble_demo.data.DataConversion;
import anh.trinh.ble_demo.data.DeviceInfo;
import anh.trinh.ble_demo.data.CommandID;
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
	public boolean mServerReady = false;
	public volatile boolean mWriteBLESuccess = false;
	public CountDownTimer mCountDown;
	public ThreadSignal mBLEThreadSignal = new ThreadSignal();
	public ArrayList<BluetoothGattCharacteristic> mWriteCharacteristic,
			mNotifyCharateristic;
	private BluetoothMessage mBTMsg;
	public short mBTMsgIndex = 0;
	public int mNumOfDev;
	public ArrayList<DeviceInfo> mDevInfoList = new ArrayList<DeviceInfo>();
	public ArrayList<Zone_c> mZoneList = new ArrayList<Zone_c>();
	public int mNumOfActScene;
	public int mNumOfInactScene;
	public ArrayList<Scene_c> mSceneList = new ArrayList<Scene_c>();
	public ArrayList<Scene_c> mSceneListUpdate = new ArrayList<Scene_c>();
	public ProcessBTMsg mProcessMsg = new ProcessBTMsg(HomeActivity.this);
	public BleThread mBLEThread;
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
				// mBLEThreadSignal.sendSignal();
				// mBLEThread.interrupt();
				// mWriteBLESuccess = true;
			}
		}
	};

	//
	//
	//
	// Handle Message from Threads
	public Handler mMsgHandler = new Handler() {
		private BluetoothMessage mMsg = new BluetoothMessage();

		@Override
		public void handleMessage(Message msg) {
			BluetoothMessage btMsg = (BluetoothMessage) msg.obj;
			switch (msg.what) {

			case CommandID.NUM_OF_DEVS:
				mMsg.setType(BTMessageType.BLE_DATA);
				mMsg.setIndex(mBTMsgIndex);
				mMsg.setLength((byte) 4);
				mMsg.setCmdIdH((byte) CommandID.GET);
				mMsg.setCmdIdL((byte) CommandID.DEV_WITH_INDEX);
				mMsg.setPayload(DataConversion.int2ByteArr(0xFFFFFFFF));
				mProcessMsg.putBLEMessage(mMsg);
				Log.i(TAG, "send DEV WITH INDEX");

				break;
			case CommandID.DEV_WITH_INDEX:
				// Get DeviceControlFragment from its index
				Log.i(TAG, "Received Msg");
				break;
			case CommandID.DEV_VAL:
				mProcessMsg.putBLEMessage(btMsg);
				break;
			case CommandID.NUM_OF_SCENES:
				// Request to get active scene
				mMsg.setType(BTMessageType.BLE_DATA);
				mMsg.setIndex(mBTMsgIndex);
				mMsg.setLength((byte) 1);
				mMsg.setCmdIdH((byte) CommandID.GET);
				mMsg.setCmdIdL((byte) CommandID.ACT_SCENE_WITH_INDEX);
				mMsg.setPayload(new byte[] { (byte) 0xFF });
				mProcessMsg.putBLEMessage(mMsg);
				break;
			case CommandID.NUM_OF_RULES:
				String sceneName = (String) msg.obj;
				mMsg.setType(BTMessageType.BLE_DATA);
				mMsg.setIndex(mBTMsgIndex);
				mMsg.setLength((byte) 10);
				mMsg.setCmdIdH((byte) CommandID.GET);
				mMsg.setCmdIdL((byte) CommandID.RULE_WITH_INDEX);

				ByteBuffer payloadBuf = ByteBuffer.allocate(10);
				payloadBuf.put(sceneName.getBytes());
				payloadBuf.put((byte) 0xFF);
				payloadBuf.put((byte) 0xFF);
				mMsg.setPayload(payloadBuf.array());
				payloadBuf.clear();
				mProcessMsg.putBLEMessage(mMsg);
				break;

			default:
				break;
			}
		};
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
				// TODO Auto-generated method stub
				actionBar.setSelectedNavigationItem(pos);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});

		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

		// start ble thread
		mBLEThread = new BleThread(HomeActivity.this);
		mBLEThread.start();
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
		mBLEThread.mHandler.getLooper().quit();
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
		// TODO Auto-generated method stub
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
		// mWriteCharacteristic.getProperties();
		// Log.i(TAG, "uuid:" + mWriteCharacteristic.getUuid().toString());
		// Log.i(TAG, "uuid:" + mNotifyCharateristic.getUuid().toString());
		if (mWriteCharacteristic == null) {
			showDialog("BLE device don't support to write :(");
			finish();
		}
		if (mNotifyCharateristic != null) {
			mBluetoothLeService.setCharacteristicNotification(
					mNotifyCharateristic.get(0), true);

		}

		/* step by step */
		int startTime = 3000;

		// Step1: Request number of device
		requestNumOfDev(200);
		// Step2: After timeout request device with index if missed
		// requestDevIndexAgain(startTime + 6000);
		// Step3: Request Zone name;
		requestZoneName(startTime + 6500);
		// Step4: Display Device List
		showDeviceListUI(startTime + 8500);
		// Step5: Request number of scene
		requestNumOfScene(startTime + 9000);
		// TODO: Step7: Request Num of Rule
		// Step6: Request Rule with index
		requestRuleIndex(startTime + 10000);
		// TODO: Step7: Request Rule with index again if missed
		// requestRuleIndexAgain(startTime + 13000);
		// Step9: Request inactscene list
		requestInactiveScene(startTime + 14000);
		// Step10: Display Scene List
		showSceneListUI(startTime + 16000);

	}

	/**
	 * Receive and process Bluetooth Message
	 * 
	 * @param intent
	 */
	private void receiveBTMessage(Intent intent) {
		mBTMsg = mProcessMsg.getBLEMessage(intent);
		mProcessMsg.processBTMessage(mBTMsg);
		// Message msg =
		// mBLEThread.mHandler.obtainMessage(BTMessageType.BLE_READ,
		// mBTMsg);
		// mBLEThread.mHandler.sendMessage(msg);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	public Handler getmMsgHandler() {
		return mMsgHandler;
	}

	public void setmMsgHandler(Handler mMsgHandler) {
		this.mMsgHandler = mMsgHandler;
	}

	/***********************************************************************************************
	 * 
	 **********************************************************************************************/

	// search device index in list
	private boolean isExistDevIndex(ArrayList<DeviceInfo> mDevList, int index) {
		for (int i = 0; i < mDevList.size(); i++) {
			if (mDevList.get(i).getDevIdx() == index) {
				return true;
			}
		}
		Log.i(TAG, "there was full list of devices");
		return false;
	}

	// search scene index in list
	private boolean isNonExistSceneIndex(ArrayList<Scene_c> mSceneList,
			int index) {
		for (int i = 0; i < mSceneList.size(); i++) {
			if (mSceneList.get(i).getID() != index) {
				return true;
			}
		}
		return false;
	}

	/***********************************************************************************************
	 * Implementation timeout functions
	 * 
	 ***********************************************************************************************/
	private void requestNumOfDev(int timeout) {
		// Request Number of devices
		new CountDownTimer(timeout, timeout) {

			@Override
			public void onTick(long arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish() {
				// TODO Get number of devices
				BluetoothMessage msg = new BluetoothMessage();
				msg.setType(BTMessageType.BLE_DATA);
				msg.setIndex(mBTMsgIndex);
				msg.setLength((byte) 0);
				msg.setCmdIdH((byte) CommandID.GET);
				msg.setCmdIdL((byte) CommandID.NUM_OF_DEVS);
				mProcessMsg.putBLEMessage(msg);
				Log.i(TAG, "Get num of dev");
			}
		}.start();
	}

	private void requestZoneName(int timeout) {
		new CountDownTimer(timeout, timeout) {

			@Override
			public void onTick(long millisUntilFinished) {

			}

			@Override
			public void onFinish() {
				mZoneList = getListOfZone(mDevInfoList);
				BluetoothMessage btMsg = new BluetoothMessage();
				for (int i = 0; i < mZoneList.size(); i++) {
					Log.i(TAG, "request zone's name "
							+ mZoneList.get(i).getID());			
					btMsg.setType(BTMessageType.BLE_DATA);
					btMsg.setIndex(mBTMsgIndex);
					btMsg.setLength((byte) 1);
					btMsg.setCmdIdH((byte) CommandID.GET);
					btMsg.setCmdIdL((byte) CommandID.ZONE_NAME);
					btMsg.setPayload(new byte[] { (byte) mZoneList.get(i)
							.getID() });
					mProcessMsg.putBLEMessage(btMsg);
					
					for(int k = 100000000; k > 0 ; k--);
				}

			}
		}.start();
	}

	private void showDeviceListUI(int timeout) {
		// display device list after 3s
		new CountDownTimer(timeout, timeout) {

			@Override
			public void onTick(long arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				if (!mDevInfoList.isEmpty()) {
					// TODO Auto-generated method stub
					// mZoneList = getListOfZone(mDevInfoList);
					DeviceControlFragment mDeviceFrag = (DeviceControlFragment) getSupportFragmentManager()
							.getFragments().get(0);
					// mZoneList = getListOfZone(mDevInfoList);
					mDeviceFrag.updateUI(mZoneList);
				}
			}
		}.start();
	}

	private void requestDevIndexAgain(int timeout) {
		new CountDownTimer(timeout, timeout) {

			@Override
			public void onTick(long millisUntilFinished) {

			}

			@Override
			public void onFinish() {
				Log.i(TAG, "Request device index again");
				if (mDevInfoList.size() < mNumOfDev) {
					for (int i = 0; i < mNumOfDev; i++) {
						if (!isExistDevIndex(mDevInfoList, i)) {
							BluetoothMessage btMsg = new BluetoothMessage();
							btMsg.setType(BTMessageType.BLE_DATA);
							btMsg.setIndex(mBTMsgIndex);
							btMsg.setLength((byte) 4);
							btMsg.setCmdIdH((byte) CommandID.GET);
							btMsg.setCmdIdL((byte) CommandID.DEV_WITH_INDEX);
							btMsg.setPayload(DataConversion.int2ByteArr(i));
							mProcessMsg.putBLEMessage(btMsg);
						}
					}
				}

			}
		}.start();
	}

	private void requestNumOfScene(int timeout) {
		new CountDownTimer(timeout, timeout) {

			@Override
			public void onTick(long arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish() {
				// send request to get number of scene;
				Log.i(TAG, "request number of scene");
				BluetoothMessage msg = new BluetoothMessage();
				msg.setType(BTMessageType.BLE_DATA);
				msg.setIndex(mBTMsgIndex);
				msg.setLength((byte) 0);
				msg.setCmdIdH((byte) CommandID.GET);
				msg.setCmdIdL((byte) CommandID.NUM_OF_SCENES);
				mProcessMsg.putBLEMessage(msg);

			}
		}.start();
	}

	private void requestInactiveScene(int timeout) {
		// get inactive scene
		new CountDownTimer(timeout, timeout) {

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish() {
				// Request to get inactive scene
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
		}.start();
	}

	private void requestRuleIndex(int timeout) {
		// request rules
		new CountDownTimer(timeout, timeout) {

			@Override
			public void onTick(long arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				for (int i = 0; i < mSceneList.size(); i++) {
					// send request to get list rules of scene;
					if (mSceneList.get(i).getActived()) {
						BluetoothMessage msg = new BluetoothMessage();
						msg.setType(BTMessageType.BLE_DATA);
						msg.setIndex(mBTMsgIndex);
						msg.setLength((byte) 10);
						msg.setCmdIdH((byte) CommandID.GET);
						msg.setCmdIdL((byte) CommandID.RULE_WITH_INDEX);

						ByteBuffer payloadBuf = ByteBuffer.allocate(10);
						payloadBuf.put(mSceneList.get(i).getName().getBytes());
						payloadBuf.put((byte) 0xFF);
						payloadBuf.put((byte) 0xFF);
						msg.setPayload(payloadBuf.array());
						payloadBuf.clear();
						mProcessMsg.putBLEMessage(msg);
					}
				}
			}
		}.start();
	}

	private void requestRuleIndexAgain(int timeout) {
		for (int i = 0; i < mSceneList.size(); i++) {
			if (mSceneList.get(i).getActived() == true) {
				mSceneList.get(i).getName();
			}
		}
		new CountDownTimer(timeout, timeout) {

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				if (mSceneList.get(0).getNumOfRule() < mNumOfInactScene) {
					for (int i = 0; i < mNumOfDev; i++) {
						if (isExistDevIndex(mDevInfoList, i)) {
							BluetoothMessage btMsg = new BluetoothMessage();
							btMsg.setType(BTMessageType.BLE_DATA);
							btMsg.setIndex(mBTMsgIndex);
							btMsg.setLength((byte) 4);
							btMsg.setCmdIdH((byte) CommandID.GET);
							btMsg.setCmdIdL((byte) CommandID.RULE_WITH_INDEX);
							btMsg.setPayload(DataConversion.int2ByteArr(i));
							mProcessMsg.putBLEMessage(btMsg);
						}
					}
				}
			}
		}.start();
	}

	private void requestSceneIndexAgain(int timeout) {
		new CountDownTimer(timeout, timeout) {

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				if (mSceneList.size() < mNumOfActScene) {
					for (int i = 0; i < mNumOfActScene; i++) {
						if (isNonExistSceneIndex(mSceneList, i)) {
							BluetoothMessage btMsg = new BluetoothMessage();
							btMsg.setType(BTMessageType.BLE_DATA);
							btMsg.setIndex(mBTMsgIndex);
							btMsg.setLength((byte) 4);
							btMsg.setCmdIdH((byte) CommandID.GET);
							btMsg.setCmdIdL((byte) CommandID.ACT_SCENE_WITH_INDEX);
							btMsg.setPayload(DataConversion.int2ByteArr(i));
							mProcessMsg.putBLEMessage(btMsg);
						}
					}
				}
			}
		}.start();
	}

	private void showSceneListUI(int timeout) {
		new CountDownTimer(timeout, timeout) {

			@Override
			public void onTick(long arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish() {
				mProgDialog.dismiss();

				if (mDevInfoList.size() == 0) {
					Toast.makeText(getApplicationContext(),
							"have no any device", Toast.LENGTH_SHORT).show();
					finish();
					return;
				}
				ScenesFragment mSceneFrag = (ScenesFragment) getSupportFragmentManager()
						.getFragments().get(1);
				mSceneFrag.updateSceneUI(mSceneList);

				// for (int i = 0; i < mSceneList.size(); i++) {
				// // send request to get list rules of scene;
				// if(!mSceneList.get(i).getActived()){
				// BluetoothMessage msg = new BluetoothMessage();
				// msg.setType(BTMessageType.BLE_DATA);
				// msg.setIndex(mBTMsgIndex);
				// msg.setLength((byte) 8);
				// msg.setCmdIdH((byte) CommandID.GET);
				// msg.setCmdIdL((byte) CommandID.NUM_OF_RULES);
				// msg.setPayload(mSceneList.get(i).getName().getBytes());
				// mProcessMsg.putBLEMessage(mWriteCharacteristic, msg);
				// }
				// }

			}
		}.start();
	}

	/**
	 * Get zone list from device list
	 * 
	 * @param deviceList
	 */
	public ArrayList<Zone_c> getListOfZone(ArrayList<DeviceInfo> deviceList) {
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

}

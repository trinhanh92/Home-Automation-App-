package anh.trinh.ble_demo;

import java.io.ObjectOutputStream.PutField;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
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
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import anh.trinh.ble_demo.data.BTMessageType;
import anh.trinh.ble_demo.data.BluetoothMessage;
import anh.trinh.ble_demo.data.DataConversion;
import anh.trinh.ble_demo.data.DeviceInfo;
import anh.trinh.ble_demo.data.CommandID;
import anh.trinh.ble_demo.data.ProcessBTMsg;
import anh.trinh.ble_demo.list_view.Rule_c;
import anh.trinh.ble_demo.list_view.Scene_c;
import anh.trinh.ble_demo.thread_sync.ACKisReceived;
import anh.trinh.ble_demo.thread_sync.BLEWriteThread;
import anh.trinh.ble_demo.thread_sync.MonitorObject;

public class HomeActivity extends FragmentActivity implements TabListener {

	private final static String TAG = HomeActivity.class.getSimpleName();

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	public static final int TIMEOUT_REC_BLE_MSG = 2000;
	private TextView mConnectionState;
	private String mDeviceName;
	private String mDeviceAddress;
	private ViewPager viewPager;
	private TabsPagerAdapter mPagerAdapter;
	private ActionBar actionBar;
	// Tab titles
	private String[] actionTabs = { "Device Control", "Scenes" };

	public BluetoothLeService mBluetoothLeService;

	private boolean mConnected = false;
	public boolean mServerReady = false;
	public MonitorObject mWriteSuccess = new MonitorObject();
	public boolean mWrited = true;
	public BluetoothGattCharacteristic mWriteCharacteristic,
			mNotifyCharateristic;
	private BluetoothMessage mBTMsg;
	public short mBTMsgIndex = 0;
	public int mNumOfDev;
	public ArrayList<DeviceInfo> mDevInfoList = new ArrayList<DeviceInfo>();
	public int mNumOfActScene;
	public int mNumOfInactScene;
	public ArrayList<Scene_c> mActSceneList = new ArrayList<Scene_c>();
	public ProcessBTMsg mProcessMsg = new ProcessBTMsg(HomeActivity.this);

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
				// mBLEWriteThread.start();
				getServerDeviceData();
			} else if (BluetoothLeService.ACTION_DATA_NOTIFY.equals(action)) {
				Log.i(TAG, "new data indicate");
				mBluetoothLeService.readCharacteristic(mNotifyCharateristic);
				// receiveBTMessage(intent);
			} else if (BluetoothLeService.ACTION_DATA_READ.equals(action)) {
				// receive message from CC
				Log.i(TAG, "new data to read");
				receiveBTMessage(intent);
			} else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)) {
				Log.i(TAG, "write successfully");
				mWrited = true;
				synchronized (mWriteSuccess) {
					mWriteSuccess.notify();
				}
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
				try {
					mProcessMsg.putBLEMessage(mWriteCharacteristic, mMsg);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Log.i(TAG, "send DEV WITH INDEX");

				break;
			case CommandID.DEV_WITH_INDEX:
				// Get DeviceControlFragment from its index
				Log.i(TAG, "Received Msg");
				break;
			case CommandID.DEV_VAL:
				try {
					mProcessMsg.putBLEMessage(mWriteCharacteristic, btMsg);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case CommandID.NUM_OF_SCENES:
				// Request to get active scene
				mMsg.setType(BTMessageType.BLE_DATA);
				mMsg.setIndex(mBTMsgIndex);
				mMsg.setLength((byte) 1);
				mMsg.setCmdIdH((byte) CommandID.GET);
				mMsg.setCmdIdL((byte) CommandID.ACT_SCENE_WITH_INDEX);
				mMsg.setPayload(new byte[] { (byte) 0xFF });
				try {
					mProcessMsg.putBLEMessage(mWriteCharacteristic, mMsg);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

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
	private BluetoothGattCharacteristic getWriteCharacteristic(
			List<BluetoothGattService> gattServices) {
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
					return gattCharacteristic;
				}
			}
		}

		return null;
	}

	/**
	 * Get notification Characteristic
	 * 
	 * @param gattServices
	 * @return
	 */
	private BluetoothGattCharacteristic getReadCharacteristic(
			List<BluetoothGattService> gattServices) {
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
					return gattCharacteristic;
				}
			}
		}

		return null;
	}

	private void getServerDeviceData() {
		// Get Characteristic support to write
		mWriteCharacteristic = getWriteCharacteristic(mBluetoothLeService
				.getSupportedGattServices());
		// Get notification Characteristic
		mNotifyCharateristic = getReadCharacteristic(mBluetoothLeService
				.getSupportedGattServices());
		int mCharaProp = mWriteCharacteristic.getProperties();
		Log.i(TAG, "uuid:" + mWriteCharacteristic.getUuid().toString());
		Log.i(TAG, "uuid:" + mNotifyCharateristic.getUuid().toString());
		if (mWriteCharacteristic == null) {
			showDialog("BLE device don't support to write :(");
			finish();
		}
		if (mNotifyCharateristic != null) {
			mBluetoothLeService.setCharacteristicNotification(
					mNotifyCharateristic, true);
		}

		/* step by step */
		int startTime = 3000;
		
		//Step1: Request number of device
		requestNumOfDev(200);
		//Step2:  After timeout request device with index if missed
//		requestDevIndexAgain(startTime);
		//TODO: Step3: Request Zone name;
		
		//Step4: Display Device List
		showDeviceListUI(startTime + 300);
		//Step5: Request number of scene
		requestNumOfScene(startTime + 500);
		//TODO: Step7: Request Num of Rule
		//Step6: Request Rule with index
		requestRuleIndex(startTime + 1300);
		//TODO: Step7: Request Rule with index again if missed
//		requestRuleIndexAgain(startTime + 2000);
		//Step9: Request inactscene list
//		requestInactiveScene(startTime + 2300);
		//Step10: Display Scene List
		showSceneListUI(startTime + 3000);

	}

	/**
	 * Receive and process Bluetooth Message
	 * 
	 * @param intent
	 */
	private void receiveBTMessage(Intent intent) {
		mBTMsg = mProcessMsg.getBLEMessage(intent);
		mProcessMsg.processBTMessage(mBTMsg);
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
	private boolean isNonExistDevIndex(ArrayList<DeviceInfo> mDevList, int index) {
		for (int i = 0; i < mDevList.size(); i++) {
			if (mDevList.get(i).getDevIdx() != index) {
				return true;
			}
		}
		return false;
	}

	// search rule index in list
	private boolean isNonExistRuleIndex(ArrayList<Rule_c> mRuleList, int index) {
		for (int i = 0; i < mRuleList.size(); i++) {
			if (mRuleList.get(i).getRuleIndex() != index) {
				return true;
			}
		}
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
				try {
					mProcessMsg.putBLEMessage(mWriteCharacteristic, msg);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.i(TAG, "Get num of dev");
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
					DeviceControlFragment mDeviceFrag = (DeviceControlFragment) getSupportFragmentManager()
							.getFragments().get(0);
					mDeviceFrag.updateUI(mDevInfoList);
				}
			}
		}.start();
	}

	private void requestDevIndexAgain(int timeout) {
		new CountDownTimer(timeout, timeout) {

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				if (mDevInfoList.size() < mNumOfDev) {
					for (int i = 0; i < mNumOfDev; i++) {
						if (isNonExistDevIndex(mDevInfoList, i)) {
							BluetoothMessage btMsg = new BluetoothMessage();
							btMsg.setType(BTMessageType.BLE_DATA);
							btMsg.setIndex(mBTMsgIndex);
							btMsg.setLength((byte) 4);
							btMsg.setCmdIdH((byte) CommandID.GET);
							btMsg.setCmdIdL((byte) CommandID.DEV_WITH_INDEX);
							btMsg.setPayload(DataConversion.int2ByteArr(i));
							try {
								mProcessMsg.putBLEMessage(mWriteCharacteristic,
										btMsg);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
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
				BluetoothMessage msg = new BluetoothMessage();
				msg.setType(BTMessageType.BLE_DATA);
				msg.setIndex(mBTMsgIndex);
				msg.setLength((byte) 0);
				msg.setCmdIdH((byte) CommandID.GET);
				msg.setCmdIdL((byte) CommandID.NUM_OF_SCENES);
				try {
					mProcessMsg.putBLEMessage(mWriteCharacteristic, msg);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

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
				BluetoothMessage mMsg = new BluetoothMessage();
				mMsg.setType(BTMessageType.BLE_DATA);
				mMsg.setIndex(mBTMsgIndex);
				mMsg.setLength((byte) 1);
				mMsg.setCmdIdH((byte) CommandID.GET);
				mMsg.setCmdIdL((byte) CommandID.INACT_SCENE_WITH_INDEX);
				mMsg.setPayload(new byte[] { (byte) 0xFF });
				try {
					mProcessMsg.putBLEMessage(mWriteCharacteristic, mMsg);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

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
				for (int i = 0; i < mActSceneList.size(); i++) {
					// send request to get list rules of scene;
					BluetoothMessage msg = new BluetoothMessage();
					msg.setType(BTMessageType.BLE_DATA);
					msg.setIndex(mBTMsgIndex);
					msg.setLength((byte) 10);
					msg.setCmdIdH((byte) CommandID.GET);
					msg.setCmdIdL((byte) CommandID.RULE_WITH_INDEX);

					ByteBuffer payloadBuf = ByteBuffer.allocate(10);
					payloadBuf.put(mActSceneList.get(i).getName().getBytes());
					payloadBuf.put((byte) 0xFF);
					payloadBuf.put((byte) 0xFF);
					msg.setPayload(payloadBuf.array());
					payloadBuf.clear();
					try {
						mProcessMsg.putBLEMessage(mWriteCharacteristic, msg);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	private void requestRuleIndexAgain(int timeout) {
		String sceneName;
		for(int i = 0; i < mActSceneList.size(); i++){
			if(mActSceneList.get(i).getActived() == true){
				sceneName = mActSceneList.get(i).getName();
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
//				mActSceneList.get(0).g
				if (mActSceneList.get(0).getNumOfRule() < mNumOfInactScene) {
					for (int i = 0; i < mNumOfDev; i++) {
						if (isNonExistDevIndex(mDevInfoList, i)) {
							BluetoothMessage btMsg = new BluetoothMessage();
							btMsg.setType(BTMessageType.BLE_DATA);
							btMsg.setIndex(mBTMsgIndex);
							btMsg.setLength((byte) 4);
							btMsg.setCmdIdH((byte) CommandID.GET);
							btMsg.setCmdIdL((byte) CommandID.DEV_WITH_INDEX);
							btMsg.setPayload(DataConversion.int2ByteArr(i));
							try {
								mProcessMsg.putBLEMessage(mWriteCharacteristic,
										btMsg);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}

			}
		}.start();
	}
	
	private void requestSceneIndexAgain(int timeout){
		new CountDownTimer(timeout, timeout) {

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				if (mActSceneList.size() < mNumOfActScene) {
					for (int i = 0; i < mNumOfActScene; i++) {
						if (isNonExistSceneIndex(mActSceneList, i)) {
							BluetoothMessage btMsg = new BluetoothMessage();
							btMsg.setType(BTMessageType.BLE_DATA);
							btMsg.setIndex(mBTMsgIndex);
							btMsg.setLength((byte) 4);
							btMsg.setCmdIdH((byte) CommandID.GET);
							btMsg.setCmdIdL((byte) CommandID.ACT_SCENE_WITH_INDEX);
							btMsg.setPayload(DataConversion.int2ByteArr(i));
							try {
								mProcessMsg.putBLEMessage(mWriteCharacteristic,
										btMsg);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
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
				ScenesFragment mSceneFrag = (ScenesFragment) getSupportFragmentManager()
						.getFragments().get(1);
				mSceneFrag.updateSceneUI(mActSceneList);
			}
		}.start();
	}
}

package anh.trinh.ble_demo;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;
import anh.trinh.ble_demo.data.BTMessageType;
import anh.trinh.ble_demo.data.BluetoothMessage;
import anh.trinh.ble_demo.data.CommandID;
import anh.trinh.ble_demo.data.DataConversion;
import anh.trinh.ble_demo.data.DeviceInfo;
import anh.trinh.ble_demo.data.ProcessBTMsg;
import anh.trinh.ble_demo.list_view.Device_c;
import anh.trinh.ble_demo.list_view.Zone_c;

public class RequestBLEDataThread extends Thread {
	private final String TAG = "Request ble data thread";
	private HomeActivity mContext;
	private ProcessBTMsg mProcessMsg;
	
	public RequestBLEDataThread(HomeActivity mContext) {
		setPriority(MAX_PRIORITY);
		this.mContext = mContext;
		mProcessMsg = new ProcessBTMsg(mContext);
	}
	
	@Override
	public void run() {
		super.run();
		requestNumOfDev(200);
		requestZoneName(11000);
		requestNumOfScene(16000);
		requestNumOfRule(20000);
		requestInactiveScene(31000);
	}
	
	
	
	private void requestNumOfDev(int timeout){
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				BluetoothMessage msg = new BluetoothMessage();
				msg.setType(BTMessageType.BLE_DATA);
				msg.setIndex(mContext.mBTMsgIndex);
				msg.setLength((byte) 0);
				msg.setCmdIdH((byte) CommandID.GET);
				msg.setCmdIdL((byte) CommandID.NUM_OF_DEVS);
				mProcessMsg.putBLEMessage(msg);
				Log.i(TAG, "Get num of dev");
			}
		}, timeout);
	}
	
	private void requestZoneName(int timeout){
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				mContext.mZoneList = getListOfZone(mContext.mDevInfoList);
				BluetoothMessage btMsg = new BluetoothMessage();
				Log.i(TAG, "request zone's name ");
				btMsg.setType(BTMessageType.BLE_DATA);
				btMsg.setIndex(mContext.mBTMsgIndex);
				btMsg.setLength((byte) 1);
				btMsg.setCmdIdH((byte) CommandID.GET);
				btMsg.setCmdIdL((byte) CommandID.ZONE_NAME);
				btMsg.setPayload(new byte[] { (byte) 0xFF });
				mProcessMsg.putBLEMessage(btMsg);
			}
		}, timeout);
	}
	
	private void requestNumOfScene(int timeout){
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				Log.i(TAG, "request number of scene");
				BluetoothMessage msg = new BluetoothMessage();
				msg.setType(BTMessageType.BLE_DATA);
				msg.setIndex(mContext.mBTMsgIndex);
				msg.setLength((byte) 0);
				msg.setCmdIdH((byte) CommandID.GET);
				msg.setCmdIdL((byte) CommandID.NUM_OF_SCENES);
				mProcessMsg.putBLEMessage(msg);
			}
		}, timeout);
	}
	
	private void requestNumOfRule(int timeout){
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				for (int i = 0; i < mContext.mSceneList.size(); i++) {
					// send request to get list rules of scene;
					if (mContext.mSceneList.get(i).getActived()) {
						BluetoothMessage msg = new BluetoothMessage();
						msg.setType(BTMessageType.BLE_DATA);
						msg.setIndex(mContext.mBTMsgIndex);
						msg.setLength((byte) 8);
						msg.setCmdIdH((byte) CommandID.GET);
						msg.setCmdIdL((byte) CommandID.NUM_OF_RULES);
						msg.setPayload(mContext.mSceneList.get(i).getName().getBytes());
						mProcessMsg.putBLEMessage(msg);
					}
				}
			}
		}, timeout);
	}
	
	private void requestInactiveScene(int timeout){
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				Log.i(TAG, "request inactive scene");
				BluetoothMessage mMsg = new BluetoothMessage();
				mMsg.setType(BTMessageType.BLE_DATA);
				mMsg.setIndex(mContext.mBTMsgIndex);
				mMsg.setLength((byte) 1);
				mMsg.setCmdIdH((byte) CommandID.GET);
				mMsg.setCmdIdL((byte) CommandID.INACT_SCENE_WITH_INDEX);
				mMsg.setPayload(new byte[] { (byte) 0xFF });
				mProcessMsg.putBLEMessage(mMsg);
			}
		}, timeout);
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

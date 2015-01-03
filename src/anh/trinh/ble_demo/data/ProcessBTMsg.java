package anh.trinh.ble_demo.data;

/***********************************************************************************************
 * 						BLE Packet format:
 * _________________________________________________________________
 * _________Header_________|__________________Message_______________
 * | msgType | msgIndex    | Length | CmdId(1) | CmdId(2) | Payload
 * |____1____|______2______|____1___|____1_____|_____1____|_________
 * 
 * msgType: Data or ACK
 * msgIndex: Index of message
 * Length: Length of payload
 * CmdId1:
 * 		- GET
 * 		- SET
 * CmdId2:
 * 		- NUM_OF_DEVS
 * 		- DEV_WITH_INDEX
 * 		- DEV_VAL	
 * 		- ...
 * 
 **********************************************************************************************/

import java.nio.ByteBuffer;
import java.util.ArrayList;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import anh.trinh.ble_demo.BluetoothLeService;
import anh.trinh.ble_demo.DeviceControlFragment;
import anh.trinh.ble_demo.HomeActivity;
import anh.trinh.ble_demo.ScenesFragment;
import anh.trinh.ble_demo.list_view.Device_c;
import anh.trinh.ble_demo.list_view.Rule_c;
import anh.trinh.ble_demo.list_view.Scene_c;

public class ProcessBTMsg {
	private HomeActivity mContext;
	private final static String TAG = "ProcessMessage";

	public ProcessBTMsg(HomeActivity mContext) {
		this.mContext = mContext;
	}

	/*********************************************************************************************
 * 
 *********************************************************************************************/
	/**
	 * Get BLE device data
	 * 
	 * @param intent
	 * @return
	 */
	public BluetoothMessage getBLEMessage(Intent intent) {
		byte[] recBuf = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
		for (int i = 0; i < recBuf.length; i++) {
			System.out.printf("%d ", recBuf[i]);
		}
		System.out.println();

		BluetoothMessage msg = parseBTMessage(recBuf);
		return msg;

	}

	/**
	 * Send data to BLE device
	 * @param msg
	 * 
	 * @return
	 */
	public void putBLEMessage(BluetoothMessage msg) {
		ByteBuffer sendBuf = ByteBuffer.allocate(msg.getLength() + 6);
		sendBuf.put(msg.getType());
		sendBuf.put(DataConversion.short2ByteArr(msg.getIndex()));
		sendBuf.put(msg.getLength());
		sendBuf.put(msg.getCmdIdH());
		sendBuf.put(msg.getCmdIdL());
		if (msg.getLength() != 0) {
			sendBuf.put(msg.getPayload());
		}

		// System.out.println("data send to BLE");
		// for (int i = 0; i < msg.getLength() + 6; i++) {
		// System.out.printf("%d ", sendBuf.array()[i]);
		// }
		// System.out.println();

		// characteristic.setValue(sendBuf.array());
		// sendBuf.clear();
		// mContext.mBTMsgIndex++;
		// synchronized (mContext.mWriteSuccess) {
		// if (!mContext.mWrited) {
		// // mContext.mWriteSuccess.wait(timeout);
		// try {
		// mContext.mWriteSuccess.wait(timeout);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// }
		// mContext.mBluetoothLeService.writeCharacteristic(characteristic);
		// mContext.mWrited = false;
		mContext.mBTMsgIndex++;
		Bundle mBundle = new Bundle();
		mBundle.putByteArray("BLE_MSG_SEND", sendBuf.array());
		sendBuf.clear();

		Message btMsg = mContext.mBLEThread.mHandler.obtainMessage(
				BTMessageType.BLE_WRITE_DATA, mBundle);
		mContext.mBLEThread.mHandler.sendMessage(btMsg);
		// sendBuf.clear();
	}

	/**
	 * Send ACK back to BLE device
	 * 
	 * @param characteristic
	 * @param msg
	 */
	public void sendBLEMessageACK(BluetoothGattCharacteristic characteristic,
			short msgIndex) {
		Log.i(TAG, "send ACK " + msgIndex);
		ByteBuffer sendBuf = ByteBuffer.allocate(4);
		sendBuf.put(BTMessageType.BLE_ACK);
		sendBuf.put(DataConversion.short2ByteArr(msgIndex));
		sendBuf.put((byte) 0);

		Bundle mBundle = new Bundle();
		mBundle.putByteArray("BLE_MSG_SEND", sendBuf.array());
		sendBuf.clear();

		Message btMsg = mContext.mBLEThread.mHandler.obtainMessage(
				BTMessageType.BLE_WRITE_ACK, mBundle);
		mContext.mBLEThread.mHandler.sendMessage(btMsg);
	}

	/**
	 * Parse Data Buffer receive to Bluetooth Message
	 * 
	 * @param message
	 * @return
	 */
	public BluetoothMessage parseBTMessage(byte[] msgBuf) {
		BluetoothMessage BTMsg;
		byte msgType = msgBuf[0];
		final short msgIndex = DataConversion.byteArr2Short(msgBuf[1],
				msgBuf[2]);
		if (msgType == BTMessageType.BLE_ACK) {
			// BTMsg = new BluetoothMessage();
			// BTMsg.setType(msgType);
			// BTMsg.setIndex(msgIndex);
			// BTMsg.setLength(msgLen);
			Log.i(TAG, "ACK from BLE");
			// mContext.mBLEThread.interrupt();
//			mContext.mWriteBLESuccess = true;
			mContext.mBLEThreadSignal.sendSignal();
			return null;

		} else {
			byte msgLen = msgBuf[3];
			byte cmdIdH = msgBuf[4];
			byte cmdIdL = msgBuf[5];
			byte[] payload = new byte[msgLen];
			for (int i = 0; i < msgLen; i++) {
				payload[i] = msgBuf[i + 6];
			}

			sendBLEMessageACK(mContext.mWriteCharacteristic.get(1), msgIndex);
			BTMsg = new BluetoothMessage(msgType, msgIndex, msgLen, cmdIdH,
					cmdIdL, payload);
		}

		return BTMsg;
	}

	/**
	 * Process Message from BLE device
	 * 
	 * @param msgQueue
	 */
	public void processBTMessage(BluetoothMessage btMsg) {
		Message handlerMsg;
		String sceneName;
		ScenesFragment mSceneFrag = (ScenesFragment) mContext
				.getSupportFragmentManager().getFragments().get(1);

		if (btMsg == null) {
			return;
		}

		int len = btMsg.getLength();
		// Get data payload
		ByteBuffer dataBuf = ByteBuffer.allocate(len);
		dataBuf.put(btMsg.getPayload());

		// Analyze kind of massage
		switch (btMsg.getCmdIdL()) {

		case CommandID.NUM_OF_DEVS:
			Log.i(TAG, "receive num of dev");
			mContext.mNumOfDev = btMsg.getPayload()[0];
			handlerMsg = mContext.mMsgHandler
					.obtainMessage(CommandID.NUM_OF_DEVS);
			mContext.mMsgHandler.sendMessage(handlerMsg);
			break;
		case CommandID.DEV_WITH_INDEX:
			Log.i(TAG, "dev with index");
			getDeviceList(dataBuf);
			break;
		case CommandID.DEV_VAL:
			Log.i(TAG, "dev val");
			// update device value from CC
			updateDeviceValue(btMsg.getPayload());
			break;
		case CommandID.ZONE_NAME:
			getZoneName(btMsg.getPayload());
			break;
		case CommandID.NUM_OF_SCENES:
			Log.i(TAG, "num of scene");
			mContext.mNumOfActScene = btMsg.getPayload()[0];
			mContext.mNumOfInactScene = btMsg.getPayload()[1];
			handlerMsg = mContext.mMsgHandler
					.obtainMessage(CommandID.NUM_OF_SCENES);
			mContext.mMsgHandler.sendMessage(handlerMsg);

			break;
		case CommandID.ACT_SCENE_WITH_INDEX:
			Log.i(TAG, "act scene with index");
			getSceneList(btMsg.getPayload(), true);
			break;
		case CommandID.INACT_SCENE_WITH_INDEX:
			Log.i(TAG, "inactive scene with index");
			getSceneList(dataBuf.array(), false);
			break;
		case CommandID.NUM_OF_RULES:
			Log.i(TAG, "num of rule");
			sceneName = new String(DataConversion.getBytesFromArray(0, 8,
					btMsg.getPayload()));
			if (btMsg.getCmdIdH() == CommandID.SET) {
				handlerMsg = mContext.mMsgHandler
						.obtainMessage(CommandID.NUM_OF_RULES);
				mContext.mMsgHandler.obtainMessage(CommandID.NUM_OF_RULES,
						sceneName);
				mContext.mMsgHandler.sendMessage(handlerMsg);
			} else {
				sendNumOfRule(sceneName);
			}

			break;
		case CommandID.RULE_WITH_INDEX:
			Log.i(TAG, "rule with index");
			if (btMsg.getCmdIdH() == CommandID.SET) {
				getRuleList(dataBuf);
			} else {
				sceneName = new String(DataConversion.getBytesFromArray(0, 8,
						btMsg.getPayload()));
				short ruleIndex = dataBuf.getShort(8);
				sendRuleListToCC(sceneName, ruleIndex);
			}
			break;
		case CommandID.RENAME_SCENE:
			Log.i(TAG, "rename scene");
			sceneName = new String(DataConversion.getBytesFromArray(0, 8,
					btMsg.getPayload()));
			Toast.makeText(mContext, "rename scene successfully",
					Toast.LENGTH_SHORT).show();

			mContext.mSceneList.clear();
			mContext.mSceneList.addAll(mSceneFrag.getListOfScene());
			// mSceneFrag.updateSceneUI(mContext.mSceneList);
			break;
		case CommandID.REMOVE_SCENE:
			Log.i(TAG, "remove scene");
			sceneName = new String(DataConversion.getBytesFromArray(0, 8,
					btMsg.getPayload()));
			Toast.makeText(mContext, "remove scene successfully!",
					Toast.LENGTH_SHORT).show();

			mContext.mSceneList.clear();
			mContext.mSceneList.addAll(mSceneFrag.getListOfScene());
			// mSceneFrag.updateSceneUI(mContext.mSceneList);
			break;
		case CommandID.NEW_SCENE:
			Log.i(TAG, "New scene");
			mContext.mProgDialog.dismiss();

			sceneName = new String(DataConversion.getBytesFromArray(0, 8,
					btMsg.getPayload()));
			Toast.makeText(mContext, "Add new scene successfully!",
					Toast.LENGTH_SHORT).show();

			mContext.mSceneList.clear();
			mContext.mSceneList.addAll(mSceneFrag.getListOfScene());
			// mSceneFrag.updateSceneUI(mContext.mSceneList);
		}
		// Clear buffer
		dataBuf.clear();

	}

	/**
	 * Get zone name from CC
	 * 
	 * @param payload
	 */
	private void getZoneName(byte[] payload) {
		int zoneID = payload[0];
		String zoneName = new String(DataConversion.getBytesFromArray(1, 16,
				payload));
		for (int i = 0; i < mContext.mZoneList.size(); i++) {
			if (mContext.mZoneList.get(i).getID() == zoneID) {
				mContext.mZoneList.get(i).setName(zoneName);
				break;
			}
		}

	}

	/**
	 * Send num of rule
	 * 
	 */
	private void sendNumOfRule(String sceneName) {
		Scene_c scene = getSceneWithName(sceneName);
		if (scene == null) {
			Log.i(TAG, "scene not exist");
			return;
		}
		BluetoothMessage mMsg = new BluetoothMessage();
		mMsg.setType(BTMessageType.BLE_DATA);
		mMsg.setIndex(mContext.mBTMsgIndex);
		mMsg.setLength((byte) 10);
		mMsg.setCmdIdH((byte) CommandID.SET);
		mMsg.setCmdIdL((byte) CommandID.NUM_OF_RULES);
		ByteBuffer payload = ByteBuffer.allocate(10);
		payload.put(scene.getName().getBytes());
		payload.putShort((short) scene.getNumOfRule());
		mMsg.setPayload(payload.array());
		payload.clear();
		putBLEMessage(mMsg);
	}

	private void sendRuleListToCC(String sceneName, short ruleIndex) {
		Scene_c scene = getSceneWithName(sceneName);
		if (scene == null) {
			Log.i(TAG, "scene not exist");
			return;
		}
		ArrayList<Rule_c> mRuleList = scene.getListOfRules();

		// scene all rule if index = 0xffff
		if (ruleIndex == -1) {
			for (int i = 0; i < scene.getNumOfRule(); i++) {
				// Log.i(TAG, "send rule with index " + i);
				Rule_c mRule = mRuleList.get(i);
				BluetoothMessage mMsg = new BluetoothMessage();
				mMsg.setType(BTMessageType.BLE_DATA);
				mMsg.setIndex((short) (mContext.mBTMsgIndex));
				mMsg.setLength((byte) 27);
				mMsg.setCmdIdH((byte) CommandID.SET);
				mMsg.setCmdIdL((byte) CommandID.RULE_WITH_INDEX);

				ByteBuffer payload = ByteBuffer.allocate(27);
				payload.put(scene.getName().getBytes());
				payload.putShort((short) i);
				payload.put(scene.getActivedInByte());
				payload.put((byte) mRule.getCond());
				if ((mRule.getCond() == ConditionDef.IN_RANGE)
						|| (mRule.getCond() == ConditionDef.IN_RANGE_EVDAY)) {
					payload.putInt(mRule.getStartDateTime());
					payload.putInt(mRule.getEndDateTime());
				} else {
					payload.putInt(mRule.getCondDevId());
					payload.putInt(mRule.getCondDevVal() << 16);
				}
				payload.put((byte) mRule.getAction());
				payload.putInt(mRule.getActDevId());
				payload.putShort((short) mRule.getActDevVal());

				mMsg.setPayload(payload.array());
				payload.clear();
				putBLEMessage(mMsg);
			}
		} else {
			Rule_c mRule = mRuleList.get(ruleIndex);
			BluetoothMessage mMsg = new BluetoothMessage();
			mMsg.setType(BTMessageType.BLE_DATA);
			mMsg.setIndex((short) (mContext.mBTMsgIndex));
			mMsg.setLength((byte) 30);
			mMsg.setCmdIdH((byte) CommandID.SET);
			mMsg.setCmdIdL((byte) CommandID.RULE_WITH_INDEX);

			ByteBuffer payload = ByteBuffer.allocate(27);
			payload.put(scene.getName().getBytes());
			payload.putShort((short) ruleIndex);
			payload.put(scene.getActivedInByte());
			payload.put((byte) mRule.getCond());
			if ((mRule.getCond() == ConditionDef.IN_RANGE)
					|| (mRule.getCond() == ConditionDef.IN_RANGE_EVDAY)) {
				payload.putInt(mRule.getStartDateTime());
				payload.putInt(mRule.getEndDateTime());
			} else {
				payload.putInt(mRule.getCondDevId());
				payload.putInt(mRule.getCondDevVal() << 16);
			}
			payload.put((byte) mRule.getAction());
			payload.putInt(mRule.getActDevId());
			payload.putShort((short) mRule.getActDevVal());

			mMsg.setPayload(payload.array());
			payload.clear();

			putBLEMessage(mMsg);
		}
	}

	/**
	 * Get rule list
	 * 
	 */

	private void getRuleList(ByteBuffer dataBuf) {
		String sceneName = new String(DataConversion.getBytesFromArray(0, 8,
				dataBuf.array()));
		int ruleIndex = dataBuf.getInt(8);
		byte condition = dataBuf.get(11);
		int timeStart = dataBuf.getInt(12);
		int timeStop = dataBuf.getInt(16);
		int condDevID = dataBuf.getInt(12);
		short condDevVal = dataBuf.getShort(16);
		byte action = dataBuf.get(20);
		int actDevID = dataBuf.getInt(21);
		short actDevVal = dataBuf.getShort(25);

		for (int i = 0; i < mContext.mSceneList.size(); i++) {
			if (sceneName.matches(mContext.mSceneList.get(i).getName())) {
				Rule_c mNewRule = new Rule_c();
				mNewRule.setRuleIndex(ruleIndex);
				mNewRule.setCond(condition);
				mNewRule.setCondDevId(condDevID);
				mNewRule.setCondDevVal(condDevVal);
				mNewRule.setAction(action);
				mNewRule.setStartDateTime(timeStart);
				mNewRule.setEndDateTime(timeStop);
				mNewRule.setActDevId(actDevID);
				mNewRule.setActDevVal(actDevVal);
				mContext.mSceneList.get(i).addRule(mNewRule);
				break;
			}
		}

	}

	/**
	 * Get list of scenes
	 * 
	 * @param dataBuf
	 * @param isActived
	 */
	private void getSceneList(byte[] dataBuf, boolean isActived) {
		int numOfScene = dataBuf.length / 9;
		String sceneName = "";
		int sceneIndex;
		for (int i = 0; i < numOfScene; i += 9) {
			sceneIndex = dataBuf[i];
			sceneName = new String(DataConversion.getBytesFromArray(1, 8,
					dataBuf));
			Scene_c newSceneObj = new Scene_c();
			newSceneObj.setIndex(sceneIndex);
			newSceneObj.setName(sceneName);
			// Log.i(TAG, sceneName);
			newSceneObj.setActived(isActived);
			mContext.mSceneList.add(newSceneObj);
		}

	}

	/**
	 * Get devList
	 * 
	 */
	public ArrayList<DeviceInfo> getDeviceList(ByteBuffer dataBuf) {
		ArrayList<DeviceInfo> mDevList = null;
		int devIndex;
		int devID;
		short devVal;
		for (int i = 0; i < dataBuf.capacity() / 10; i += 10) {
			devIndex = dataBuf.getInt(i);
			devID = dataBuf.getInt(i + 4);
			devVal = dataBuf.getShort(i + 8);
			DeviceInfo newDev = new DeviceInfo();
			newDev.setDevIdx(devIndex);
			newDev.setDevID(devID);
			newDev.setDevVal(devVal);
			if (!isDeviceExist(mContext.mDevInfoList, devID)) {
				mContext.mDevInfoList.add(newDev);
			}

		}
		return mDevList;

	}

	/**
	 * Search device exist in device List by ID
	 * 
	 * @param devList
	 * @param devID
	 * @return
	 */
	private boolean isDeviceExist(ArrayList<DeviceInfo> devList, int devID) {
		for (int i = 0; i < devList.size(); i++) {
			if (devList.get(i).getDevID() == devID) {
				return true;
			}
		}
		return false;
	}

	private Scene_c getSceneWithName(String sceneName) {
		ScenesFragment mSceneFrag = (ScenesFragment) mContext
				.getSupportFragmentManager().getFragments().get(1);
		ArrayList<Scene_c> listOfScene = new ArrayList<Scene_c>();
		listOfScene.addAll(mSceneFrag.getListOfScene());
		for (int i = 0; i < listOfScene.size(); i++) {
			Log.i(TAG, "scene get: " + i);
			if (listOfScene.get(i).getName().matches(sceneName)) {
				return listOfScene.get(i);
			}
		}
		return null;
	}

	/**
	 * Update device value from CC
	 * 
	 * @param payLoad
	 */
	private void updateDeviceValue(byte[] payLoad) {
		// int mDevID = DataConversion.byteArr2Int(payLoad);
		// short mDevVal = DataConversion.byteArr2Short(payLoad);
		// get device value version 2
		ByteBuffer dataBuf = ByteBuffer.allocate(payLoad.length);
		dataBuf.put(payLoad);
		final int mDevID = dataBuf.getInt(0);
		final short mDevVal = dataBuf.getShort(4);
		dataBuf.clear();

		for (int i = 0; i < mContext.mDevInfoList.size(); i++) {
			if (mContext.mDevInfoList.get(i).getDevID() == mDevID) {
				mContext.mDevInfoList.get(i).setDevVal(mDevVal);
			}
		}
		// if device is not exist in current list, add device to list
		if (!isDeviceExist(mContext.mDevInfoList, mDevID)) {
			DeviceInfo mDevInfo = new DeviceInfo(mContext.mDevInfoList.size(),
					mDevID, mDevVal);
			mContext.mDevInfoList.add(mDevInfo);
			mDevInfo = null;
		}

		mContext.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				DeviceControlFragment mDeviceFrag = (DeviceControlFragment) mContext
						.getSupportFragmentManager().getFragments().get(0);
				// mDeviceFrag.updateUI(mContext.mDevInfoList);
				updateDevValInZone(mDevID, mDevVal);
				mDeviceFrag.updateUI(mContext.mZoneList);
			}
		});
	}

	/**
	 * update device value in zone list
	 * 
	 * @param devID
	 * @param devVal
	 */
	private void updateDevValInZone(int devID, short devVal) {
		for (int i = 0; i < mContext.mZoneList.size(); i++) {
			for (int j = 0; j < mContext.mZoneList.get(i).getChildCount(); j++) {
				if (mContext.mZoneList.get(i).getChildIndex(j).getID() == devID) {
					mContext.mZoneList.get(i).getChildIndex(j).setVal(devVal);
					return;
				}
			}
		}
		if (!isDeviceExistInZone((byte) devID >> 24, devID)) {
			for (int i = 0; i < mContext.mZoneList.size(); i++) {
				if (mContext.mZoneList.get(i).getID() == (devID >> 24)) {
					Device_c mNewDev = new Device_c();
					mNewDev.setID(devID);
					mNewDev.setName(devID);
					mNewDev.setVal(devVal);
					mContext.mZoneList.get(i).addChildListItem(mNewDev);
					break;
				}
			}
		}

	}

	/**
	 * Determine if device exist in zone or not
	 * 
	 * @param zoneID
	 * @param devID
	 * @return
	 */
	private boolean isDeviceExistInZone(int zoneID, int devID) {
		for (int i = 0; i < mContext.mZoneList.size(); i++) {
			if (mContext.mZoneList.get(i).getID() == zoneID) {
				for (int j = 0; j < mContext.mZoneList.get(i).getChildCount(); j++) {
					if (mContext.mZoneList.get(i).getChildIndex(j).getID() == devID) {
						return true;
					}
				}
				break;
			}
		}
		return false;
	}
}

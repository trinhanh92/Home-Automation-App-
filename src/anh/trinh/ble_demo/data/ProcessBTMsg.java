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
import java.util.Arrays;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Message;
import android.util.Log;
import anh.trinh.ble_demo.BluetoothLeService;
import anh.trinh.ble_demo.DeviceControlFragment;
import anh.trinh.ble_demo.HomeActivity;
import anh.trinh.ble_demo.R;
import anh.trinh.ble_demo.list_view.Device_c;

public class ProcessBTMsg {
	private HomeActivity mContext;
	private final static String TAG = "ProcessMessage";

	public ProcessBTMsg(HomeActivity mContext) {
		this.mContext = mContext;
	}

	/**
	 * Get BLE device data
	 * 
	 * @param intent
	 * @return
	 */
	public BluetoothMessage getBLEMessage(Intent intent) {
		byte[] recBuf = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
		BluetoothMessage msg = parseBTMessage(recBuf);
		return msg;

	}

	/**
	 * Send data to BLE device
	 * 
	 * @param characteristic
	 * @param msg
	 * @return
	 */
	public void putBLEMessage(BluetoothGattCharacteristic characteristic,
			BluetoothMessage msg) throws InterruptedException {
		ByteBuffer sendBuf = ByteBuffer.allocate(msg.getLength() + 6);
		sendBuf.put(msg.getType());
		sendBuf.put(DataConversion.short2ByteArr(msg.getIndex()));
		sendBuf.put(msg.getLength());
		sendBuf.put(msg.getCmdIdH());
		sendBuf.put(msg.getCmdIdL());
		if (msg.getLength() != 0) {
			sendBuf.put(msg.getPayload());
		}
		characteristic.setValue(sendBuf.array());
		
		System.out.println("data send to BLE");
		for(int i = 0; i < msg.getLength()+6; i++){
			System.out.printf("%d ", sendBuf.array()[i]);
		}
		System.out.println();
		sendBuf.clear();
		synchronized (mContext.mWriteSuccess) {
			mContext.mWriteSuccess.wait(100);
		}
		mContext.mBluetoothLeService.writeCharacteristic(characteristic);
		mContext.mBTMsgIndex++;
	}

	/**
	 * Send ACK back to BLE device
	 * 
	 * @param characteristic
	 * @param msg
	 */
	public void sendBLEMessageACK(BluetoothGattCharacteristic characteristic,
			short msgIndex) {
		ByteBuffer sendBuf = ByteBuffer.allocate(4);
		sendBuf.put(BTMessageType.BLE_ACK);
		sendBuf.put(DataConversion.short2ByteArr(msgIndex));
		sendBuf.put((byte) 0);
		characteristic.setValue(sendBuf.array());
		
		System.out.println("data ACK to BLE");
		for(int i = 0; i < sendBuf.array().length; i++){
			System.out.printf("%d ", sendBuf.array()[i]);
		}
		System.out.println();
		
		sendBuf.clear();
		mContext.mBluetoothLeService.writeCharacteristic(characteristic);
	}

	/**
	 * Parse Data Buffer receive to Bluetooth Message
	 * 
	 * @param message
	 * @return
	 */
	public BluetoothMessage parseBTMessage(byte[] msgBuf) {
		BluetoothMessage BTMsg;

		for(int i = 0; i < msgBuf.length; i++){
			System.out.printf("%d ",msgBuf[i]);
		}
		System.out.println();
		
		byte msgType = msgBuf[0];
		short msgIndex = DataConversion.byteArr2Short(msgBuf[1], msgBuf[2]);
		if (msgType == BTMessageType.BLE_ACK) {
			byte msgLen = msgBuf[3];
			BTMsg = new BluetoothMessage();
			BTMsg.setType(msgType);
			BTMsg.setIndex(msgIndex);
			BTMsg.setLength(msgLen);

		} else {
			sendBLEMessageACK(mContext.mWriteCharacteristic, msgIndex);
			byte msgLen = msgBuf[3];
			byte cmdIdH = msgBuf[4];
			byte cmdIdL = msgBuf[5];
			byte[] payload = new byte[msgLen];
			for (int i = 0; i < msgLen; i++) {
				payload[i] = msgBuf[i + 6];
			}

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
	public void processBTMessageQueue(ArrayList<BluetoothMessage> msgQueue) {

		int len = 0;
		Message handlerMsg;
		// Get length of payload
		
		for (BluetoothMessage msg : msgQueue) {
			len += msg.getLength();
			if(msg.getType() == BTMessageType.BLE_ACK){
				return;
			}
		}
		ByteBuffer dataBuf = ByteBuffer.allocate(len);

		// Get data of payload
		for (BluetoothMessage msg : msgQueue) {
			dataBuf.put(msg.getPayload());
		}

		// Analyze kind of massage
		switch (msgQueue.get(0).getCmdIdL()) {

		case CommandID.NUM_OF_DEVS:
			Log.i(TAG, "receive num of dev");
			mContext.mNumOfDev = dataBuf.get(0);
			handlerMsg = mContext.mMsgHandler
					.obtainMessage(CommandID.NUM_OF_DEVS);
			mContext.mMsgHandler.sendMessage(handlerMsg);
			break;
		case CommandID.DEV_WITH_INDEX:
			// mContext.mDevInfoList = getDeviceList(dataBuf);
			Log.i(TAG, "dev with index");
			getDeviceList(dataBuf);
			break;
		case CommandID.DEV_VAL:
			// update device value from CC
			updateDeviceValue(msgQueue.get(0).getPayload());
			break;

		default:
			break;
		}
		// Clear buffer
		dataBuf.clear();

	}

	/**
	 * Get List of Devices from payload of Message queue
	 * 
	 * @param dataBuf
	 * @return mListOfDev
	 */
	public ArrayList<DeviceInfo> getDeviceList(ByteBuffer dataBuf) {
		ArrayList<DeviceInfo> mListOfDev = new ArrayList<DeviceInfo>();
		DeviceInfo mDevice = null;
		ByteBuffer devIdx = null;
		ByteBuffer devID = null;
		ByteBuffer devVal = null;
		// Parse payload content device information to List of device object
		// for(int i = 0; i < 40; i++){
		for (int i = 0; i < dataBuf.array().length; i++) {
			System.out.println(dataBuf.array()[i]);
			switch (i % 10) {
			case 0:
				mDevice = new DeviceInfo();
				devIdx = ByteBuffer.allocate(4);
				devIdx.put(dataBuf.get(i));
				break;
			case 1:
			case 2:
				devIdx.put(dataBuf.get(i));
				break;
			case 3:
				devIdx.put(dataBuf.get(i));
				mDevice.setDevIdx(devIdx.getInt(0));
				devIdx.clear();
				break;
			case 4:
				devID = ByteBuffer.allocate(4);
				devID.put(dataBuf.get(i));
				break;
			case 5:
			case 6:
				devID.put(dataBuf.get(i));
				break;
			case 7:
				devID.put(dataBuf.get(i));
				mDevice.setDevID(devID.getInt(0));
				devID.clear();
				break;
			case 8:
				devVal = ByteBuffer.allocate(2);
				devVal.put(dataBuf.get(i));
				break;
			case 9:
				devVal.put(dataBuf.get(i));
				mDevice.setDevVal(devVal.getShort(0));
				devVal.clear();
				if (false == isDeviceExist(mContext.mDevInfoList,
						mDevice.getDevID())) {
					mContext.mDevInfoList.add(mDevice);
				}
				mDevice = null;
				break;
			default:
				Log.i("DataInfo", "error");
				break;
			}
		}
		return mListOfDev;
	}

	/**
	 * Create Message for testing in Mobile
	 * 
	 */
	public ArrayList<BluetoothMessage> createMessageQueue() {
		ArrayList<BluetoothMessage> mMsgList = new ArrayList<BluetoothMessage>();
		BluetoothMessage mBLEMsg;
		byte[] msgArray = new byte[43];

		msgArray[0] = 40;
		msgArray[1] = CommandID.SET;
		msgArray[2] = CommandID.NUM_OF_DEVS;

		// LEVEL BULB
		msgArray[3] = 0x00; // device index = 0
		msgArray[4] = 0x00;
		msgArray[5] = 0x00;
		msgArray[6] = 0x00;

		msgArray[7] = 0x00; // zone ID = 0
		msgArray[8] = 0x00; // node ID = 0
		msgArray[9] = 0x00; // endpoint ID = 0
		msgArray[10] = DeviceTypeDef.LEVEL_BULB; // Device ID = LEVEL BULB
		msgArray[11] = 0x02; // Device Value;
		msgArray[12] = 0x00; //

		// DIMMER
		msgArray[13] = 0x00; // device index = 1
		msgArray[14] = 0x00;
		msgArray[15] = 0x00;
		msgArray[16] = 0x01;

		msgArray[17] = 0x01; // zone ID = 1
		msgArray[18] = 0x00; // node ID = 0
		msgArray[19] = 0x00; // endpoint ID = 0
		msgArray[20] = DeviceTypeDef.DIMMER; // Device ID = DIMMER
		msgArray[21] = 0x02;
		msgArray[22] = 0x00;

		// TEMP SENSOR
		msgArray[23] = 0x00; // device index = 2
		msgArray[24] = 0x00;
		msgArray[25] = 0x00;
		msgArray[26] = 0x02;

		msgArray[27] = 0x00; // zone ID = 0
		msgArray[28] = 0x00; // node ID = 0
		msgArray[29] = 0x00; // endpoint ID = 0
		msgArray[30] = DeviceTypeDef.PIR_SENSOR; // Device ID = TEMP SENSOR
		msgArray[31] = 0x20; // Device Value
		msgArray[32] = 0x13;

		// BUTTON
		msgArray[33] = 0x00; // device index = 3
		msgArray[34] = 0x00;
		msgArray[35] = 0x00;
		msgArray[36] = 0x03;

		msgArray[37] = 0x01; // zone ID = 1
		msgArray[38] = 0x00; // node ID = 0
		msgArray[39] = 0x00; // endpoint ID = 0
		msgArray[40] = DeviceTypeDef.BUTTON; // Device ID = BUTTON
		msgArray[41] = 0x00; // Device Value
		msgArray[42] = 0x01;

		mBLEMsg = parseBTMessage(msgArray);
		mMsgList.add(mBLEMsg);

		return mMsgList;

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

	/**
	 * Update device value from CC
	 * 
	 * @param payLoad
	 */
	private void updateDeviceValue(byte[] payLoad) {
		int mDevID = DataConversion.byteArr2Int(payLoad);
		short mDevVal = DataConversion.byteArr2Short(payLoad);

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
				mDeviceFrag.updateUI(mContext.mDevInfoList);
			}
		});
	}
}

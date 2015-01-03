package anh.trinh.ble_demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import anh.trinh.ble_demo.data.BTMessageType;
import anh.trinh.ble_demo.data.BluetoothMessage;

/**
 * a worker Thread with task: send message to BLE device
 * 
 */
public class BleThread extends Thread {
	public Handler mHandler;
	private HomeActivity mContext;

	public BleThread(HomeActivity mContext) {
		this.mContext = mContext;
	}

	@Override
	public void run() {
		Looper.prepare();
		mHandler = new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				Bundle mBundle = (Bundle) msg.obj;
				byte[] sendBuf = mBundle.getByteArray("BLE_MSG_SEND");
				switch (msg.what) {
				case BTMessageType.BLE_READ:
					BluetoothMessage btMsg = (BluetoothMessage) msg.obj;
					mContext.mProcessMsg.processBTMessage(btMsg);
					break;

				case BTMessageType.BLE_WRITE_DATA:
					System.out.printf("data send to BLE: ");
					for (int i = 0; i < sendBuf.length; i++) {
						System.out.printf("%d ", sendBuf[i]);
					}
					System.out.println();
					mContext.mWriteCharacteristic.get(0).setValue(sendBuf);
					mContext.mBluetoothLeService
							.writeCharacteristic(mContext.mWriteCharacteristic.get(0));
					mContext.mBLEThreadSignal.waitSignal();
					break;
					
				case BTMessageType.BLE_WRITE_ACK:
					System.out.println("ack send to BLE");
					mContext.mWriteCharacteristic.get(1).setValue(sendBuf);
					mContext.mBluetoothLeService
							.writeCharacteristic(mContext.mWriteCharacteristic.get(1));
					mContext.mBLEThreadSignal.waitSignal();
					break;
				default:
					Log.e("BLE Message:", "BLE message type invalid!");
					break;
				}
//				try {
//					sleep(1000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				mContext.mWriteBLESuccess = false;
////				mContext.ble_timeout_send(400);
//				while(true){
//					if(mContext.mWriteBLESuccess){
//						break;
//					}
//				}
				return false;
			}
		});
		Looper.loop();
	}

}
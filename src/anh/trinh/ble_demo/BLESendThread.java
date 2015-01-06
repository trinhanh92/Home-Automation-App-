package anh.trinh.ble_demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import anh.trinh.ble_demo.data.BTMessageType;
import anh.trinh.ble_demo.data.BluetoothMessage;

/**
 * worker Thread with task: send message to BLE device
 * 
 */
public class BLESendThread extends HandlerThread {
//	private final String TAG = "ble thread";
	private Handler mHandler;
	private HomeActivity mContext;

	public BLESendThread() {
		super("BLESendThread", Process.THREAD_PRIORITY_BACKGROUND - 1);
	}
	
	public void bleThreadInit(HomeActivity mContext) {
		this.mContext = mContext;
	}
	
	@Override
	protected void onLooperPrepared() {
		super.onLooperPrepared();
		mHandler = new Handler(getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Bundle mBundle = (Bundle) msg.obj;
				byte[] sendBuf = mBundle.getByteArray("BLE_MSG_SEND");
				switch (msg.what) {
				case BTMessageType.BLE_READ:
					Intent intent = (Intent) msg.obj;
					BluetoothMessage btMsg = mContext.mProcessMsg.getBLEMessage(intent); 
					mContext.mProcessMsg.processBTMessage(btMsg);
					break;

				case BTMessageType.BLE_WRITE_DATA:
//					Log.i(TAG, "BLE send DATA: ");
					System.out.printf("BLE send DATA: ");
					for (int i = 0; i < sendBuf.length; i++) {
						System.out.printf("%d ", sendBuf[i]);
					}
					System.out.println();
					mContext.mWriteCharacteristic.get(0).setValue(sendBuf);
					mContext.mBluetoothLeService
							.writeCharacteristic(mContext.mWriteCharacteristic
									.get(0));
					try {
						sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// mContext.mBLEThreadSignal.waitSignal(600);
					break;

				case BTMessageType.BLE_WRITE_ACK:
//					Log.i(TAG, "BLE send ACK: ");
					System.out.printf("BLE send ACK: ");
					for (int i = 0; i < sendBuf.length; i++) {
						System.out.printf("%d ", sendBuf[i]);
					}
					System.out.println();

					mContext.mWriteCharacteristic.get(1).setValue(sendBuf);
					mContext.mBluetoothLeService
							.writeCharacteristic(mContext.mWriteCharacteristic
									.get(1));
//					try {
//						sleep(100);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
					mContext.mBLEThreadSignal.waitSignal(100);
					break;
				default:
					Log.e("BLE Message:", "BLE message type invalid!");
					break;
				}
			}
		};
	}

	public void sendMessage(int what, Object mObject) {
		Message msg = mHandler.obtainMessage(what, mObject);
		mHandler.sendMessage(msg);
	}
}
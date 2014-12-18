package anh.trinh.ble_demo.thread_sync;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Message;
import android.util.Log;
import anh.trinh.ble_demo.HomeActivity;

public class BLEWriteThread extends Thread{

	private final int QUEUE_SIZE = 10;
	private HomeActivity mContext;
//	private BlockingQueue<Message> mMsgQueue = new LinkedBlockingQueue<Message>(QUEUE_SIZE);
	private BluetoothGattCharacteristic mCharacteristic;
	public BLEWriteThread(HomeActivity mContext) {
		this.mContext = mContext;
	}
	
	@Override
	public void run() {
		Message msg;
		super.run();
		
		while(true){
//			while((msg = mContext.mBLEMessageQueue.poll()) != null){
//				try {
//					msg = mContext.mBLEMessageQueue.take();
//				} catch (InterruptedException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				mCharacteristic = (BluetoothGattCharacteristic)msg.obj;
//				
//				if (msg.what == 0) {
//					mContext.mBTMsgIndex++;
//					synchronized (mContext.mWriteSuccess) {
//						try {
//							mContext.mWriteSuccess.wait(100);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				}
//				Log.i("BLE thread", "need to write");
//				mContext.mBluetoothLeService.writeCharacteristic(mCharacteristic);
//			}
		}
	}
	

}

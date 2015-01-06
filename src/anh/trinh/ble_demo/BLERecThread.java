package anh.trinh.ble_demo;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import anh.trinh.ble_demo.data.BluetoothMessage;

/**
 * worker Thread with task: send message to BLE device
 * 
 */
public class BLERecThread extends HandlerThread {
//	private final String TAG = "ble thread";
	private Handler mHandler;
	private HomeActivity mContext;

	public BLERecThread() {
		super("BLESendThread", Process.THREAD_PRIORITY_BACKGROUND);
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
				Intent intent = (Intent) msg.obj;
				BluetoothMessage btMsg = mContext.mProcessMsg.getBLEMessage(intent); 
				mContext.mProcessMsg.processBTMessage(btMsg);
			}
		};
	}

	public void sendMessage(int what, Object mObject) {
		Message msg = mHandler.obtainMessage(what, mObject);
		mHandler.sendMessage(msg);
	}
}
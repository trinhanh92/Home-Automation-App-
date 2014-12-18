package anh.trinh.ble_demo.thread_sync;

import android.util.Log;

public class ACKisReceived {
	private MonitorObject mMonitorObj = new MonitorObject();
	private boolean wasSignaled = true;

	public void isAckReceived() {
		// TODO Auto-generated constructor stub
		synchronized (mMonitorObj) {
			if (!wasSignaled) {
				try {
					mMonitorObj.wait(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				wasSignaled = false;
			}
		}
	}

	public void setHasACK() {
		synchronized (mMonitorObj) {
			wasSignaled = true;
			mMonitorObj.notify();
			Log.i("ACK", "notify");
		}
	}

}

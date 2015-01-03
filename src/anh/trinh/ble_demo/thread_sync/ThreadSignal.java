package anh.trinh.ble_demo.thread_sync;

public class ThreadSignal {
	private volatile boolean hasSignaled = false;
	private Object mLockOn = new Object();

	public void waitSignal() {
		synchronized (mLockOn) {
			if (!hasSignaled) {
				try {
					mLockOn.wait(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
//			hasSignaled = false;
			System.out.println("lock on");
		}
	}

	public void sendSignal() {
		synchronized (mLockOn) {
			hasSignaled = true;
			mLockOn.notifyAll();
		}
	}

}

package at.schoenlaub.distributed.dts.timeclient;

import java.io.Closeable;
import java.io.IOException;
import java.util.TimerTask;

public class CloseableCloserTimerTask extends TimerTask{
	private Closeable cl;
	
	public CloseableCloserTimerTask(Closeable closeable)
	{
		this.cl = closeable;
	}
	@Override
	public void run() {
		try {
			System.out.println("Timer fired! About to close Socket!");
			this.cl.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

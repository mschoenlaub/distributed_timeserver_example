package at.schoenlaub.distributed.dts.timeserver;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;



public class UnicastSenderThread extends Thread{
	private DatagramPacket p;
	public UnicastSenderThread(DatagramPacket p)
	{
		this.p = p;
	}
	public void run()
	{
		DatagramSocket sock;
		try {
			sock = new DatagramSocket();
			sock.send(p);
			System.out.println("Sending response to " + p.getAddress() + ":"+ p.getPort());
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

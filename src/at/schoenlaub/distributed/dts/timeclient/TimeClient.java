package at.schoenlaub.distributed.dts.timeclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;

import at.schoenlaub.distributed.dts.common.*;


public class TimeClient extends Thread{
	
	private static void printUsage()
	{
		System.out.println("Usage TimeClient <MulticastIP> <MulticastPort");
	}
	public static void main(String[] args) {
		if (args.length != 2)
		{
			printUsage();
		}
		else
		{
			try {
				int port = Integer.parseInt(args[1]);
				TimeClient c = new TimeClient(args[0], port);
				System.out.println("Starting Client with MulticastAddress "+ args[0] +":"+port);
				c.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			catch(NumberFormatException ex){
					printUsage();
			}
		}
	}
	
	private PacketFactory packetFactory;
	private MulticastSocket multiSock;
	private DatagramSocket uniSock;
	private InetAddress addr;
	private int multicastPort;
	
	public TimeClient(String multiCastAddress, int multicastPort) throws IOException
	{
		addr = InetAddress.getByName(multiCastAddress);
		this.multicastPort = multicastPort;
		multiSock = new MulticastSocket();
		uniSock = new DatagramSocket();
		packetFactory = new PacketFactory();
	}
	public void run()
	{
		Dictionary<String, Long> answerDictionary;
		Timer t = new Timer();
		byte[] ansBuff = new byte[255];
		DatagramPacket p = new DatagramPacket(ansBuff, ansBuff.length);

		while (true)
		{
			answerDictionary = new Hashtable<String,Long>();
			try {
				Thread.sleep(5*1000);
			} catch (InterruptedException e1) {
				System.exit(0);
			}			
			try {
				System.out.println("Creating UnicastSocket...");
				uniSock = new DatagramSocket();
				DatagramPacket req = packetFactory.createRequest(addr,multicastPort,uniSock.getLocalPort());
				System.out.println("Sending request to "+ req.getAddress()+":"+req.getPort());
				multiSock.send(req);
			} catch (IOException e) {
				System.err.println("IOException occurred when trying to send.");
				continue;
			}
			System.out.println("Scheduled 1000ms Timer");
			t.schedule(new CloseableCloserTimerTask(uniSock), 1000); //Close the socket in 1000ms
			
			while (!uniSock.isClosed())
			{				
				try {
					uniSock.receive(p);
					ByteArrayInputStream bais = new ByteArrayInputStream(p.getData());
					InputStreamReader isr = new InputStreamReader(bais,Charset.forName("UTF-8"));
					StringBuilder identifier = new StringBuilder();
					StringBuilder time = new StringBuilder();
					for (int c = -1;(char)c!='#';c= isr.read())
					{
						if (c > -1)
						identifier.append((char)c);		
					}
					for (int c = -1;(char)c!='#';c= isr.read())
					{
						if (c > -1)
						time.append((char)c);
					}		
					long timeStamp = Long.parseLong(time.toString());
					answerDictionary.put(identifier.toString(),timeStamp);
					System.out.println("Received from " + identifier.toString() + ": " + new Date(timeStamp));
				} catch (IOException e) {
					if (uniSock.isClosed())
						System.out.println("Socket is closed!");
					else
						System.err.println("Something went wrong when reading from UnicastSocket!");
					break;
				}
			}
			long ts=getAvgTimeStamp(answerDictionary.elements());
			if (ts > 0)
			{
				Date avgTime = new Date(ts);
				System.out.println("Average Time is " + avgTime.toString());
			}
			else
			{
				System.out.println("No server answered in time!");
			}
		}

	}
	public void finalize()
	{
		if (!multiSock.isClosed())
			multiSock.close();
		if (!uniSock.isClosed())
			uniSock.close();
	}	
	
	private long getAvgTimeStamp(Enumeration<Long> enumeration)
	{
		long total = 0;
		int count = 0;
		while (enumeration.hasMoreElements())
		{
			long val = enumeration.nextElement();
			total += val;
			count++;
		}
		if (count == 0)
			return 0;
		
		return total/count;
	}
}

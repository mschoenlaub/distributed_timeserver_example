package at.schoenlaub.distributed.dts.timeserver;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import at.schoenlaub.distributed.dts.common.PacketFactory;


public class TimeServer extends Thread{
	
	private static void printUsage()
	{
		System.out.println("Usage TimeServer <Identifier> <MulticastIP>:<MulticastPort>");
	}
	public static void main(String[] args) {
		if (args.length != 3)
			printUsage();
		else
		{
			try {
				int port = Integer.parseInt(args[2]);
				System.out.println("Starting Server " +args[0]+ " with MulticastAddress "+ args[1] +":"+port);
				TimeServer s = new TimeServer(args[0], args[1],port);
				s.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			catch (NumberFormatException ex){
				printUsage();
			}
		}
	}
	
	private MulticastSocket multiSock;
	private String identifier;
	private InetAddress addr;
	private PacketFactory packetFactory;
	
	public TimeServer(String identifier, String multicastAddress, int multicastPort) throws IOException
	{
		addr = InetAddress.getByName(multicastAddress);
		multiSock = new MulticastSocket(multicastPort);
		multiSock.joinGroup(addr);
		packetFactory = new PacketFactory();	
		this.identifier = identifier;
	}
	public void run()
	{
		byte[] buf = new byte[255];
		DatagramPacket req = new DatagramPacket(buf, buf.length);
		while (true)
		{
			try {
				System.out.println("Waiting for request...");
				multiSock.receive(req);
				System.out.println("Got request from " + req.getAddress());
			} catch (IOException e) {
				continue;
			}
			System.out.println("Creating response...");
			DatagramPacket resp = packetFactory.createResponse(identifier, req);
			UnicastSenderThread sender = new UnicastSenderThread(resp);
			System.out.println("Starting UnicastSender...");
			sender.start();
		}
	}
	
	public void finalize()
	{
		multiSock.close();
	}
	
	public String getIdentifier() {
		return identifier;
	}
}

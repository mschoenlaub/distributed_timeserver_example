package at.schoenlaub.distributed.dts.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Date;

public class PacketFactory {
	private static PacketFactory fac;
	
	public static PacketFactory getInstance()
	{
		if (fac == null)
			fac = new PacketFactory();
		return fac;
	}
	public DatagramPacket createRequest(InetAddress address, int multicastPort, int unicastPort)
	{
		byte[] buf = ("REQ#"+unicastPort+"#").getBytes(Charset.forName("UTF-8"));
		System.out.println("Creating request "+ "REQ#"+unicastPort+"#");
		DatagramPacket packet = new DatagramPacket(buf, buf.length,address,multicastPort);
		return packet;
	}
	
	public DatagramPacket createResponse(String identifier, DatagramPacket request)
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(request.getData());
		InputStreamReader isr = new InputStreamReader(bais,Charset.forName("UTF-8"));
		
		StringBuilder cmd = new StringBuilder();
		StringBuilder port = new StringBuilder();
				
		try {
			for (int c = -1;(char)c!='#';c= isr.read())
			{
				if (c > -1)
				cmd.append((char)c);	
			}
			for (int c = -1;(char)c!='#';c=isr.read())
			{
				if (c >-1)
				port.append((char)c);
			}
		} catch (IOException io)
		{
			io.printStackTrace();
		}
		int iport = Integer.parseInt(port.toString());
		System.out.println("Parsed Request => CMD: " + cmd.toString() + " Port: " + port);
		Date d = new Date();
		byte[] buffer = (identifier+"#"+d.getTime()+"#").getBytes(Charset.forName("UTF-8"));
		System.out.println("Time is " + d);
		DatagramPacket p = new DatagramPacket(buffer, buffer.length,request.getAddress(),iport);
		return p;
	}
}

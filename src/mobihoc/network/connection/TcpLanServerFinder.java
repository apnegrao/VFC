package mobihoc.network.connection;

import java.util.*;
import java.net.*;

import mobihoc.network.InfoItem;
import mobihoc.exception.InfoItemResultException;
import mobihoc.network.client.*;

/** Classe TcpLanServerFinder.
 * Permite enviar pacotes UDP pela LAN à procura de servidores e conectar-se a um deles.
 **/
public class TcpLanServerFinder extends ServerFinder {

	public TcpLanServerFinder(IServerFinderListener sfl) {
		super(sfl);
	}
	
	public void run()  {
		DatagramSocket probe;
		try {
			probe = new DatagramSocket(8118);
		} catch (SocketException e) {
			System.out.println("TcpLanServerFinder :: Problemas a criar o socket. MSG: " + e);
			return;
		}
		byte[] outData = new String("Mobihoc::UdpLanProbe").getBytes();
		DatagramPacket outPacket;
		try {
			outPacket = new DatagramPacket(outData, outData.length, InetAddress.getByName("255.255.255.255"), mobihoc.network.connection.TcpLanConnectionInfo.udpProbePort);
		} catch (UnknownHostException e) {
			System.out.println("TcpLanServerFinder :: Problemas com o host. MSG: " + e);
			return;
		}
		try {
			probe.send(outPacket);
		} catch(java.io.IOException e) {
			System.out.println("TcpLanServerFinder :: Problemas na conecção a enviar. MSG: " + e);
		}
		
		byte[] inData = new byte[1024];
		DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
		try {
			probe.setSoTimeout(5000);
		} catch (SocketException e) {
			System.out.println("TcpLanServerFinder :: Problemas com o timer." + e);
		}
		List<ServerRecord> result = new ArrayList<ServerRecord>();
		while (true) {
			try {
				probe.receive(inPacket);
			} catch (SocketTimeoutException e) {
				break;
			} catch (java.io.IOException e) {
				System.out.println("TcpLanServerFinder :: Problemas a receber." + e);
			}
			String hostname = new String(inPacket.getAddress().getHostAddress());
			String data = new String(inPacket.getData());
			data = data.substring(data.indexOf("<port>") + "<port>".length(), data.indexOf("</port>"));
			System.out.println("Found Server: " + hostname + ":" + data);
			int port = new Integer(data).intValue();
			result.add(new TcpServerRecord(hostname, port, false));
		}
		probe.close();
		sfl.callbackSearchResults(result);
	}
	
	public void requestedInfoFilled() throws InfoItemResultException { }

	public boolean isInfoNeeded() {
		return false;
	}

}

package mobihoc.network.connection;

import java.util.*;

import java.lang.Thread;
import java.net.*;

/** Classe UpdDiscoveryThread.
 * Responsável por responder a pedidos de descoberta de clientes.
 **/
public class UdpDiscoveryThread extends Thread {

	private int _port;
	//private String _hostname;
	
	public UdpDiscoveryThread(int port) {
		_port = port;
	}
	
	public void run() {
		DatagramSocket listener;
		byte[] inData = new byte[1024];
		String reply = "<port>" + _port + "</port>"; 
		byte[] outData = reply.getBytes();
		try {
			listener = new DatagramSocket(mobihoc.network.connection.TcpLanConnectionInfo.udpProbePort);
			listener.setBroadcast(true);
			
		} catch (java.net.SocketException e) {
			System.out.println("UdpDiscovery :: Problemas a criar o socket. Vou sair");
			return;
		}
		DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
		System.out.println("UdpDiscovery :: Running");
		while(true) {
			try {
				listener.receive(inPacket);
				System.out.println("HERE");
			} catch(java.io.IOException e) {
				System.out.println("UdpDiscovery :: Problemas na conecção a receber. Vou continuar.");
				continue;
			}
			try {
				DatagramPacket outPacket = new DatagramPacket(outData, outData.length, inPacket.getAddress(), inPacket.getPort());
				listener.send(outPacket);
			} catch(java.io.IOException e) {
				System.out.println("UdpDiscovery :: Problemas na conecção a enviar. Vou continuar.");
			}
		}
	}
}

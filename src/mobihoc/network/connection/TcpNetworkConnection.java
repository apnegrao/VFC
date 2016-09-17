package mobihoc.network.connection;

import java.io.*;
import java.net.*;

import mobihoc.Mobihoc;

import mobihoc.network.*;
import mobihoc.network.client.*;
import mobihoc.network.server.*;

/** Classe TcpNetworkConnection.
 * Classe que establece uma ligação TCP/IP
 **/
public class TcpNetworkConnection extends NetworkConnection {

	private ServerSocket serverSocket;
	
	public NetworkStream connect(ServerRecord server) throws IOException {
		TcpServerRecord sr;
		
		if (server instanceof TcpServerRecord) {
			sr = (TcpServerRecord) server;
		} else {
			throw new IOException("Received ServerRecord that isn't an instance of TcpServerRecord");
		}
		
		Socket s = new Socket(sr.getHostname(), sr.getPort().intValue());
		// Descomentar para simular o cliente perde coneccao com o servidor.
		//s.setSoTimeout(5000);

		return new SocketStream(s);
	}
	
	public void startServer(HostRecord config) throws IOException {
		TcpHostRecord cfg;
		
		if (config instanceof TcpHostRecord) {
			cfg = (TcpHostRecord) config;
		} else {
			throw new IOException("Received HostRecord that isn't an instance of TcpHostRecord");
		}
		
		serverSocket = new ServerSocket(cfg.getPort());

		Mobihoc.log("[S] Server running on port " + cfg.getPort());
	}
	
	public NetworkStream accept() throws IOException {
		Socket clientSocket = serverSocket.accept();
		return new SocketStream(clientSocket);
	}

}

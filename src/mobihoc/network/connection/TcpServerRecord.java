package mobihoc.network.connection;

import mobihoc.network.*;
import mobihoc.network.client.*;

/** Classe TcpServerRecord.
 * Contém host e port de um servidor tcp
 **/
public class TcpServerRecord extends ServerRecord {

	private String hostname;
	private int port;

	TcpServerRecord(String hostname, int port, boolean isDirect) {
		super(isDirect);
		this.hostname = hostname;
		this.port = port;
	}
	
	String getHostname() {
		return hostname;
	}
	
	Integer getPort() {
		return new Integer(port);
	}
	
	public String toString() {
		return "TcpServerRecord: hostname=" + hostname + " port=" + port;
	}

	public NetworkConnection getNetworkConnection() {
		return new TcpNetworkConnection();
	}
	
}

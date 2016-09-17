package mobihoc.network.connection;

import mobihoc.network.*;
import mobihoc.network.server.*;

/** Classe TcpHostRecord.
 * Contém port de um servidor tcp
 **/
public class TcpHostRecord extends HostRecord {

	private int port;

	TcpHostRecord(int port) {
		this.port = port;
	}
	
	Integer getPort() {
		return new Integer(port);
	}
	
	public String toString() {
		return "TcpHostRecord: port=" + port;
	}
	
	public NetworkConnection getNetworkConnection() {
		return new TcpNetworkConnection();
	}
	
}

package mobihoc.network.connection;

import mobihoc.network.ConnectionInfo;
import mobihoc.network.client.*;
import mobihoc.network.server.*;

/** Classe TcpDirectConnectionInfo.
 * Classe que representa a informação de uma connecção TCP directa
 **/
public class TcpDirectConnectionInfo extends ConnectionInfo {
	
	public ServerFinder getServerFinder(IServerFinderListener sfl) {
		return new TcpDirectServerFinder(sfl);
	}
	
	public String toString() {
		return "Tcp Direct";
	}
	
	public String getConnectionName() {
		return "mobihoc.network.connection.TcpConnection";
	}

	public HostConfig getHostConfig(IHostConfigListener hcl) {
		return new TcpDirectHostConfig(hcl);
	}
	
}

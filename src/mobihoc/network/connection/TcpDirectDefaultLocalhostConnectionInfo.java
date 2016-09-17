package mobihoc.network.connection;

import mobihoc.network.ConnectionInfo;
import mobihoc.network.client.*;
import mobihoc.network.server.*;

/** Classe TcpDirectDefaultLocalhostConnectionInfo.
 * Classe que representa a informação de uma connecção TCP directa
 **/
public class TcpDirectDefaultLocalhostConnectionInfo extends ConnectionInfo {
	
	public ServerFinder getServerFinder(IServerFinderListener sfl) {
		return new TcpDirectDefaultLocalhostServerFinder(sfl);
	}
	
	public String toString() {
		return "Tcp Localhost";
	}
	
	public String getConnectionName() {
		return "mobihoc.network.connection.TcpConnection";
	}

	public HostConfig getHostConfig(IHostConfigListener hcl) {
		return new TcpDirectDefaultLocalhostHostConfig(hcl);
	}
	
}

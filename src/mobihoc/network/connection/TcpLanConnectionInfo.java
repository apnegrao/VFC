package mobihoc.network.connection;

import mobihoc.network.ConnectionInfo;
import mobihoc.network.client.*;
import mobihoc.network.server.*;

/** Classe TcpLanConnectionInfo.
 * Classe que representa a informação de uma connecção TCP obtida por
 * broadcast na LAN.
 **/
public class TcpLanConnectionInfo extends ConnectionInfo {

	public static final int udpProbePort = 9119;
	
	public ServerFinder getServerFinder(IServerFinderListener sfl) {
		return new TcpLanServerFinder(sfl);
	}
	
	public String toString() {
		return "Tcp Lan";
	}
	
	public String getConnectionName() {
		return "mobihoc.network.connection.TcpLanConnection";
	}

	public HostConfig getHostConfig(IHostConfigListener hcl) {
		return new TcpLanHostConfig(hcl);
	}
	
}

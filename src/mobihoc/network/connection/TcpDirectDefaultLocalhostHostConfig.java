package mobihoc.network.connection;

import java.util.*;

import mobihoc.network.InfoItem;
import mobihoc.exception.*;
import mobihoc.network.server.*;

/** Classe TcpDirectDefaultLocalhostHostConfig.
 * Permite criar um servidor TCP localmente.
 **/
public class TcpDirectDefaultLocalhostHostConfig extends HostConfig {
	
	public TcpDirectDefaultLocalhostHostConfig(IHostConfigListener hcl) {
		super(hcl);
	}
	
	public void run() {
		int port = 1337;

		hcl.callbackConfigResults(new TcpHostRecord(port));
	}
	
	public void requestedInfoFilled() throws InfoItemResultException {
		throw new InfoItemResultException("No needed info");
	}
	
	public boolean isInfoNeeded() {
		return false;
	}

}

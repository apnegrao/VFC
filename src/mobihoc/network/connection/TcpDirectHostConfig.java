package mobihoc.network.connection;

import java.util.*;

import mobihoc.network.InfoItem;
import mobihoc.exception.*;
import mobihoc.network.server.*;

/** Classe TcpDirectHostConfig.
 * Permite criar um servidor TCP localmente.
 **/
public class TcpDirectHostConfig extends HostConfig {

	private InfoItem infoPort;
	
	public TcpDirectHostConfig(IHostConfigListener hcl) {
		super(hcl);
	}
	
	public void run() {
		List<InfoItem> neededInfo = new ArrayList<InfoItem>();
		infoPort = new InfoItem("PORT", " Server Port");
		
		neededInfo.add(infoPort);
		
		// Passar para a aplicação de cima
		hcl.callbackNeedInfo(Collections.unmodifiableList(neededInfo));
	}
	
	public void requestedInfoFilled() throws InfoItemResultException {
		int port;
		try {
			port = new Integer(infoPort.getResult()).intValue();
		} catch (java.lang.NumberFormatException e) {
			throw new InfoItemResultException("NumberFormatException while attempting to parse the port");
		}

		hcl.callbackConfigResults(new TcpHostRecord(port));
	}
	
	public boolean isInfoNeeded() {
		return true;
	}

}

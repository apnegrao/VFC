package mobihoc.network.connection;

import java.util.*;

import mobihoc.network.InfoItem;
import mobihoc.exception.*;
import mobihoc.network.server.*;

/** Classe TcpLanHostConfig.
 * Permite criar um servidor TCP que possibilita a descoberta a clientes em LAN
 **/
public class TcpLanHostConfig extends HostConfig {

	private InfoItem infoPort;
	
	public TcpLanHostConfig(IHostConfigListener hcl) {
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

		UdpDiscoveryThread udt = new UdpDiscoveryThread(port);
		udt.start();

		hcl.callbackConfigResults(new TcpHostRecord(port));
	}
	
	public boolean isInfoNeeded() {
		return true;
	}

}

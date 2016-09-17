package mobihoc.network.connection;

import java.util.*;

import mobihoc.network.InfoItem;
import mobihoc.exception.InfoItemResultException;
import mobihoc.network.client.*;

/** Classe TcpDirectServerFinder.
 * Permite ligar directamente a um servidor TCP.
 **/
public class TcpDirectServerFinder extends ServerFinder {

	private InfoItem infoHostname;
	private InfoItem infoPort;

	public TcpDirectServerFinder(IServerFinderListener sfl) {
		super(sfl);
	}
	
	public void run()  {
		List<InfoItem> neededInfo = new ArrayList<InfoItem>();
		infoHostname = new InfoItem("HOSTNAME", "Server Hostname");
		infoPort = new InfoItem("PORT", "Server Port");
		
		neededInfo.add(infoHostname);
		neededInfo.add(infoPort);
		
		// Passar para a aplicação de cima
		sfl.callbackNeedInfo(Collections.unmodifiableList(neededInfo));
	}
	
	public void requestedInfoFilled() throws InfoItemResultException {
		String hostname = infoHostname.getResult();
		int port;
		try {
			port = new Integer(infoPort.getResult()).intValue();
		} catch (java.lang.NumberFormatException e) {
			throw new InfoItemResultException("NumberFormatException while attempting to parse the port");
		}
		
		List<ServerRecord> result = new ArrayList<ServerRecord>();
		result.add(new TcpServerRecord(hostname, port, true));
		sfl.callbackSearchResults(result);
	}

	public boolean isInfoNeeded() {
		return true;
	}

}

package mobihoc.network.connection;

import java.util.*;

import mobihoc.network.InfoItem;
import mobihoc.exception.InfoItemResultException;
import mobihoc.network.client.*;

/** Classe TcpDirectDefaultLocalhostServerFinder.
 * Permite ligar directamente a um servidor TCP.
 **/
public class TcpDirectDefaultLocalhostServerFinder extends ServerFinder {

	public TcpDirectDefaultLocalhostServerFinder(IServerFinderListener sfl) {
		super(sfl);
	}

	public void run()  {
		String hostname = "localhost";
		int port = 1337;
		
		List<ServerRecord> result = new ArrayList<ServerRecord>();
		result.add(new TcpServerRecord(hostname, port, true));
		sfl.callbackSearchResults(result);
	}

	public void requestedInfoFilled() throws InfoItemResultException {
		throw new InfoItemResultException("No needed info");
	}

	public boolean isInfoNeeded() {
		return false;
	}

}

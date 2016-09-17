package mobihoc.network;

import java.io.*;
import java.util.*;

/** Classe ConnectionInfoFactory.
 * Mantém objectos ConnectionInfo, que representam os diferentes tipos de ligações à rede existentes para
 * serem usadas pela aplicação cliente mobihoc.
 **/
public class ConnectionInfoFactory {

	public static List<ConnectionInfo> getAvailableConnectionInfos() {
		List<ConnectionInfo> list = new ArrayList<ConnectionInfo>();
		
		list.add(new mobihoc.network.connection.TcpDirectConnectionInfo());
		list.add(new mobihoc.network.connection.TcpDirectDefaultLocalhostConnectionInfo());
		list.add(new mobihoc.network.connection.TcpLanConnectionInfo());
		
		return list;
	}

}

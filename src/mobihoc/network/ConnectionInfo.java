package mobihoc.network;

import java.io.*;
import mobihoc.network.client.*;
import mobihoc.network.server.*;

/** Classe ConnectionInfo.
 * Superclasse que representa vários tipos de ligação diferentes.
 **/
public abstract class ConnectionInfo {
	
	public abstract NetworkFinder getServerFinder(IServerFinderListener sfl);
	public abstract String toString();
	public abstract String getConnectionName();
	public abstract NetworkFinder getHostConfig(IHostConfigListener hcl);
	
}

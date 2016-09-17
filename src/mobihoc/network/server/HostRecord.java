package mobihoc.network.server;

import mobihoc.network.*;

/** Classe HostRecord.
 * Representa uma configuração para um servidor.
 **/
public abstract class HostRecord {

	public abstract String toString();
	public abstract NetworkConnection getNetworkConnection();

}

package mobihoc.network.client;

import mobihoc.network.*;
import mobihoc.exception.InfoItemResultException;

/** Classe ServerFinder.
 * Superclasse de todos os scanners de servidores existentes.
 **/
public abstract class ServerFinder extends NetworkFinder {

	protected IServerFinderListener sfl;
	
	public ServerFinder(IServerFinderListener sfl) {
		this.sfl = sfl;
	}

}

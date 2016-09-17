package mobihoc.network;

import mobihoc.exception.*;

/** Classe NetworkFinder.
 * Superclasse de classes que procuram peers ou configuram servidores.
 **/
public abstract class NetworkFinder {
	
	public abstract void run();
	
	public abstract void requestedInfoFilled() throws InfoItemResultException;
	
	public abstract boolean isInfoNeeded();

}

package mobihoc.network.server;

import mobihoc.network.*;
import mobihoc.exception.EmptyInfoItemResultException;

/** Classe HostConfig.
 * Superclasse de todas as configurações para servidores de rede.
 **/
public abstract class HostConfig extends NetworkFinder {

	protected IHostConfigListener hcl;
	
	public HostConfig(IHostConfigListener hcl) {
		this.hcl = hcl;
	}

}

package mobihoc.session.server;

import java.util.*;

import mobihoc.session.DataPool;
import mobihoc.session.DataUnit;
import mobihoc.session.UserAgent;
import mobihoc.session.Phi;

public abstract class CMB {

	protected DataPool _pool;
	protected Set<UserAgent>_clients;
	protected Map<UserAgent, Phi> _phi;

	public CMB(DataPool pool, Set<UserAgent> clients) {
		_pool = pool;
		_clients = clients;
	}
	
	public CMB(DataPool pool, Set<UserAgent> clients, Map<UserAgent, Phi> phi) {
		_pool = pool;
		_clients = clients;
		_phi = phi;
	}

	public abstract void updatesReceived(DataUnit[] dus, UserAgent client);

	public abstract Map<UserAgent, List<DataUnit>> computeUpdatesToDiffuse(long t);

	public abstract void deactivateClient(UserAgent client);
}

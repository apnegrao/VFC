package mobihoc.session.server.cmb;

import java.util.*;

import mobihoc.session.DataPool;
import mobihoc.session.DataUnit;
import mobihoc.session.UserAgent;
import mobihoc.session.server.CMB;

public class BroadcastCMB extends CMB {
	
	private boolean[] D;

	public BroadcastCMB(DataPool pool, Set<UserAgent> clients) {
		super(pool,clients);
		D = new boolean[_pool.count()];
		for (int i = 0; i < D.length; i++) {
			D[i] = false;
		}
	}

	public void updatesReceived(DataUnit[] dus, UserAgent client) {
		synchronized (this) {
			for (int i = 0; i < dus.length; i++) {
				D[dus[i].getId()] = true;
			}
		}
	}

	public Map<UserAgent, List<DataUnit>> computeUpdatesToDiffuse(long t) {
		synchronized (this) {
			Vector<DataUnit> updates = new Vector<DataUnit>();
			for (int i = 0; i < _pool.count(); i++) {
				DataUnit du = _pool.getRef(i);
				if (D[i]) {
					if (du != null) {
						updates.addElement(du);
					}
				}
			}
			updates.trimToSize();
			DataUnit[] upvector = new DataUnit[updates.size()];  
			updates.copyInto(upvector);
			
			Map<UserAgent, List<DataUnit>> cliupdates = new HashMap<UserAgent, List<DataUnit>>();
			for (UserAgent c : _clients) {
				List<DataUnit> ldu = new ArrayList<DataUnit>();
				for (DataUnit du : upvector) {
					ldu.add(du);
				}
				cliupdates.put(c, ldu);
			}
			return cliupdates;
		}
	}

	public void deactivateClient(UserAgent client) {
		// what happens if a client goes away
	}

}

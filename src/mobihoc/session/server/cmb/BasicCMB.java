package mobihoc.session.server.cmb;

import mobihoc.Mobihoc;
import mobihoc.session.DataPool;
import mobihoc.session.DataUnit;
import mobihoc.session.UserAgent;
import mobihoc.session.server.CMB;
import mobihoc.session.server.UpdateQueue;

import java.util.*;

public class BasicCMB extends CMB {
	
	private UpdateQueue _uq;

	public BasicCMB(DataPool pool, Set<UserAgent> clients) {
		super(pool,clients);

		_uq = new UpdateQueue();
		if (pool.size() == 0) Mobihoc.log("BasicCMB::BasicCMB Invalid pool size (size = 0)");
		_uq.configure(_pool.size());
	}

	public void updatesReceived(DataUnit[] dus, UserAgent client) {
		synchronized (this) {
			_uq.add(dus);
			_uq.dumpContents("[S] Write: UQ contents ->");
			System.out.println();
		}
	}

	public Map<UserAgent, List<DataUnit>> computeUpdatesToDiffuse(long t) {
		synchronized (this) {
			int[] upIds = _uq.getUpdateIds();

			_pool.merge(_uq.flush());
			
			if (upIds!=null && upIds.length > 0) {
				// print state after merge
				System.out.println("[S] Merging:");
				System.out.print(  "[S]    Pool ->");
				_pool.dump();
				System.out.println();
				_uq.dumpContents(  "[S]    UQ   ->");
				System.out.println();
			}
			
			DataUnit[] upvector = _pool.retrieve(upIds);
			
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

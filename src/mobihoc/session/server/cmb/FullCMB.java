package mobihoc.session.server.cmb;

import java.util.*;

import mobihoc.session.DataPool;
import mobihoc.session.DataUnit;
import mobihoc.session.UserAgent;
import mobihoc.session.server.CMB;
import mobihoc.session.Phi;
import mobihoc.session.server.UpdateQueue;

public class FullCMB extends CMB {
	
	private Map<UserAgent, List<Integer>> timesUpdated;		/** the number of updates since last time we sent the updated object to the client **/
	private UpdateQueue _uq;
	private Map<UserAgent, Phi> _phi;
	private Map<UserAgent, List<DataUnit>> lastUpdates;	/** the array containing all the updates that were sent the last time **/
	
	/*public FullCMB(DataPool pool, int[] clients) {
		super(pool,clients);
		_uq = new UpdateQueue();
		_uq.configure(_pool.size());
		
		System.out.println("[S] << Vfc Full CMB>>");
		
	}*/

	public FullCMB(DataPool pool, Set<UserAgent> clients, Map<UserAgent, Phi> phi) {
		super(pool,clients,phi);
		_uq = new UpdateQueue();
		_uq.configure(_pool.size());
		_phi = phi;
		
		System.out.println("[S] << Vfc Full CMB>>");
		
		timesUpdated = new TreeMap<UserAgent, List<Integer>>();
		lastUpdates = new TreeMap<UserAgent, List<DataUnit>>();
		
		for (UserAgent c : clients) {
			List<Integer> times = new ArrayList<Integer>();
			List<DataUnit> up = new ArrayList<DataUnit>();
			for (int o = 0; o < _pool.size(); o++) {
				times.add(0);
				up.add(null);
			}
			timesUpdated.put(c, times);
			lastUpdates.put(c, up);
		}
	}
	
	/** 
	for each of the registered clients, the number of new updates received for the passed dataUnit(s) is incremented
	the source of the update does not get his own counter incremented because he was the one that originated it
	**/
	public void updatesReceived(DataUnit[] dus, UserAgent client) {
		synchronized (this) {
			System.out.println("[S] << Vfc Full CMB>>");

			_uq.add(dus);
			_uq.dumpContents("[S] Write: UQ contents ->");
			System.out.println();

			// Increment the number of updates received
			for (UserAgent c : timesUpdated.keySet()) {
				if (c.equals(client)) continue; // Don't increment the number of new updates received for the sender of the update
				System.out.println("[S]  Increment updates for client #" + c);
				for (int o = 0; o < dus.length; o++) {
					timesUpdated.get(c).set(dus[o].getId(), timesUpdated.get(c).get(dus[o].getId()) + 1);
				}
			}
		}
	}

	/** computes the updates to be sent at the beginning of round "t" which is passed as argument **/
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
			Map<UserAgent, List<DataUnit>> cliupdates = new TreeMap<UserAgent, List<DataUnit>>();
			
			/** for each of the registered clients **/
			for (UserAgent c : _clients) {
				cliupdates.put(c, new ArrayList<DataUnit>());
				Vector<DataUnit> updates = new Vector<DataUnit>();
				
				Phi phi = _phi.get(c);	/** we assume there is a phi for each of the clients. the phi's might or might not be identic **/

				if (phi == null) { /** if there is no consistency policy specified,  the client receives all the "new" updates **/
					for (int i = 0; i < _pool.count(); i++) { /** foreach of the objects in the datapool **/
						if (timesUpdated.get(c).get(i) > 0) { /** there are new updates since the last time the client got an update on this object **/
							DataUnit du = _pool.getRef(i);
							//fixme: vale a pena testar de o dataUnit != null se temos a certeza de que foi de facto actualizado desde a ultima vez?
							if (du != null) updates.addElement(du);
							timesUpdated.get(c).set(i, 0); /** reset the counter, since we're sending the update this round**/
							lastUpdates.get(c).set(i, du); /** sets the lastUpdate to reflect the new update sent to the client **/
						} else { /** the object is still the same since last (round) time we checked it **/
							/** do nothing **/
						}
					}
				} else {
					for (int i = 0; i < _pool.count(); i++) { /** foreach of the objects in the datapool **/
						DataUnit du = _pool.getRef(i);
						if (phi.shouldBeSentToClient(du, lastUpdates.get(c).get(i), timesUpdated.get(c).get(i), t, _pool)) {/** the shouldBeSentToClient tests whether the dataUnit is null or not**/
							updates.addElement(du);
							timesUpdated.get(c).set(i, 0); /** reset the counter, since we're sending the update this round**/
							lastUpdates.get(c).set(i, du); /** sets the lastUpdate to reflect the new update sent to the client **/
						} else { /** according to the phi in consideration, the object should not be sent to the client **/
							/** do nothing **/
						}
					}
				}
				
				updates.trimToSize();
				for(DataUnit d : updates) {
					cliupdates.get(c).add(d);
				}
			}
			return cliupdates;
		}
	}

	public void deactivateClient(UserAgent client) {
		// what happens if a client goes away
	}

}

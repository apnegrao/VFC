package mobihoc.session;

import mobihoc.session.DataPool;
import mobihoc.session.DataUnit;
import java.util.*;//Vector;
import java.lang.Math;
import mobihoc.annotation.PhiAnnotation;

public class Phi implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer[] _regObjects;	// O - Subset of objects that the consistency specification refers to. 
	private int[] _zones;		// Z - Consistency zone vector Z specifying how to draw the consistency zones around the pivots. It is #Z sized and specifies #Z + 1 consistency zones.
	private KVec[] _kVectors;	// C - Consistency scale characterizing the consistency degrees for applying into the consistency zones. It is #C sized with #C = #Z + 1 consistency degrees.
	private int _clientId;
	private List<Integer> _pivots; // V - Set identifying the pivot objects for each view of the virtual world.
	
	
	/** constructor to be used for creating "default" phi **/
	
	public Phi(){
		KVec k0 = new KVec(3,0,0);//update a cada 3 rounds
		KVec k1 = new KVec(50,10,500);//update a cada 20 rounds, a cada 10 updates, ou a 500% de diferença nos valores
		this._regObjects = null;
		this._zones = new int[2];
		this._zones[0] = 40;//a 2 espacos de distancia
		this._zones[1] = -1;//a mais de 2 espacos de distancia
		this._kVectors = new KVec[2];
		this._kVectors[0] = k0;
		this._kVectors[1] = k1;
		this._pivots = new ArrayList<Integer>();
	}
	
	public Phi(Integer[] objects, int[] zones, KVec[] kVectors, List<Integer> pivots) {
		this._regObjects = objects;
		this._zones = zones;
		this._kVectors = kVectors;
		this._pivots = pivots;
	}
	
	/** constructor without the set of pivots **/
	public Phi(Integer[] objects, int[] zones, KVec[] kVectors) {
		this._regObjects = objects;
		this._zones = zones;
		this._kVectors = kVectors;
		this._pivots = new ArrayList<Integer>();
	}
	
	
	public Integer[] getRegObjects() {
		return _regObjects;
	}
	
	public Phi clone(int id) {
		Phi copy = new Phi(_regObjects, _zones, _kVectors, new ArrayList<Integer>(_pivots));
		copy.setClientId(id);
		return copy;
	}
	
	public int[] getZones() {
		return _zones;
	}
	
	public KVec[] getKVectors() {
		return _kVectors;
	}
	
	public int getClientId() {
		return _clientId;
	}
	
	public void setZones(int[] zones) {
		this._zones = zones;
	}
	
	public void setKVectors(KVec[] kVectors) {
		this._kVectors = kVectors;
	}
	
	public void addPivot(Integer duId) {
		this._pivots.add(duId);
	}
	
	public void setClientId(int clientId) {
		this._clientId = clientId;
	}
	
	/** registers a new object to the current phi **/
	public void registerObject(int objectId) {
		java.lang.reflect.Array.setInt(_regObjects, _regObjects.length, objectId);
	}
	
	/** returns true if the object referenced by the Id argument should be considered "dirty". otherwise returns false **/
	public boolean shouldBeSentToClient(DataUnit du, DataUnit lastUpdate, int timesUpdated, long tick, DataPool pool) {
		//Vector<Integer> objects = new Vector<Integer>(java.util.Arrays.asList(_regObjects));
		KVec k;
		int zoneIndex = _zones.length;
		if (du == null) return false;
		//if (!objects.contains(du.getId())) return false;	// if the considered dataUnit is not registered with the considered phi, then False
		
		for (Integer duId : _pivots) {//obtain the closest possible zone index
			zoneIndex = Math.min(zoneIndex, getZoneIndex(du, lastUpdate, pool.getRef(duId)));
		}
		
		if (_pivots.size() == 0) zoneIndex = 0;
		
		k = _kVectors[zoneIndex];	// find the K vector associated with the consistency zone to which the object (du) belongs
		
		if ((tick % k.getTheta()) == 0) return true;
		if (k.getSigma() <= timesUpdated) return true;
		if (k.getNiu() <= (du.compareNiu(lastUpdate))) return true;
		return false;
	}
	
	/** returns the index of the of consistency zone to which the argument object belongs **/
	public int getZoneIndex(DataUnit du, DataUnit oldDu, DataUnit pivot) {
		//if (du.isOmnipresent() || pivot.isOmnipresent()) return 0;
		int distance = du.compareWithPivot(pivot);
		if (oldDu != null) {
			distance = Math.min(distance, oldDu.compareWithPivot(pivot));
		}
		int radius;
		int maxRadius;
		for (int i = 0; i < _zones.length; i++) {
			radius = _zones[i];
			if (radius == -1) return i; // -1 = oo
			if (distance <= radius) return i;
		}
		return 0;
	}

	public static Phi fromAnnotation(PhiAnnotation anot) {
		int numZones = anot.zones();
		int[] zones = new int[numZones];
		KVec[] vectors = new KVec[numZones];
		for (int i = 0; i < numZones; i++) {
			zones[i] = anot.zoneRange()[i];
			vectors[i] = new KVec(anot.theta()[i], anot.sigma()[i], anot.niu()[i]);
		}
		return new Phi(null, zones, vectors);
	}

}

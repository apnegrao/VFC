package mobihoc.session;

import java.util.Enumeration;
import java.util.Vector;

import mobihoc.network.server.ConnectionHandler;

//Class containing all the objects that represent the modelled state by the application
//each individual state object is represented by a DataUnit
public class DataPool implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	private boolean _isFreezed;
	
	/** Vector needed for variable length array operation (mainly for adding new DataUnits to the Pool) **/
	private Vector<DataUnit> _poolU;
	
	/**
	fixed length array can be used because when in frozen state, the only allowed operations are those of reading/processing of the DataUnits.
	there can be no inserting of new DataUnits while the Pool is in frozen state
	**/
	private DataUnit[] _poolF;
	private int _counter;
	
	public DataPool() {
		_counter = 0;
		_poolU = new Vector<DataUnit>();
		_poolF = null;
		_isFreezed = false;
	}
	
	/**
	adds a group of dataUnits to the _poolU, if the DataPool is not frozen, else returns an array of type long filled with NULL_ID
	returns an array of the new id's of the added data units to the dataPool
	**/
	public int[] register(DataUnit[] dus) {
		synchronized (this) {
			int[] result = new int [dus.length];
			for (int i = 0; i < dus.length; i++) {
				if(_isFreezed) {
					result[i] = DataUnit.NULL_ID;
					continue;
				}
				DataUnit du = dus[i].clone();
				int id = DataUnit.NULL_ID;
				if (du != null) {
					id = _counter++;
					du.setId(id);
					_poolU.addElement(du);
				}
				result[i] = id;
			}
			return result;
		}
	}
	
	/**
	transfers all the dataUnits from the _poolU to _poolF
	empties _poolU
	makes it impossible to add new dataUnits to the DataPool unless defrosted
	**/
	public void freeze() throws Exception {
		synchronized (this) {
			if(!_isFreezed) {
				_poolF = new DataUnit[_poolU.size()];
				Enumeration<DataUnit> elements = _poolU.elements();
				int i = 0;
				while(elements.hasMoreElements()) {
					DataUnit du = elements.nextElement();
					if (i != du.getId()) {
						_poolF = null;
						throw new Exception("Inconsistent pool state.");
					}
					_poolF[i++] = du;
				}
				_poolU.removeAllElements();
				_isFreezed = true;
			}
		}
	}

	/** returns the number of DataUnits in the DataPool **/
	public int size() {
		synchronized (this) {
			if(_isFreezed) {
				return _poolF.length;
			} else {
				return _poolU.size();
			}
		}
	}
	
	/**
	seems that an update is only made when the DataPool is in the frozen state
	updates the DataUnits contained in the _poolF with the new DataUnits from the updates[]
	only existing data units (in the data pool) are updated. in other words DataUnits that do not belong to the DataPool are not added to the pool or influence the existing data in any other way
	**/
	public int merge(DataUnit[] updates) {
		synchronized (this) {
			int num = 0;
			if (updates != null && _isFreezed) {
				for (int i = 0; i < updates.length; i++) {
					DataUnit update = updates[i];
					int id = update.getId();
					if (id < 0 || id >= _poolF.length) {
						continue;
					}
//					_poolF[id].merge(update);
					_poolF[id] = update.clone();
					num++;
				}
			}
			return num;
		}
	}
	
	/**
	returns the DataUnits corresponding to the id array passed as argument
	operation only executed when the DataPool is frozen
	**/
	public DataUnit[] retrieve(int[] ids) {
		synchronized (this) {
			if (ids == null || !_isFreezed) {
				return null;
			}
			DataUnit[] objs = new DataUnit[ids.length];
			for(int i = 0; i < ids.length; i++) {
				int id = ids[i];
				objs[i] = (id < 0 || id >= _poolF.length)?null:_poolF[id].clone();
			}
			return objs;
		}
	}
	
	/**
	returns all the DataUnits contained in the DataPool
	operation only executed when the DataPool is not frozen
	**/
	public DataUnit[] getContents() {
		synchronized (this) {
			if (_isFreezed) {
				return null;
			}
			int length = size();
			DataUnit[] objs = new DataUnit[length];
			for(int i = 0; i < length; i++) {
				objs[i] = _poolU.get(i).clone();
			}
			return objs;
		}
	}

	/** returns the number of elements (DataUnits) in the DataPool **/
	public int count() {
		synchronized (this) {
			return _counter;
		}
	}
	
	/**
	returns the DataUnit associated to the id passed as argument
	only appliable in frozen state
	**/
	public DataUnit getRef(int id) {
		synchronized (this) {
			if (_isFreezed && id>= 0 && id < _poolF.length) {
				return _poolF[id];
			} else {
				return null;
			}
		}
	}

	/** resets the DataPool **/
	public void clear() {
		synchronized (this) {
			_counter = DataUnit.NULL_ID;
			if(_isFreezed) {
				_poolF = null;
				_isFreezed = false;
			} else {
				_poolU.removeAllElements();
			}
		}
	}

	/** displays the contents of the DataPool **/
	public void dump() {
		synchronized (this) {
			if (_isFreezed) {
				dumpFreezed();
			} else {
				dumpUnfreezed();
			}
		}
	}

	/** displays the contents of the _poolU (defrosted pool) to the standard output **/
	private void dumpUnfreezed() {
		boolean first = true;
		System.out.print("{");
		Enumeration<DataUnit> elements = _poolU.elements();
		while(elements.hasMoreElements()) {
			DataUnit du = elements.nextElement();
			if (first) {
				System.out.print(du.print());
				first = false;
			} else {
				System.out.print(" "+du.print());
			}
		}
		System.out.println("}");
	}

	/** displays the contents of the _poolF (frozen pool) to the standard output **/
	private void dumpFreezed() {
		DataUnit[] dus = _poolF;
		System.out.print("{");
		for(int i = 0; i < dus.length; i++) {
			System.out.print(dus[i].print());
			if (i != dus.length-1) {
				System.out.print(",");
			}
		}
		System.out.print("}");
	}

	public boolean isFrozen() {
		return _isFreezed;
	}
}
package mobihoc.session.server;

import java.util.Enumeration;
import java.util.Vector;

import mobihoc.session.DataUnit;

public class UpdateQueue {
	
	private DataUnit[] _indexes;
	private Vector<Integer> _updates;

	public UpdateQueue() {
		_indexes = null;
		_updates = new Vector<Integer>();
	}

	public void configure(int size) {
		synchronized (this) {
			if (size > 0 && _indexes == null) {
				_indexes = new DataUnit[size];
				_updates.removeAllElements();
			}
		}
	}

	public void add(DataUnit[] updates) {
		synchronized (this) {
			for (int i = 0; i < updates.length; i++) {
				DataUnit du = updates[i];
				int id = du.getId();
				if (id >= 0 && id < _indexes.length) {
					if (_indexes[id] == null) {
						_updates.addElement(new Integer(id));
					}
					_indexes[id] = du;
				}
			}
		}
	}

	public DataUnit[] flush() {
		synchronized (this) {
			DataUnit[] updates = new DataUnit[_updates.size()];
			Enumeration<Integer> e = _updates.elements();
			int i = 0;
			while (e.hasMoreElements()) {
				int id = (e.nextElement()).intValue();
				updates[i++] = _indexes[id];
				_indexes[id] = null;
			}
			_updates.removeAllElements();
			return updates;
		}
	}


	public int[] getUpdateIds() {
		synchronized (this) {
			int[] updates = new int[_updates.size()];
			Enumeration<Integer> e = _updates.elements();
			int i = 0;
			while (e.hasMoreElements()) {
				updates[i++] = (e.nextElement()).intValue();
			}
			return updates;
		}
	}

/*
	public void stats() {
		System.out.println("UQ: #updates="+_updates.size());
	}
*/
	public void dumpContents(String prefix) {
		synchronized (this) {
			boolean isFirst = true;
			System.out.print(prefix+" {");
			Enumeration<Integer> e = _updates.elements();
			while (e.hasMoreElements()) {
				int id = (e.nextElement()).intValue();
				System.out.print(((isFirst)?"":",")+id + "-" + _indexes[id].print());
				if (isFirst) {
					isFirst = false;
				}
			}
			System.out.print("}");
		}
	}
}
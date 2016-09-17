package app.proto3.client;

import mobihoc.session.DataUnit;

/* [Serializable] */
public class IntegerDataUnit extends DataUnit {

	private static final long serialVersionUID = 1L;
	
	private Integer _val;
	
	public IntegerDataUnit() {
		super();
	}
	
	public IntegerDataUnit(int val) {
		super();
		_val = new Integer(val);
	}

	public int getValue() {
		return _val.intValue();
	}
	
	public DataUnit clone() {
		DataUnit du = new IntegerDataUnit(_val.intValue());
		du.setId(this.getId());
		return du;
	}

	public String print() {
		return "("+this.getId()+","+_val.toString()+")";
	}
	
	public void merge(DataUnit update) {
		this._val = new Integer(((IntegerDataUnit)update).getValue());
	}

	public float compareNiu(DataUnit du) {
		return (float)this._val/((IntegerDataUnit)du).getValue();
	}
	
	public int getX() {
		return -1;
	}
	
	public int getY() {
		return -1;
	}
	
	public boolean isOmnipresent() {
		return true;
	}
	
	public boolean isPivot() {
		return true;
	}
}

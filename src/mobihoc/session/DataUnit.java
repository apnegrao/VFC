package mobihoc.session;

import java.util.*;
import java.io.*;

import mobihoc.Mobihoc;

public abstract class DataUnit implements Serializable, Comparable<DataUnit>, Cloneable {

	public static final int NULL_ID = -1;

	private int _id;

	public DataUnit() {
		_id = NULL_ID;
	}

	public DataUnit(int id) {
		_id = id;
	}

	public int getId() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public abstract String print();
	
	public DataUnit clone() {
		try {
			return (DataUnit)super.clone();
		} catch (CloneNotSupportedException e) {
			System.out.println("ERROR: CloneNotSupportedException in DataUnit::clone");
			return null;
		}
	}
	
	public abstract void merge(DataUnit update);

	public abstract float compareNiu (DataUnit du);
	
	public abstract boolean isPivot();
	
	public int compareWithPivot(DataUnit pivot) {
		return 0;
	}

	public static void printSet(String prefix, DataUnit[] dus) {
		//String out = new String();
		String out = "";
		out += prefix + " {";
		//out.concat(prefix + " {");
		for(int i = 0; i < dus.length; i++) {
			if(dus[i] == null) {
				//out.concat("N");
				out += "N";
			} else {
				//out.concat(dus[i].print());
				out += dus[i].print();
			}
			if (i != dus.length-1) {
				//out.concat(",");
				out += ",";
			}
		}
		//out.concat("}");
		out += "}";
		Mobihoc.log(out);
	}
	
	public int compareTo(DataUnit o) {
		return this.getId()-o.getId();
	}
	
	public boolean equals(Object o) {
		try {
			return this.compareTo((DataUnit)o) == 0;
		} catch (ClassCastException e) {
			return false;
		}
	}

}

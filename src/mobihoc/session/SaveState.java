package mobihoc.session;

import java.util.*;
import java.io.*;

import mobihoc.session.Phi;

public class SaveState implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	private DataPool _pool;
	private Map<Integer, UserAgent> _duAssociation;
	private Map<UserAgent, Phi> _phi;
	private String _saveName;

	public SaveState(DataPool pool, Map<Integer, UserAgent> duAssociation, Map<UserAgent, Phi> phi, String saveName) {
		_pool = pool;
		_duAssociation = duAssociation;
		_phi = phi;
		_saveName = saveName;
	}
	
	public boolean flush() {
		System.out.println("Vou gravar o save com o nome: " + _saveName);
		try {
        		// Serialize to a file
        		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(_saveName)));
        		out.writeObject(this);
			out.close();
		} catch (IOException e) {
			System.out.println("Could not save with name " + _saveName);
			return false;
		}		
		return true;
	}
	
	public static SaveState buildSaveState(String fileName) {
		SaveState res = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fileName)));
			// De-serialize the object
			res = (SaveState) in.readObject();
			in.close();
			
		} catch(IOException e) {
			System.out.println("SaveState : Could not build save. " + e);
		} catch (java.lang.ClassNotFoundException e) {
			System.out.println("SaveState : Could not build save. " + e);}
		return res;
	}
	
	public boolean printComparation(SaveState ss) {
		System.out.println("Comparar...");
		this._pool.dump();
		System.out.println("");
		ss.getDataPool().dump();
		System.out.println("");
		System.out.println(this._duAssociation.toString());
		System.out.println(ss.getDuAssociation().toString());
		System.out.println("");
		System.out.println(this._phi.toString());
		System.out.println(ss.getPhi().toString());
		return true;
	}
	
	public DataPool getDataPool() {
		return _pool;
	}
	
	public Map<Integer, UserAgent> getDuAssociation() {
		return _duAssociation;
	}
	
	public Map<UserAgent, Phi> getPhi() {
		return _phi;
	}
	
	public String getSaveName() {
		return _saveName;
	}
	
}
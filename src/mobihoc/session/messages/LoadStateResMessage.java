package mobihoc.session.messages;

import java.util.*;

import mobihoc.session.Message;
import mobihoc.session.DataUnit;
import mobihoc.session.UserAgent;

/* [Serializable] */
public class LoadStateResMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	private DataUnit[] _pool;
	private Map<Integer, UserAgent> _duAssociation;
	
	public LoadStateResMessage(DataUnit[] pool, Map<Integer, UserAgent> duAssociation){
		super();
		type = MType.LOAD_STATE_RES;
		_pool = pool;
		_duAssociation = duAssociation;
	}

	public DataUnit[] getDataUnits() {
		return _pool;
	}
	
	public Map<Integer, UserAgent> getDuAssociation() {
		return _duAssociation;
	}
}
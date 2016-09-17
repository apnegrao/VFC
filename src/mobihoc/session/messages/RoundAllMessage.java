package mobihoc.session.messages;

import mobihoc.session.DataUnit;
import mobihoc.session.Message;

/* [Serializable] */
public class RoundAllMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	public long tickno;
	public Object[] dus;
	
	public RoundAllMessage(){
		type = MType.ROUND_ALL;
	}
	
	public RoundAllMessage(long tickno, DataUnit[] dus){
		type = MType.ROUND_ALL;
		this.tickno = tickno;
		this.dus = dus;
	}

	public DataUnit[] getDataUnits() {
		if (dus == null) {
			return null;
		} else {
			DataUnit[] d = new DataUnit[dus.length];
			for(int i = 0; i < dus.length; i++) {
				d[i] = (DataUnit)dus[i];
			}
			return d;
		}
	}
}

package mobihoc.session.messages;

import mobihoc.session.DataUnit;
import mobihoc.session.Message;

/* [Serializable] */
public class WriteMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	public Object[] dus;

	public WriteMessage(){
		super();
		type = MType.WRITE;
	}

	public WriteMessage(DataUnit[] dus){
		super();
		type = MType.WRITE;
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

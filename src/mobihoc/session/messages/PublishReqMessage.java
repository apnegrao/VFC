package mobihoc.session.messages;

import mobihoc.session.DataUnit;
import mobihoc.session.Message;

/* [Serializable] */
public class PublishReqMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	public Object[] dus;

	public PublishReqMessage(){
		super();
		type = MType.PUBLISH_REQ;
	}

	public PublishReqMessage(DataUnit[] dus){
		super();
		type = MType.PUBLISH_REQ;
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

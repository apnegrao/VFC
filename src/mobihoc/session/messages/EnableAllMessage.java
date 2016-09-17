package mobihoc.session.messages;

import mobihoc.session.Message;

/* [Serializable] */
public class EnableAllMessage extends Message {
	
	private static final long serialVersionUID = 1L;
	
	public EnableAllMessage(){
		type = MType.ENABLE_ALL;
	}
	
	public EnableAllMessage(MStatus status){
		type = MType.ENABLE_ALL;
		this.status = status;
	}
}

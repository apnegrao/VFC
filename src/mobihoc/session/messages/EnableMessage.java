package mobihoc.session.messages;

import mobihoc.session.Message;

/* [Serializable] */
public class EnableMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	public EnableMessage(){
		type = MType.ENABLE;
	}
}

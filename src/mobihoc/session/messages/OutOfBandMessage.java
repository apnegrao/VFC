package mobihoc.session.messages;

import mobihoc.session.Message;

/* [Serializable] */
public class OutOfBandMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	public String msg;
	
	public OutOfBandMessage(){
		type = MType.OUT_OF_BAND;
	}

	public OutOfBandMessage(String msg){
		type = MType.OUT_OF_BAND;
		this.msg = msg;
	}
}

package mobihoc.session.messages;

import mobihoc.session.DataUnit;
import mobihoc.session.Message;

/* [Serializable] */
public class ReadMessage extends Message {

	private static final long serialVersionUID = 1L;
	

	public ReadMessage(){
		super();
		type = MType.READ;
	}

}

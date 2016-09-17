package mobihoc.session.messages;

import mobihoc.session.Message;

/* [Serializable] */
public class DisableMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	public byte[] data;
	
	public DisableMessage(){
		type = MType.DISABLE;
	}
	
	public DisableMessage(byte[] key){
		type = MType.DISABLE;
		data = key;
	}
}

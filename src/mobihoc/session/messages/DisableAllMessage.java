package mobihoc.session.messages;

import mobihoc.session.Message;

/* [Serializable] */
public class DisableAllMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	public byte[] data;
	
	public DisableAllMessage(){
		type = MType.DISABLE_ALL;
	}
	
	public DisableAllMessage(byte[] key){
		type = MType.DISABLE_ALL;
		data = key;
	}
}

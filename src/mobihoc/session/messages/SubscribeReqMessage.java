package mobihoc.session.messages;

import mobihoc.session.Message;
import mobihoc.session.UserAgent;

/* [Serializable] */
public class SubscribeReqMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	public byte[] data;
	
	private UserAgent userAgent;
	
	public SubscribeReqMessage(){
		type = MType.SUBSCRIBE_REQ;
	}

	public SubscribeReqMessage(MStatus status){
		type = MType.SUBSCRIBE_REQ;
		this.status = status;
	}

	public SubscribeReqMessage(UserAgent ua) {
		type = MType.SUBSCRIBE_REQ;
		userAgent = ua;
	}
	
	public SubscribeReqMessage(byte[] key){
		type = MType.SUBSCRIBE_REQ;
		data = key;
	}
	
	public UserAgent getUserAgent() {
		return userAgent;
	}
}

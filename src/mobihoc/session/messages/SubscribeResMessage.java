package mobihoc.session.messages;

import mobihoc.session.Message;

/* [Serializable] */
public class SubscribeResMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	public int clientID;

	public SubscribeResMessage(){
		type = MType.SUBSCRIBE_RES;
	}

	public SubscribeResMessage(MStatus status){
		type = MType.SUBSCRIBE_RES;
		this.status = status;
		this.clientID = 0;
	}

	public SubscribeResMessage(MStatus status, int clientID){
		type = MType.SUBSCRIBE_RES;
		this.status = status;
		this.clientID = clientID;
	}
}
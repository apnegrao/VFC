package mobihoc.session.messages;

import mobihoc.session.Message;

/* [Serializable] */
public class SetControlParamResMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	public int clientID;

	public SetControlParamResMessage() {
		type = MType.SET_CONTROL_PARAM_RES;
	}
}

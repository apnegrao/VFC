package mobihoc.session.messages;

import mobihoc.session.Message;
import mobihoc.session.Phi;

/* [Serializable] */
public class SetControlParamReqMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	private Phi _phi;
	
	public SetControlParamReqMessage(Phi phi){
		super();
		type = MType.SET_CONTROL_PARAM_REQ;
		_phi = phi;
	}

	public Phi getPhi() {
		return _phi;
	}
}

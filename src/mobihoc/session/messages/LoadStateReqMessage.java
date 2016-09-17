package mobihoc.session.messages;

import mobihoc.session.Message;
import mobihoc.session.SaveState;

/* [Serializable] */
public class LoadStateReqMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	private SaveState _ss;
	
	public LoadStateReqMessage(SaveState ss){
		super();
		type = MType.LOAD_STATE_REQ;
		_ss = ss;
	}
	
	public SaveState getSaveState() {
		return _ss;
	}

}
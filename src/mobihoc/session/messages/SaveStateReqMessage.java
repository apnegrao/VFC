package mobihoc.session.messages;

import mobihoc.session.Message;

/* [Serializable] */
public class SaveStateReqMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	private String _saveName;
	
	public SaveStateReqMessage(String saveName){
		super();
		type = MType.SAVE_STATE_REQ;
		_saveName = saveName;
	}

	public String getSaveName() {
		return _saveName;
	}
}

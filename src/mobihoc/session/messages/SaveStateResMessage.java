package mobihoc.session.messages;

import mobihoc.session.Message;
import mobihoc.session.SaveState;

/* [Serializable] */
public class SaveStateResMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	public SaveState ss;
	
	public SaveStateResMessage(){
		type = MType.SAVE_STATE_RES;
		ss = null;
	}

	public SaveStateResMessage(MStatus status){
		type = MType.SAVE_STATE_RES;
		this.status = status;
		this.ss = null;
	}
	
	public SaveStateResMessage(MStatus status, SaveState ss){
		type = MType.SAVE_STATE_RES;
		this.status = status;
		this.ss = ss;
	}

	public SaveState getSave() {
		return ss;
	}
}

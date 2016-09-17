package mobihoc.session.messages;

import mobihoc.session.Message;

/* [Serializable] */
public class ErrorMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	public String _error;

	public ErrorMessage(String error){
		super();
		type = MType.ERROR;
		_error = error;
	}

	public String getError() {
		return _error;
	}
}

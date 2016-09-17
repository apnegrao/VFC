package mobihoc.session;

/* [Serializable] */
public class Message implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public enum MType {
		NULL,
		SUBSCRIBE_REQ,
		SUBSCRIBE_RES,
		PUBLISH_REQ,
		PUBLISH_RES,
		PUBLISH_ALL,
		ENABLE,
		ENABLE_ALL,
		WRITE,
		READ,
		ROUND_ALL,
		DISABLE,
		DISABLE_ALL,
		ERROR,
		OUT_OF_BAND,
		TEST,
		SET_CONTROL_PARAM_REQ,
		SAVE_STATE_REQ,
		SAVE_STATE_RES,
		LOAD_STATE_REQ,
		LOAD_STATE_RES,
		SET_CONTROL_PARAM_RES
	}
	
	public enum MStatus {
		OK,
		OP_NOT_ALLOWED,
		SERVER_ERROR
	}

	public String sender=null;
	public String[] receivers=null;
	public MType type;
	public MStatus status;
	
	public Message() { }
	
	public Message(String[] receiversAddress, MType msgType)
	{
		receivers = receiversAddress;
		type = msgType;
	}
}
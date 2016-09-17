package app.proto3.client;

import mobihoc.session.Message;

/* [Serializable] */
public class TestObj2 extends Message {

	private static final long serialVersionUID = 1L;
	
	public Object[] dus;
	
	public TestObj2(){
		super();
//		type = PUBLISH_REQ_MESSAGE;
	}

	public TestObj2(Object[] dus){
		super();
//		type = PUBLISH_REQ_MESSAGE;
		this.dus = dus;
	}
}

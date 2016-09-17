package mobihoc.session.messages;

import mobihoc.session.Message;

/* [Serializable] */
public class PublishResMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	public int[] duIds;
	
	public PublishResMessage(){
		type = MType.PUBLISH_RES;
		duIds = null;
	}

	public PublishResMessage(MStatus status){
		type = MType.PUBLISH_RES;
		this.status = status;
		this.duIds = null;
	}
	
	/** usado nao so para enviar o status bem como os id's com os quais os novos objectos foram registados junto ao servidor **/
	public PublishResMessage(MStatus status, int[] duIds){
		type = MType.PUBLISH_RES;
		this.status = status;
		this.duIds = duIds;
	}
}

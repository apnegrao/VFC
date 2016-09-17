package mobihoc.session.notifications;

import mobihoc.session.Notification;
import mobihoc.session.NotificationType;
import mobihoc.session.client.OpRes;

public class PublishResNotification extends Notification {

	public OpRes status;
	public int[] duIds;
	
	public PublishResNotification()
	{
		super();
		type = NotificationType.PUBLISH_RES_NOTIFICATION;
		this.status = OpRes.OK;	// Estava aqui 0...
	}
	
	public PublishResNotification(OpRes status)
	{
		super();
		type = NotificationType.PUBLISH_RES_NOTIFICATION;
		this.status = status;
		this.duIds = null;
	}

	/** usado para passar informacao sobre o Status de retorno quando se estao a registar novos objectos junto ao servidor bem como os Id's com que ficaram registados **/
	public PublishResNotification(OpRes status, int[] duIds) {
		super();
		type = NotificationType.PUBLISH_RES_NOTIFICATION;
		this.status = status;
		this.duIds = duIds;
	}

}

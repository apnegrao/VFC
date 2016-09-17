package mobihoc.session.notifications;

import mobihoc.session.Notification;
import mobihoc.session.NotificationType;
import mobihoc.session.client.OpRes;

public class SubscribeResNotification extends Notification {

	public OpRes result;
	public String descr;
	
	public SubscribeResNotification()
	{
		super();
		type = NotificationType.SUBSCRIBE_RES_NOTIFICATION;
		result = OpRes.OK;	// Estava ca 0...
		descr = null;
	}
	
	public SubscribeResNotification(OpRes result)
	{
		super();
		type = NotificationType.SUBSCRIBE_RES_NOTIFICATION;
		this.result = result;
		descr = null;
	}
}
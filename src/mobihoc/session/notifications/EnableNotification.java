package mobihoc.session.notifications;

import mobihoc.session.Notification;
import mobihoc.session.NotificationType;
import mobihoc.session.client.OpRes;

public class EnableNotification extends Notification {

	public OpRes status;
	
	public EnableNotification()
	{
		super();
		type = NotificationType.ENABLE_NOTIFICATION;
		this.status = OpRes.OK;
	}
	
	public EnableNotification(OpRes status)
	{
		super();
		type = NotificationType.ENABLE_NOTIFICATION;
		this.status = status;
	}
}
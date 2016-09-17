package mobihoc.session.notifications;

import mobihoc.session.Notification;
import mobihoc.session.NotificationType;

public class DisableNotification extends Notification {

	public int status;
	
	public DisableNotification()
	{
		super();
		type = NotificationType.DISABLE_NOTIFICATION;
		this.status = 0;
	}
	
	public DisableNotification(int status)
	{
		super();
		type = NotificationType.DISABLE_NOTIFICATION;
		this.status = status;
	}
}
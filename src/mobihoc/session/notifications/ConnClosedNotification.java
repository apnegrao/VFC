package mobihoc.session.notifications;

import mobihoc.session.Notification;
import mobihoc.session.NotificationType;

public class ConnClosedNotification extends Notification {

	public int address;
	
	public ConnClosedNotification()
	{
		super();
		type = NotificationType.CONN_CLOSE_NOTIFICATION;
		this.address = 0;
	}
	
	public ConnClosedNotification(int address)
	{
		super();
		type = NotificationType.CONN_CLOSE_NOTIFICATION;
		this.address = address;
	}
}
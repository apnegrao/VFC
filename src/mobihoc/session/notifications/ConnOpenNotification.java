package mobihoc.session.notifications;

import mobihoc.session.Notification;
import mobihoc.session.NotificationType;

public class ConnOpenNotification extends Notification {

	public int address;
	
	public ConnOpenNotification()
	{
		super();
		type = NotificationType.CONN_OPEN_NOTIFICATION;
		this.address = 0;
	}
	
	public ConnOpenNotification(int address)
	{
		super();
		type = NotificationType.CONN_OPEN_NOTIFICATION;
		this.address = address;
	}
}
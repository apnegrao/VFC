package mobihoc.session;

import mobihoc.session.NotificationType;

public class MessageInNotification extends Notification {

	public String msg;
	
	public MessageInNotification()
	{
		super();
		type = NotificationType.MESSAGE_IN_NOTIFICATION;
		this.msg = null;
	}
	
	public MessageInNotification(String msg)
	{
		super();
		type = NotificationType.MESSAGE_IN_NOTIFICATION;
		this.msg = msg;
	}
}
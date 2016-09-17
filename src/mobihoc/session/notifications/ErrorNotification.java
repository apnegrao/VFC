package mobihoc.session.notifications;

import mobihoc.session.Notification;
import mobihoc.session.NotificationType;

public class ErrorNotification extends Notification {

	public String error;
	
	public ErrorNotification(String error) {
		super();
		type = NotificationType.ERROR_NOTIFICATION;
		this.error = error;
	}
}
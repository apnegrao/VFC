package mobihoc.session.notifications;

import mobihoc.session.Notification;
import mobihoc.session.DataUnit;
import mobihoc.session.NotificationType;

public class UpdateNotification extends Notification {

	public DataUnit[] dus;
	
	public UpdateNotification()
	{
		super();
		type = NotificationType.UPDATE_NOTIFICATION;
	}

	public UpdateNotification(DataUnit[] dus){
		super();
		type = NotificationType.UPDATE_NOTIFICATION;
		this.dus = dus;
	}
}
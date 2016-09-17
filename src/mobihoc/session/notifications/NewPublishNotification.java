package mobihoc.session.notifications;

import mobihoc.session.Notification;
import mobihoc.session.DataUnit;
import mobihoc.session.NotificationType;

public class NewPublishNotification extends Notification {

	public DataUnit[] dus;
	
	/*public NewPublishNotification()
	{
		super();
		type = NotificationType.NEW_PUBLISH_NOTIFICATION;
	}*/

	public NewPublishNotification(DataUnit[] dus){
		super();
		type = NotificationType.NEW_PUBLISH_NOTIFICATION;;
		this.dus = dus;
	}

}

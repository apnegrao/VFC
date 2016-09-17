package mobihoc.session.notifications;

import mobihoc.session.Notification;
import mobihoc.session.NotificationType;

import mobihoc.session.DataUnit;

public class LoadStateNotification extends Notification {

	public int[] ids;
	public DataUnit[] dus;
	
	public LoadStateNotification(int[] ids, DataUnit[] dus) {
		super();
		type = NotificationType.LOAD_STATE_NOTIFICATION;
		this.ids = ids;
		this.dus = dus;
	}
}

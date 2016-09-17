package mobihoc.api;

import mobihoc.session.INotificationListener;
import mobihoc.session.MessageInNotification;
import mobihoc.session.Notification;
import mobihoc.session.server.*;
import mobihoc.session.notifications.*;
import mobihoc.network.server.*;

public class MobihocServer implements INotificationListener {

	private SessionServer _body;

	private IMobihocServerListener _listener;
	
	public MobihocServer(IMobihocServerListener listener)
	{
		_body = new SessionServer(this);
		_listener = listener;
	}

	public void open(HostRecord config) {
		_body.requestOpen(1000, config);
	}
	
	public void close() {
		_body.requestClose();
	}

	public void callbackNotificationReceived(Notification notification) {
		System.out.println("[S] Notification received.");
		try {
			switch(notification.type) {
	
			case MESSAGE_IN_NOTIFICATION:
			{
				MessageInNotification n = (MessageInNotification)notification;
				_listener.callbackMessageIn(n.msg);
				break;
			}
	
			case CONN_OPEN_NOTIFICATION:
			{
				ConnOpenNotification n = (ConnOpenNotification)notification;
				_listener.callbackClientPublished(n.address);
				break;
			}
	
			case CONN_CLOSE_NOTIFICATION:
			{
				ConnClosedNotification n = (ConnClosedNotification)notification;
				_listener.callbackClientUnpublished(n.address);
				break;
			}
	
			default:
			}
		} catch (ClassCastException e) {
			System.out.println("[S] Exception casting notification.");
		}
	}
}
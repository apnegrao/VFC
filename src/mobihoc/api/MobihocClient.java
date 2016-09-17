package mobihoc.api;

import java.util.*;

import mobihoc.api.IMobihocClientListener;
import mobihoc.session.client.OpRes;
import mobihoc.session.DataUnit;
import mobihoc.session.UserAgent;
import mobihoc.session.Notification;
import mobihoc.session.INotificationListener;
import mobihoc.session.client.*;
import mobihoc.session.notifications.*;
import mobihoc.session.Phi;
import mobihoc.network.client.ServerRecord;

public class MobihocClient implements INotificationListener {

	private SessionClient _body;
	private IMobihocClientListener _listener;	
	private List<DataUnit> _waitingPublishDu = new ArrayList<DataUnit>();

	private DataUnit[] _ongoingDus = null;
	
	private int _idCounter = 0;
	
	public MobihocClient(IMobihocClientListener listener)
	{
		_listener = listener;
		_body = new SessionClient(this);
	}

	/*
	 * Request methods
	 */

	public boolean reconnect() {
		OpRes status = _body.requestReconnect();
		return status == OpRes.OK;
	}

	public boolean subscribe(ServerRecord sr, UserAgent ua) {
		OpRes status = _body.requestSubscribe(sr, ua);
		return status == OpRes.OK;
	}

	public boolean publish(DataUnit[] data) {
		_ongoingDus = data;
		return _body.requestPublish(data) == OpRes.OK;
	}
	
	public boolean publish(Collection<DataUnit> data) {
		return publish(data.toArray(new DataUnit[]{}));
	}
	
	public boolean publish(DataUnit data) {
		return publish(new DataUnit[] {data});
	}

	public boolean enable() {
		return _body.requestEnable() == OpRes.OK;
	}
	
	public boolean disable() {
		return _body.requestDisable() == OpRes.OK;
	}
	
	public boolean write(DataUnit[] updates) {
		return _body.requestWrite(updates) == OpRes.OK;
	}
	
	public boolean write(Collection<DataUnit> updates) {
		return write(updates.toArray(new DataUnit[]{}));
	}
	
	public boolean write(DataUnit update) {
		return write(new DataUnit[] {update});
	}

	public boolean sendOutOfBand(String msg) {
		return _body.requestSendOutOfBand(msg) == OpRes.OK;
	}

	public boolean save(String filename) {
		return _body.requestSaveState(filename) == OpRes.OK;
	}
	
	public boolean load(String filename) {
		return _body.requestLoadState(filename) == OpRes.OK;
	}
	
	public boolean sendPhi(Phi phi) {
		return _body.requestSetControlParam(phi) == OpRes.OK;
	}
	
	public int getNextId() {
		return _idCounter++;
	}
	
	public boolean tryPublish(Collection<DataUnit> data) {
		int times = 0;
		while (publish(data) == false) {
			if (times == 10) {
				System.out.println("Gave up on publishing");
				return false;
			}
			times++;
			System.out.println("Publish failed, sleeping for 150ms");
			try { Thread.sleep(150); } catch (InterruptedException e) { }
		}
		return true;
	}

	public boolean tryPublish(DataUnit data) {
		List<DataUnit> lst = new ArrayList<DataUnit>();
		lst.add(data);
		return tryPublish(lst);
	}
	
	public void addDelayedPublish(DataUnit data) {
		_waitingPublishDu.add(data);
	}
	
	public boolean flushDelayedPublish() {
		boolean ret = tryPublish(_waitingPublishDu);
		_waitingPublishDu.clear();
		return ret;
	}

	/*
	 * Callback methods
	 */
	
	public void callbackNotificationReceived(Notification notification) {
		System.out.println("[C] Notification received.");

		switch(notification.type) {

		case SUBSCRIBE_RES_NOTIFICATION:
		{
			SubscribeResNotification notif = (SubscribeResNotification)notification;
			if (notif.result == OpRes.OK) {
				_listener.callbackSubscribeResult(true, "Subscription succeeded.");
			} else {
				_listener.callbackSubscribeResult(false, "Subscription failed.");				
			}
			break;
		}

		case PUBLISH_RES_NOTIFICATION:
		{
			PublishResNotification notif = (PublishResNotification)notification;
			if (notif.status == OpRes.OK) {
				System.out.println("[C] Publish: notification status ok.");
				_listener.callbackPublishResult(true, _ongoingDus, notif.duIds);
			} else {
				_listener.callbackPublishResult(false, _ongoingDus, null);				
				System.out.println("[C] Publish: notification status ko.");
			}
			_ongoingDus = null;
			break;
		}
		
		case ENABLE_NOTIFICATION:
		{
			EnableNotification notif = (EnableNotification)notification;
			_listener.callbackEnable(notif.status == OpRes.OK);
			break;
		}
		
		case DISABLE_NOTIFICATION:
		{
			_listener.callbackDisable();
			break;
		}

		case UPDATE_NOTIFICATION:
		{
			UpdateNotification notif = (UpdateNotification)notification;
			_listener.callbackUpdateState(notif.dus);
			break;
		}
		
		case NEW_PUBLISH_NOTIFICATION:
		{
			NewPublishNotification notif = (NewPublishNotification)notification;
			_listener.callbackNewData(notif.dus);
			break;
		}
		
		case LOAD_STATE_NOTIFICATION:
		{
			LoadStateNotification notif = (LoadStateNotification)notification;
			_listener.callbackLoadState(notif.ids, notif.dus);
			break;
		}
		
		case ERROR_NOTIFICATION:
		{
			ErrorNotification notif = (ErrorNotification)notification;
			_listener.callbackError(notif.error);
			break;
		}
		
		case CONN_CLOSE_NOTIFICATION:
		{
			ConnClosedNotification notif = (ConnClosedNotification)notification;
			_listener.callbackConnClosed();
			break;
		}
		
		default:
			System.out.println("[C] Error?: Notification " + notification.type + " not handled yet.");

		}
	}
}
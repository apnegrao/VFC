package mobihoc.session.client;

import java.util.*;

import mobihoc.network.client.*;
import mobihoc.session.*;
import mobihoc.session.messages.*;
import mobihoc.session.notifications.*;
import mobihoc.session.Phi;

public class SessionClient implements IConnectionListener
{
	
	private enum ClientState {
		IDLE,
		SUBSCRIBING,
		SUBSCRIBED,
		PUBLISHING,
		RECONNECTING,
		ACTIVE
	}

	private INotificationListener _notifListener = null;

	private ClientState _state;
	private ClientState _prevState;
	private Connection _conn = null;

	private UserAgent _userAgent;
	private ServerRecord _sr;

	public SessionClient(INotificationListener notifListener) {
		_notifListener = notifListener;
		_conn = null;
		_state = ClientState.IDLE;
		_prevState = ClientState.IDLE;
	}

	/*
	 * Service requests
	 */
	
	public synchronized OpRes requestReconnect() {
		if (_state == ClientState.IDLE){
			if (_sr == null || _userAgent == null) {
				return OpRes.INVALID_ARGS;
			}
			_conn = new Connection(this);
			_conn.connect(_sr);
			_state = ClientState.RECONNECTING;
			return OpRes.OK;
		}

		return OpRes.NOT_ALLOWED;
	}


	public synchronized OpRes requestSubscribe(ServerRecord sr, UserAgent ua) {
		if (_state == ClientState.IDLE){
			if (sr == null || ua == null) {
				return OpRes.INVALID_ARGS;
			}
			_userAgent = ua;
			_sr = sr;
			_conn = new Connection(this);
			_conn.connect(sr);
			_state = ClientState.SUBSCRIBING;
			System.out.println("[C] Subscribing: connecting to server.");
			return OpRes.OK;
		}

		return OpRes.NOT_ALLOWED;
	}

	public synchronized OpRes requestPublish(DataUnit[] data){
		if (_state == ClientState.SUBSCRIBED) {
			if (_conn.send(new PublishReqMessage(data))) {
				_state = ClientState.PUBLISHING;
				System.out.println("[C] Publishing: sending message.");
				return OpRes.OK;
			} else {
				shutdown();
				System.out.println("[C] Publishing: unable to send message.");
				return OpRes.UNABLE_TO_SEND_MSG;
			}
		}
		return OpRes.NOT_ALLOWED;
	}
	
	public synchronized OpRes requestEnable() {
		if (_state == ClientState.SUBSCRIBED) {
			if (_conn.send(new EnableMessage())) {
				System.out.println("[C] Enable: sending message.");
				return OpRes.OK;
			} else {
				shutdown();
				System.out.println("[C] Enable: unable to send message.");
				return OpRes.UNABLE_TO_SEND_MSG;
			}
		}
		return OpRes.NOT_ALLOWED;
	}
	
	public synchronized OpRes requestDisable() {
		if (_state == ClientState.ACTIVE){
			if (_conn.send(new DisableMessage())) {
				System.out.println("[C] Disable: sending message.");
				return OpRes.OK;
			} else {
				shutdown();
				System.out.println("[C] Disable: unable to send message.");
				return OpRes.UNABLE_TO_SEND_MSG;
			}
		}
		return OpRes.NOT_ALLOWED;
	}

	public synchronized OpRes requestSendOutOfBand(String msg) {
		if (_state != ClientState.IDLE && _state != ClientState.SUBSCRIBING) {
			_conn.send(new OutOfBandMessage(msg));
			return OpRes.OK;
		}
		return OpRes.NOT_ALLOWED;
	}

	public synchronized OpRes requestWrite(DataUnit[] updates) {
		if (_state == ClientState.ACTIVE) {
			_conn.send(new WriteMessage(updates));
			return OpRes.OK;
		}
		return OpRes.NOT_ALLOWED;
	}

	public synchronized OpRes requestSaveState(String filename) {
		if (_state == ClientState.ACTIVE) {
			_conn.send(new SaveStateReqMessage(filename));
			return OpRes.OK;
		}
		_notifListener.callbackNotificationReceived(new ErrorNotification("Save: Couldt'n save game because it hasn't started yet."));
		return OpRes.NOT_ALLOWED;
	}

	public synchronized OpRes requestLoadState(String filename) {
		//if (_state == ClientState.ACTIVE) {
			SaveState ss = SaveState.buildSaveState(filename);
			_conn.send(new LoadStateReqMessage(ss));
			return OpRes.OK;
		/*}
		return OpRes.NOT_ALLOWED;*/
	}
	
	public synchronized OpRes requestSetControlParam(Phi phi){
		if (_state == ClientState.SUBSCRIBED) {
			_conn.send(new SetControlParamReqMessage(phi));
			return OpRes.OK;
		}
		return OpRes.NOT_ALLOWED;
	}

	/*
	 * Connection handlers
	 */

	public void callbackConnectionStatus(Connection.Status status) {
		if (status == Connection.Status.CONNECTED) {
			handleInConnectionUp();
		} else {
			handleInConnectionDown(status);
		}
	}

	public synchronized void handleInConnectionUp() {
		if(_state == ClientState.SUBSCRIBING) {
			if (_conn.send(new SubscribeReqMessage(_userAgent))) {
				System.out.println("[C] Subscribing: sending message.");
				return;
			} else {
				shutdown();
				_notifListener.callbackNotificationReceived(
						new SubscribeResNotification(OpRes.UNABLE_TO_SEND_MSG));
				System.out.println("[C] Subscribing: unable to send message.");
				return;
			}
		} else if(_state == ClientState.RECONNECTING) {
			if (_conn.send(new SubscribeReqMessage(_userAgent))) {
				System.out.println("[C] Reconnecting: sending message.");
				return;
			} else {
				shutdown();
				_notifListener.callbackNotificationReceived(
						new ErrorNotification("Unable to reconnect to server."));
				System.out.println("[C] Reconnecting: unable to send message.");
				return;
			}
		}

		System.out.println("[C] Subscribe: operation not allowed.");
	}

	public synchronized void handleInConnectionDown(Connection.Status result) {
		shutdown();
		
		if(_state == ClientState.SUBSCRIBING) {
			_notifListener.callbackNotificationReceived(
					new SubscribeResNotification(OpRes.CONNECTION_CLOSED));
			System.out.println("[C] Subscribing: connection closed.");
			return;
		}
		
		if(_state == ClientState.PUBLISHING) {
			_notifListener.callbackNotificationReceived(
				new PublishResNotification(OpRes.CONNECTION_CLOSED));
			System.out.println("[C] Publishing: connection closed.");
			return;
		}
	}


	/*
	 * Server message handlers
	 */

	public void callbackMsgReceived(Connection net, Object data) {
		Message msg = null;
		try {
			msg = (Message)data;
		} catch(Exception e) {
			System.out.println("[C] Object is not a message object.");
			System.out.println("[C] 	Dump:" + data);
			System.out.println("[C] 	Class:" + data.getClass().getName());
			e.printStackTrace();
			return;
		}

		try {
			switch(msg.type) {
	
			case SUBSCRIBE_RES:
				handleSrvSubscribeRes((SubscribeResMessage)msg);
				break;
				
			case PUBLISH_RES:
				handleSrvPublishRes((PublishResMessage)msg);
				break;
				
			case PUBLISH_ALL:
				handleSrvPublishAll((PublishAllMessage)msg);
				break;
				
			case ENABLE_ALL:
				handleSrvEnableAll((EnableAllMessage)msg);
				break;
			
			case ROUND_ALL:
				handleSrvRoundAll((RoundAllMessage)msg);
				break;
			
			case DISABLE_ALL:
				handleSrvDisableAll((DisableAllMessage)msg);
				break;
				
			case SAVE_STATE_RES:
				handleSrvSaveStateRes((SaveStateResMessage)msg);
				break;
				
			case LOAD_STATE_RES:
				handleSrvLoadStateRes((LoadStateResMessage)msg);
				break;
			
			case SET_CONTROL_PARAM_RES:
				handleSrvSetControlParamRes((SetControlParamResMessage)msg);
				break;
			case ERROR:
				handleError((ErrorMessage)msg);
				break;
			}
		} catch (Exception e) {
			System.out.println("[C] Unable to cast to the specific message type.");
			e.printStackTrace();
		}
	}

	public synchronized void handleSrvSubscribeRes(SubscribeResMessage msgIn){
		if(_state == ClientState.SUBSCRIBING) {
			if (msgIn.status == Message.MStatus.OK) {
				_state = ClientState.SUBSCRIBED;
				_notifListener.callbackNotificationReceived(
						new SubscribeResNotification(OpRes.OK));
			} else {
				shutdown();
				_notifListener.callbackNotificationReceived(
						new SubscribeResNotification(OpRes.REMOTE_ERROR));
			}
			return;
		}
		else if(_state == ClientState.RECONNECTING) {
			if (msgIn.status == Message.MStatus.OK) {
				_state = _prevState;
				_conn.send(new ReadMessage());
			} else {
				shutdown();
			}
			return;
		}

		System.out.println("[C] Subscribe: response not allowed. " + _state);
	}
	
	/** metodo chamado para tratar da informação retornada por parte do servidor quando são registados novos objectos junto a este - a msg contem o Status bem como os Id's que foram atribuidos aos objectos junto ao servidor **/
	public synchronized void handleSrvPublishRes(PublishResMessage msgIn) {
		if(_state == ClientState.PUBLISHING) {
			_state = ClientState.SUBSCRIBED;
			if (msgIn.status == Message.MStatus.OK) {
				System.out.println("[C] handleSrvPublishRes - received ack from the server for the newly registered objects");
				_notifListener.callbackNotificationReceived(
						new PublishResNotification(OpRes.OK, msgIn.duIds));
			} else {
				shutdown();
				_notifListener.callbackNotificationReceived(
						new PublishResNotification(OpRes.REMOTE_ERROR));
			}
			return;
		}
		System.out.println("[C] Publish: response not allowed.");
	}

	public synchronized void handleSrvPublishAll(PublishAllMessage msgIn) {
		if(_state == ClientState.SUBSCRIBED) {
			System.out.println("[C] handleSrvPublishAll - receiving newly registered dataUnits from server");
			DataUnit[] dus = msgIn.getDataUnits();
			System.out.println("[C] - got " + dus.length + " data units in the message");
			
			_notifListener.callbackNotificationReceived(
					new NewPublishNotification(msgIn.getDataUnits()));
			return;
		}
		System.out.println("[C] SrvPublishAll: not allowed in current state.");
	}

	public synchronized void handleSrvEnableAll(EnableAllMessage msgIn) {
		if(_state == ClientState.SUBSCRIBED) {
			if (msgIn.status == Message.MStatus.OK) {
				_state = ClientState.ACTIVE;
				_notifListener.callbackNotificationReceived(
						new EnableNotification(OpRes.OK));
			} else {
				shutdown();
				_notifListener.callbackNotificationReceived(
						new EnableNotification(OpRes.REMOTE_ERROR));
			}
			return;
		}
		System.out.println("[C] Enable: message discarded.");
	}
	
	/** method called when the server sends the list of new updates to the client **/
	public synchronized void handleSrvRoundAll(RoundAllMessage msgIn) {
		if(_state == ClientState.ACTIVE) {
			DataUnit[] dus = null;
			try {
				dus = msgIn.getDataUnits();
				if (dus != null && dus.length > 0) {
					System.out.println("[C] Round ("+msgIn.tickno+")");
					DataUnit.printSet("[C]     Updates->",dus);
					System.out.println();
					_notifListener.callbackNotificationReceived(new UpdateNotification(dus));
				}				
			} catch (ClassCastException e) {
				System.out.println("[C] Round (" + msgIn.tickno + ") - error.");
				return;
			}
			return;
		}
		System.out.println("[C] Round: message discarded.");
	}
	
	public synchronized void handleSrvDisableAll(DisableAllMessage msgIn) {
		if(_state == ClientState.ACTIVE) {
			shutdown();
			_notifListener.callbackNotificationReceived(
					new DisableNotification());
			return;
		}
		System.out.println("[C] Disable: message discarded.");
	}

	public synchronized void handleSrvSaveStateRes(SaveStateResMessage msgIn) {
		if(_state == ClientState.ACTIVE) {
			msgIn.getSave().flush();
			/*SaveState ss = SaveState.buildSaveState(msgIn.getSave().getSaveName());
			ss.printComparation(msgIn.getSave());*/
		}
	}

	public synchronized void handleSrvLoadStateRes(LoadStateResMessage msgIn) {
		if(!(_state == ClientState.ACTIVE)) {
			System.out.println("[C] handleSrvLoadStateRes - received state update");
			// Alterar os ids dos meus objectos locais
			List<Integer> myIds = new ArrayList<Integer>();
			for (Integer duId : msgIn.getDuAssociation().keySet()) {
				if(msgIn.getDuAssociation().get(duId).equals(_userAgent)) {
					// É meu
					myIds.add(duId);
				}
			}
			System.out.println("Os meus novos IDs : " + myIds);
			int[] duIds = new int[myIds.size()];
			int i = 0;
			for (Integer id : myIds) {
				duIds[i] = id.intValue();
				i++;
			}
			// Propagar para a aplicação
			_notifListener.callbackNotificationReceived(new LoadStateNotification(duIds, msgIn.getDataUnits()));
		}
	}
	
	public synchronized void handleSrvSetControlParamRes(SetControlParamResMessage msgIn) {
		if(_state == ClientState.SUBSCRIBED) {
			System.out.println("[C] handleSrvSetControlParamRes - received confirmation for new Phi");
			
			return;
		}
		
		return;
	}

	public synchronized void handleError(ErrorMessage msgIn) {
		_notifListener.callbackNotificationReceived(new ErrorNotification(msgIn.getError()));
		return;
	}

	private void shutdown() {
		if (_state != ClientState.RECONNECTING) _prevState = _state;
		_state = ClientState.IDLE;
		_conn.disconnect();
		_notifListener.callbackNotificationReceived(new ConnClosedNotification());
		System.out.println("[C] Client shutdown.");
	}
}
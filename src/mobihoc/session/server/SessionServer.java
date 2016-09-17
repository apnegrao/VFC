package mobihoc.session.server;

import java.util.*;

import mobihoc.Mobihoc;
import mobihoc.network.server.*;
import mobihoc.session.DataPool;
import mobihoc.session.DataUnit;
import mobihoc.session.SaveState;
import mobihoc.session.UserAgent;
import mobihoc.session.INotificationListener;
import mobihoc.session.Message;
import mobihoc.session.MessageInNotification;
import mobihoc.session.messages.*;
import mobihoc.session.notifications.*;
import mobihoc.session.server.cmb.BasicCMB;
import mobihoc.session.server.cmb.FullCMB;
import mobihoc.session.Phi;

public class SessionServer implements INetworkListener, IRoundListener {

	private enum ServerState {
		IDLE,
		SETUP,
		ACTIVE
	}

	private enum ServerStatus {
		OK,
		INTERNAL_STATE,
		OP_NOT_ALLOWED
	}
	
	private ServerState _state;
	private DataPool _pool;
//	private UpdateQueue _uq;
	private NetworkManager _net;
	private RoundTrigger _trigger;
	private INotificationListener _notifListener;
	private CMB _cmb;
	private int _period;
	private Map<UserAgent, Phi> _phi;
	private Phi _defaultPhi;
	//the key is the ID of the DU and the value is the UserAgent that registered it
	private Map<Integer,UserAgent> _duAssociation;
	// the key is the Address and the value is the UserAgent
	private Map<Integer, UserAgent> _registeredUsers;
	//
	private boolean _stateLoaded;

	public SessionServer(INotificationListener notifListener) {
		_state = ServerState.IDLE;
		_notifListener = notifListener;
		_trigger = new RoundTrigger(this);
		_pool = new DataPool();
		_cmb = null;
		//Setting up the "default" phi
		_defaultPhi = new Phi();
		_phi = new TreeMap<UserAgent, Phi>();
		_duAssociation = new TreeMap<Integer,UserAgent>();
		_registeredUsers = new TreeMap<Integer, UserAgent>();
		_period = 1000; // 1 second
		_stateLoaded = false;
	}


	/*
	 * Service: Open
	 */
	
	/** 
	resets/sets the NetworkManager of the SessionServer
	resets/sets the time interval between Rounds 
	**/
	public synchronized ServerStatus requestOpen(int roundPeriod, HostRecord config) {
		if (stateInvalid()) {
			shutdown();
			return ServerStatus.INTERNAL_STATE;
		}
		if(_state == ServerState.IDLE) {
			_period = roundPeriod;
			_net = new NetworkManager(this);
			_net.enable(config);
			_state = ServerState.SETUP;
			return ServerStatus.OK;
		}
		return ServerStatus.OP_NOT_ALLOWED;
	}


	/*
	 * Service: Close
	 */

	public synchronized ServerStatus requestClose() {
		if (stateInvalid()) {
			shutdown();
			return ServerStatus.INTERNAL_STATE;
		}
		if(_state == ServerState.SETUP) {
			_net.disable();
			_state = ServerState.IDLE;
			return ServerStatus.OK;
		}
		if(_state == ServerState.ACTIVE) {
			_trigger.disable();
			_net.disable();
			_state = ServerState.IDLE;
			return ServerStatus.OK;
		}
		return ServerStatus.OP_NOT_ALLOWED;
	}

	
	/*
	 * Client remote requests
	 */

	public void callbackMsgReceived(INetworkServices net, int address, Object data)	{
		Message msg = null;
		try {
			msg = (Message)data;
		} catch(ClassCastException e) {//initially catching Exception. changed to ClassCastException
			System.out.println("[S] Object is not a message object.");
			e.printStackTrace();
			return;
		}

		try {
			switch(msg.type) {
	
			case SUBSCRIBE_REQ:
				handleCliSubscribeReq((SubscribeReqMessage)msg, address);
				break;
			
			case PUBLISH_REQ:
				handleCliPublishReq((PublishReqMessage)msg, address);
				break;
				
			case SET_CONTROL_PARAM_REQ:
				handleCliSetControlParamReq((SetControlParamReqMessage)msg, address);
				break;
				
			case ENABLE:
				handleCliEnable((EnableMessage)msg);
				break;
				
			case WRITE:
				handleCliWrite((WriteMessage)msg, address);
				break;
				
			case DISABLE:
				handleCliDisable((DisableMessage)msg);
				break;
				
			case OUT_OF_BAND:
				handleCliOutOfBandMessage((OutOfBandMessage)msg, address);
				break;
				
			case SAVE_STATE_REQ:
				handleCliSaveStateReq((SaveStateReqMessage)msg, address);
				break;
				
			case LOAD_STATE_REQ:
				handleCliLoadStateReq((LoadStateReqMessage)msg, address);
				break;
			case READ:
				handleCliRead((ReadMessage)msg, address);
				break;
			}
		} catch (ClassCastException e) {
			System.out.println("[S] Unable to cast to the specific message type.");
			e.printStackTrace();
		}
	}

	/** Permite o registo de um novo cliente junto ao SessionServer,
	* apenas se o servidor estiver no estado de SETUP ou ACTIVE.
	* Caso o cliente ja esteja registado ou tenha sido feito um load de um estado que nao o contem, é negado o registo.
	**/
	public synchronized void handleCliSubscribeReq(SubscribeReqMessage msg, int address) {
		if (stateInvalid()) {
			shutdown();
			return;
		}

		UserAgent newUA = msg.getUserAgent();
		
		if (newUA == null) {
			System.out.println("UserAgent é null, não vamos permitir o registo");
			_net.send(address, new SubscribeResMessage(Message.MStatus.OP_NOT_ALLOWED));
			return;
		}

		// Impedir que entre duas vezes o mesmo UserAgent, independentemente do estado
		for (UserAgent ua : _registeredUsers.values()) {
			if (ua.compareTo(newUA) == 0) {
				System.out.println(ua.toString() + " já existe, não vamos permitir");
				_net.send(address, new SubscribeResMessage(Message.MStatus.OP_NOT_ALLOWED));
				return;
			}
		}

		// Se tiver sido feito load mas o UserAgent não fazia parte do load, nega-se o registo
		if(_stateLoaded && !_duAssociation.containsValue(newUA)) {
			System.out.println("[S] Client Agent " + newUA + " does not exist in the current loaded state.");
			_net.send(address, new SubscribeResMessage(Message.MStatus.OP_NOT_ALLOWED));
			return;
		}

		if ((_state == ServerState.SETUP) || ((_state == ServerState.ACTIVE) && _duAssociation.containsValue(newUA))) {

			_registeredUsers.put(new Integer(address), newUA);
			
			
			if (!_phi.containsKey(newUA)) {
				System.out.println("[S] Adding phi to client " + newUA);
				_phi.put(newUA, _defaultPhi.clone(address));//adicionar o defaultPhi como politica de consistencia, se o cliente quiser ter um diferente, que o mande depois de estar registado
			}
			
			_net.send(address, new SubscribeResMessage(Message.MStatus.OK,address));
			System.out.println("[S] Subscribe: client " + address + " accepted.");
			return;
		}
		_net.send(address, new SubscribeResMessage(Message.MStatus.OP_NOT_ALLOWED));
		System.out.println("[S] Subscribe: client " + address + " not accepted.");
	}

	/** Verifica se um cliente está a tentar publicar objectos mas ja o tinha feito antes.
	* Tal situacao pode ocorrer apos um load de outro cliente ou
	* apos o cliente crashar e voltar a ligar-se ao servidor com o jogo em funcionamento.
	* Retorna true se foi enviada info ao cliente, false caso contrario.
	**/
	private boolean handlePossibleLoad(PublishReqMessage msg, int address) {
		if(_duAssociation.containsValue(_registeredUsers.get(new Integer(address)))) {
			// Ja cá tenho este user, deve ter sido feito um load.
			// Enviar-lhe info do estado
			System.out.println("O user " + _registeredUsers.get(new Integer(address)) + " esta a tentar publicar objectos, mas eu ja ca os tenho.");
			
			// Cada cliente tenta publicar agora o mesmo numero de objectos que tinha quando foi feito o save
			int[] preExistingIds = new int[msg.getDataUnits().length];
			int i = 0;
			UserAgent ua = _registeredUsers.get(new Integer(address));
			for (Integer valueDU : _duAssociation.keySet()) {
				if (ua.compareTo(_duAssociation.get(valueDU)) == 0) {
					preExistingIds[i] = valueDU.intValue();
					i++;
				}
			}
			// Enviar-lhe os ids dos objectos antigos do cliente
			_net.send(address, new PublishResMessage(Message.MStatus.OK, preExistingIds));
			
			sendLocalInfo(address);
			return true;
		}
		return false;
	}


	private synchronized void sendLocalInfo(int address) {
		DataUnit[] preExistingDataUnits;
		if (_pool.isFrozen()) {
			preExistingDataUnits = new DataUnit[_duAssociation.size()];
			int j = 0;
			for (Integer du : _duAssociation.keySet()) {
				preExistingDataUnits[j] = _pool.getRef(du.intValue());
				j++;
			}
		} else {
			preExistingDataUnits = _pool.getContents();
		}

		// Enviar-lhe os seus objectos juntamente com os demais presentes no estado lido 
		_net.send(address, new LoadStateResMessage(preExistingDataUnits, _duAssociation));

		if (_state == ServerState.ACTIVE) {
			// cliente caiu e esta a tentar ligar-se
			_net.send(address, new EnableAllMessage(Message.MStatus.OK));
		}
	}

	private synchronized void handleCliRead(ReadMessage msg, int address) {
		sendLocalInfo(address);
	}

	private synchronized DataUnit[] sanitizeMsgDus(DataUnit[] inDus) {
		ArrayList<DataUnit> listDU = new ArrayList<DataUnit>();
		int pos = 0;
		for (DataUnit du : inDus) {
			if (du != null) listDU.add(du);
			else System.out.println("[S] Discarded DataUnit at position " + pos + " because it's null");
			pos++;
		}
			
		if (listDU.size() == 0) return null;
			
		DataUnit[] dataUnits = new DataUnit[listDU.size()];
		for (int i = 0; i < dataUnits.length; i++) {
			dataUnits[i] = listDU.get(i);
		}
		return dataUnits;
	}


	/** permite que sejam registados novos objectos para serem geridos por parte do SessionServer, no caso desse estar no estado STATUS_SETUP **/
	public synchronized void handleCliPublishReq(PublishReqMessage msg, int address) {
		if (stateInvalid()) {
			shutdown();
			return;
		}
		
		boolean loaded = handlePossibleLoad(msg, address);

		if (loaded) return;
		
		if (_state == ServerState.SETUP) {
			DataUnit[] preExistingDataUnits = _pool.getContents();

			DataUnit[] dataUnits = sanitizeMsgDus(msg.getDataUnits());
			
			if (dataUnits == null) {
				System.out.println("[S] Publish: Will not publish an empty array of valid DataUnits.");
				_net.send(address, new PublishResMessage(Message.MStatus.OP_NOT_ALLOWED));
				return;
			}

			int[] result = _pool.register(dataUnits);

			for (int i = 0; i < dataUnits.length; i++) {
				// we're going to add this DU to the list of pivots for the considered phi
				if (dataUnits[i].isPivot()) _phi.get(_registeredUsers.get(new Integer(address))).addPivot(result[i]);
			}

			System.out.println("[S] handleCliPublishReq - registered " + result.length + " new dataUnitd from client " + address);
			
			_pool.dump();
			
			_net.send(address, new PublishResMessage(Message.MStatus.OK, result));
			System.out.println("[S] Publish: response sent to client.");
			
			for(int i = 0; i < dataUnits.length; i++) {
				dataUnits[i].setId(result[i]);
				_duAssociation.put(new Integer(result[i]), _registeredUsers.get(new Integer(address))); 
			}
			System.out.println("[S] The DU associations after this registrations are:");
			printAssociations();
			
			
			_net.broadcastExcept(address, new PublishAllMessage(dataUnits));
			System.out.println("[S] Publish: response sent to others.");
			
			//send any pre-existing dataUnits to the client only after it has received the id's which its DU's have been assigned by the server
			if (preExistingDataUnits.length != 0) {
				_net.send(address, new PublishAllMessage(preExistingDataUnits));
			}
			
			System.out.println("[S] Publish: ok ");
			return;
		}
		_net.send(address, new PublishResMessage(Message.MStatus.OP_NOT_ALLOWED));
		System.out.println("[S] Publish: operation not allowed.");
	}
	
	
	/** 
	permite que seja registado um novo conjunto de parametros de controlo junto do SessionServer, no caso desse estar no estado STATUS_SETUP 
	tem que ser na fase de SETUP para que o conjunto de parametros possa ser passado ao CMB 
	**/
	public synchronized void handleCliSetControlParamReq(SetControlParamReqMessage msg, int address) {
		if (stateInvalid()) {
			shutdown();
			return;
		}
		if (_state == ServerState.SETUP) {
			Phi newPhi = msg.getPhi();
			if (newPhi == null) {
				//ignore
				return;
			}
			
			UserAgent ua = _registeredUsers.get(new Integer(address));
			
			//we dont want to let the client to overwrite its own phi after it has registered its objects
			if (_duAssociation.containsValue(ua)) return;
			
			_phi.put(ua, newPhi);
			_net.send(address, new SetControlParamResMessage());
			
			System.out.println("[S] Setting of consistency parameters for client: " + address);
			return;
		}
		//FIXME: Isto se descomentado, da bronca no server...
		//_net.send(address, new PublishResMessage(Message.MStatus.OP_NOT_ALLOWED));
		System.out.println("[S] Setting of consistency parameters: operation not allowed.");
	}
	
	
	/**
	"activates" server 
	freezes the DataPool (impossible to add new DataUnits to the Pool while in frozen state) 
	instanciates a CMB, passing the DataPool and the addresses of the clients contained in the RegisteredUsers Map
	starts the RoundTrigger
	**/
	public synchronized void handleCliEnable(EnableMessage msg)	{
		if (stateInvalid()) {
			shutdown();
			return;
		}
		if (_state == ServerState.SETUP) {
			try {
				_pool.freeze();
				
				_cmb = new FullCMB(_pool, _phi.keySet(), _phi);
				
				System.out.println("[S] Enable: pool and CMB configured.");
			} catch(Exception e) {
				System.out.println("[S] Enable: cannot freeze pool." + e + " and the stack is:\n");
				e.printStackTrace();
				return;
			}
			_net.broadcast(new EnableAllMessage(Message.MStatus.OK));
			_trigger.enable(_period);
			_state = ServerState.ACTIVE;
			System.out.println("[S] Enable: server enabled.");
			return;
		}
		System.out.println("[S] Enable: operation not allowed.");
	}

	/** deactivates the SessionServer **/
	public synchronized void handleCliDisable(DisableMessage msgIn) {
		if (stateInvalid()) {
			shutdown();
			return;
		}
		if (_state == ServerState.ACTIVE) {
			_state = ServerState.IDLE;
			_trigger.disable();
			_net.broadcast(new DisableAllMessage());
			System.out.println("[S] Disable: server disabled.");
			return;
		}
		System.out.println("[S] Disable: operation not allowed.");
	}

	/**
	allows the state of the objects in the DataPool to be updated with the information contained in the message
	only possible while in the STATE_ACTIVE state
	**/
	public synchronized void handleCliWrite(WriteMessage msg, 
			int address) {
		if (stateInvalid()) {
			shutdown();
			return;
		}
		if (_state == ServerState.ACTIVE) {
			DataUnit[] updates = sanitizeMsgDus(msg.getDataUnits());
			
			if (updates == null) {
				System.out.println("[S] Write: Will not write an empty array of valid DataUnits updates.");
				return;
			}

			System.out.println("We got DU update with ID: " + updates[0].getId() + " from client : " + address);
			_cmb.updatesReceived(updates, _registeredUsers.get(address));
			
			DataUnit.printSet("[S] Write: received ->", updates);
			System.out.println();
			
			Mobihoc.log("[S] Write received.");
			return;
		}

		System.out.println("[S] Write: operation not allowed.");
	}

	/**
	Constructs a SaveState upon request
	**/
	public synchronized void handleCliSaveStateReq(SaveStateReqMessage msg, 
			int address) {
		if (stateInvalid()) {
			shutdown();
			return;
		}
		if (_state == ServerState.ACTIVE) {
			String saveName = msg.getSaveName();

			System.out.println("We got a save request from client with ID: " + address);

			SaveState ss = new SaveState(_pool, _duAssociation, _phi, saveName);
			
			_net.send(address, new SaveStateResMessage(Message.MStatus.OK, ss));
			
			return;
		}
		_net.send(address, new ErrorMessage("Save State: Can't save an unstarted game."));
		System.out.println("[S] Save State: operation not allowed.");
	}

	/**
	Restores a previously saved state
	**/
	public synchronized void handleCliLoadStateReq(LoadStateReqMessage msg, 
			int address) {
		if (stateInvalid()) {
			shutdown();
			return;
		}
		if (_state == ServerState.SETUP) {
			SaveState ss = msg.getSaveState();

			if (ss == null) {
				System.out.println("Load State: operation not allowed. Can't load null save state");
				_net.send(address, new ErrorMessage("Load State: Invalid save state received"));
				return;
			}

			Map<Integer, UserAgent> newDuAssociation = ss.getDuAssociation();
			for (UserAgent u : _registeredUsers.values()) {
				if (!newDuAssociation.containsValue(u)) {
					// Ja ha clientes ligados, que nao estavam no save
					System.out.println("Load State: operation not allowed. Actual clients mismatch the ones in the save");
					_net.send(address, new ErrorMessage("Load State: Registered clients mismatch the ones in the save"));
					return;
				}
			}

			_pool = ss.getDataPool();
			_duAssociation = ss.getDuAssociation();
			_phi = ss.getPhi();
			_stateLoaded = true;
			// Mandar info a possiveis users ja registados e ao que pediu o load
			DataUnit[] duState = new DataUnit[_pool.count()];
			int i  = 0;
			for (Integer du : _duAssociation.keySet()) {
				duState[i] = _pool.getRef(du.intValue());
				i++;
			}
			_net.broadcast(new LoadStateResMessage(duState, _duAssociation));
			
			System.out.println("[S] Load State: operation allowed. " + _duAssociation);
			
			return;
		}
		_net.send(address, new ErrorMessage("Load State: Can't load over an already started game"));
		System.out.println("[S] Load State: operation not allowed. State: " + _state);
	}

	public synchronized void handleCliOutOfBandMessage(OutOfBandMessage msg, 
			int address) {
		if (stateInvalid()) {
			shutdown();
			return;
		}
		if (_state == ServerState.ACTIVE || _state == ServerState.SETUP) {
			_notifListener.callbackNotificationReceived(
					new MessageInNotification(msg.msg));
			System.out.println("[S] Message OOB: message received.");
			return;
		}
		System.out.println("[S] Message OOB: operation not allowed.");
	}


	/*
	 * Round handler
	 */
	/** called when the local RoundTrigger generates a new Round event **/
	public synchronized void callbackRoundTriggered(RoundTrigger trigger, long period, long tickno) {
		if (stateInvalid()) {
			shutdown();
			return;
		}
		if (_state == ServerState.ACTIVE) {
			Map<UserAgent, List<DataUnit>> cliupdates = _cmb.computeUpdatesToDiffuse(tickno);
			if (cliupdates != null) {
				Map<UserAgent, Integer> reverseRegisteredUsers = new TreeMap<UserAgent, Integer>();
				for (Integer i : _registeredUsers.keySet()) {
					reverseRegisteredUsers.put(_registeredUsers.get(i), i);
				}

				for (UserAgent c : cliupdates.keySet()) {
					if (cliupdates.get(c) != null && cliupdates.get(c).size() > 0 && _registeredUsers.containsValue(c)) {
						DataUnit[] duUpds = new DataUnit[cliupdates.get(c).size()];
						int i = 0;
						for (DataUnit d : cliupdates.get(c)) {
							duUpds[i] = d;
							i++;
						}
						_net.send(reverseRegisteredUsers.get(c), new RoundAllMessage(tickno, duUpds));
						System.out.println("[S] Round (" + tickno + "): update to client " +
								c + "#updates=" + cliupdates.get(c).size());
					}
				}
			} else {
				_net.broadcast(new RoundAllMessage(tickno, null));
			}
			return;
		}
		System.out.println("[S] Round: operation not allowed.");
	}


	/*
	 * Low level client connection notifications
	 */
	
	public void callbackConnOpened(INetworkServices net, int client) {
		_notifListener.callbackNotificationReceived(
				new ConnOpenNotification(client));
	}

	public void callbackConnClosed(INetworkServices net, int client) {
		System.out.println("[S] Removing client " + _registeredUsers.get(client) + " from registered users.");
		_registeredUsers.remove(client);
		System.out.println("[S] Registered clients are " + _registeredUsers);
		_notifListener.callbackNotificationReceived(
				new ConnClosedNotification(client));
	}

	/*
	 * Auxiliary methods
	 */
	

	private void shutdown() {
		_net.disable();
		_trigger.disable();
		_state = ServerState.IDLE;
		_pool = null;
		System.out.println("[S] Server shutdown.");
	}
	
	private boolean stateInvalid()	{
		switch (_state) {
		case IDLE:
		case SETUP:
		case ACTIVE:
			return false;

		default:
			return true;
		}
	}
	
	private void printAssociations() {
		System.out.println("[S] Printing existing DU-Client associations:");
		for(Integer i : _duAssociation.keySet()) {
			System.out.println("\tDU with ID: " + i + " is associated with client with UserID: " + _duAssociation.get(i));
		}
	}
	
}
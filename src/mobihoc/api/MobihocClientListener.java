package mobihoc.api;

import java.util.*;

import mobihoc.api.IMobihocClientListener;
import mobihoc.api.MobihocClient;
import mobihoc.session.*;
import mobihoc.annotation.PhiAnnotation;

public class MobihocClientListener implements IMobihocClientListener {

	IMobihocApp _app;
	Map<Integer, DataUnit> stateMap = new TreeMap<Integer, DataUnit>();

	public MobihocClientListener(IMobihocApp app) {
		_app = app;
	}
	
	private void populateStateMap(DataUnit[] dus) {
		for (DataUnit du : dus) { stateMap.put(du.getId(), du); }
	}

	public void callbackSubscribeResult(boolean status, String desc) {
		System.out.println("[C] Subscription response: " + status + " " + desc);

		setEnable(status);
		setStatus(desc);
		setupPhi(status);
	}
	
	public void callbackPublishResult(boolean status, DataUnit[] dus, int[] ids) {
		System.out.println("[C] Publication result received.");
		if (status) {
			setStatus("Published result received.");
			if (dus.length != ids.length) {//verificar se o numero de objectos que foram enviados e' iguais aos id's retornados
				setStatus("Published wrong - sizes.");
				return;
			} else {
				for (int i = 0; i < dus.length; i++) {//a colocar o id correcto (de acordo com o que foi atribuido por parte do servidor)
					System.out.println("[C] Publication: localId was - " + dus[i].getId() + " while the newId is - " + ids[i]);
					dus[i].setId((int)ids[i]);
				}
				// Colocar DataUnits publicados no stateMap
				populateStateMap(dus);
				_app.callbackStateUpdated();
				/*
				for (int i = 0; i < dus.length; i++) {
					int val = dus[i].getId();
					if (val != ids[i]) {
						setStatus("Published wrong - addresses.");
						return;
					}
					System.out.println("[C] Publication: ["+i+"] "+val+"-"+ids[i]);
				}
				*/
			}
			setStatus("Published ok.");
			System.out.println("[C] Publication ok.");
		} else {
			setStatus("Not published.");
		}
	}


	public void callbackUpdateState(DataUnit[] dus) {
		System.out.println("[C] Merging updates");
		//List<DataUnit> state = _app.getState();
		for (DataUnit du : dus) {
			DataUnit localDu = stateMap.get(du.getId());
			if (localDu == null) {
				System.out.println("callbackUpdateState: Error updating du: not found on local state map");
				break;
			}
			localDu.merge(du);
		}
		_app.callbackStateUpdated();
	}
	
	public void callbackNewData(DataUnit[] dus) {
		// Colocar DataUnits recebidos no stateMap
		populateStateMap(dus);
		_app.callbackNewData(Arrays.asList(dus));
	}

	public void callbackEnable(boolean status) {
		System.out.println("[C] Enable status: " + status);
		setEnable(status);
		setStatus((status)?"Enabled.":"Disabled");
	}

	public void callbackDisable() {
		System.out.println("[C] Disabled.");
		setEnable(false);
		setStatus("Disabled");
	}

	public void callbackLoadState(int[] myIds, DataUnit[] dus) {
		// Colocar DataUnits recebidos no stateMap
		populateStateMap(dus);

		List<Integer> lstIds = new ArrayList<Integer>();
		for (int i : myIds) lstIds.add(new Integer(i));

		// Separar DataUnits em duas listas
		List<DataUnit> myDus = new ArrayList<DataUnit>();
		List<DataUnit> otherDus = new ArrayList<DataUnit>();

		for (DataUnit du : dus) {
			if (lstIds.contains(du.getId())) {
				myDus.add(du);
			} else {
				otherDus.add(du);
			}
		}

		_app.callbackLoadState(myDus, otherDus);
	}

	public void callbackError(String error) {
		_app.callbackError(error);
	}

	public void callbackConnClosed() {
		_app.callbackConnClosed("Lost connection with server.");
	}

/* Isto nao sei ainda para que serve mas supostamente, como tudo, tem algum sentido alem de imprimir stuff */
/* Mas como nao consta de nenhuma interface... */
	public void setStatus(String msg)
	// report the status of the server connection 
	{
		System.err.println("MobihocClientListener::setStatus = " + msg);
	}

	public void setEnable(boolean isEnabled) { }

	public void setupPhi(boolean isEnabled) {
		if (isEnabled) {
			System.out.println("Sending Phi...");
			// Enviar Phi ao servidor, se o encontrarmos
			MobihocClient client = _app.getMobihocClient();
			// App deve ter Phi como anotação
			PhiAnnotation anot = _app.getClass().getAnnotation(PhiAnnotation.class);
			if (anot == null) return;
			client.sendPhi(Phi.fromAnnotation(anot));
			System.out.println("Sent Phi.");
		}
	}

}

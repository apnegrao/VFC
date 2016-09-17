package app.proto3.client;

import app.*;

// ClientForm.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, August 2005

/* This class is the GUI in front of an EchoClient object
 which manages the sending and receiving of messages with
 the chosen echo service.

 A message is input via messageTF, and send to the
 service using EchoClient's echoMessage() method. 
 This method _waits_ for an answer from the server. 
 ClientForm shows the response in responseSI.

 If EchoClient detects an error when communicating with
 the server, it notifies the client by setting the
 statusSI field, and disabling any further input from
 ClientForm's messageTF field. It also writes the current 
 status of the interaction into statusSI.
 */

import mobihoc.api.IMobihocApp;
import mobihoc.api.MobihocClient;
import mobihoc.api.MobihocClientListener;
import mobihoc.session.DataUnit;
import mobihoc.session.messages.PublishReqMessage;
import mobihoc.network.client.ServerRecord;

public class GUIClientForm implements IMobihocApp {

	/* Isto e' o estado */
	private IntegerDataUnit[] _data;

	// for info coming from the server

	private GUIClient ecm;

	private MobihocClient _client;

	public GUIClientForm(ServerRecord sr, GUIClient ecm) {
		this.ecm = ecm;
		
		/*
		* State Initialization
		*/
		_data = new IntegerDataUnit[10];
		for (byte i = 0; i < _data.length; i++) {
			_data[i] = new IntegerDataUnit(i);
		}

		_client = new MobihocClient(new MobihocClientListener(this));
		// 07/05/08: To compile after changes made 
		//_client.subscribe(sr);
		
		//setEnable(true);
		
		while (true && !false) {
			commandAction();
		}
	}

	public void commandAction() {
		UserInterface ui = new ConsoleUserInterface();
		
		String line = ui.getStringFromUser("Enter command (Available 'P', 'PE', 'PEW', 'PEWD', 'state', 'compare', 'quit') or another string to send\n");
		if (line == null) {
			return;
		}

		System.out.println("Command: " + line);

/*		if (line.trim().equals("p")) {
			IntegerDataUnit[] data = new IntegerDataUnit[10];
			for (byte i = 0; i < data.length; i++) {
				data[i] = new IntegerDataUnit(i);
			}

			_client.publish(data);
			setStatus("Publishing...");

			PublishReqMessage msg = new PublishReqMessage(data);

//				TestObj2 msg = new TestObj2(data);

//				Message msg = new Message();
			
//				IntegerDataUnit du = new IntegerDataUnit(22);
//				DataUnit du = new DataUnit(55);
			byte[] a = null;
			try {
				a = Serializer.serializeToBytes(msg);
				setStatus("Serialize ok...");
			} catch (Exception e) {
				setStatus("Serialize exception...");
			}
			
			System.out.println("MSG:" + a);
			
			try {
				Object o = Serializer.deserializeFromBytes(a);
				setStatus("Deserialize ok...");
			} catch (Exception e) {
				setStatus("Deserialize exception...");
				e.printStackTrace();
			}				
			return;
			
		}
*/
		if (line.trim().equals("P")) {
			_client.publish(_data);
			//setStatus("Publishing...");
			return;
		}

		if (line.trim().equals("PE")) {
			_client.enable();
			//setStatus("Enabling...");
			return;
		}

		if (line.trim().equals("PEW")) {
			IntegerDataUnit[] updates = new IntegerDataUnit[2];
			updates[0] = new IntegerDataUnit(50);
			updates[1] = new IntegerDataUnit(51);
			updates[0].setId(0);
			updates[1].setId(2);
			DataUnit.printSet("[C] Updates to send ->", updates);
			_client.write(updates);
			//setStatus("Writing...");
			return;
		}
		
		if (line.trim().equals("PEWD")) {
			_client.disable();
			//setStatus("Disabling...");
			return;
		}

		/*if (line.trim().equals("s")) {
			TestObj1[] data = new TestObj1[10];
			for (byte i = 0; i < data.length; i++) {
				byte[] val = new byte[1];
				val[0] = i;
				data[i] = new TestObj1(i+1,val);
			}

			TestObj2 msg = new TestObj2(data);
			
			byte[] a = null;
			try {
				a = Serializer.serializeToBytes(msg);
				setStatus("Serialize ok...");
			} catch (Exception e) {
				setStatus("Serialize exception...");
			}
			
			System.out.println("MSG:" + a);

			try {
				Object o = Serializer.deserializeFromBytes(a);
				setStatus("Deserialize ok...");
			} catch (Exception e) {
				setStatus("Deserialize exception...");
				e.printStackTrace();
			}
			return;
			
		}*/
		
		if (line.trim().equals("quit")) {
			System.exit(0);
		}

		if (line.trim().equals("state")) {
			for(IntegerDataUnit unit : _data) {
				System.out.println(unit.getValue());
			}
			return;
		}

		if (line.trim().equals("compare")) {
			for(IntegerDataUnit unit : _data) {
				System.out.println(unit.compareNiu(_data[3]));
			}
			return;
		}

		// otherwise...
		
		_client.sendOutOfBand(line);

//			String resp = _core.send(messageTF.getString());
		/* Pass the input message to EchoClient, and _wait_ for a
			reply. The answer can be an error message. */
//			String resp = "ola";
//			responseSI.setText(resp); // show the response
		// messageTF.setString("");
	}

	public java.util.List<DataUnit> getState() {
		return java.util.Arrays.asList((DataUnit[])_data);
	}
	
	public void callbackStateUpdated() { }
	
	public void callbackNewData(java.util.List<DataUnit> dus) { }
	
	public void callbackLoadState(java.util.List<DataUnit> myDus, java.util.List<DataUnit> dus) { }
	
	public void callbackError(String error) { }

	public void callbackConnClosed(String error) { }

	public MobihocClient getMobihocClient() {
		return _client;
	}
}
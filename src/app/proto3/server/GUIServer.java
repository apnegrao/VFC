package app.proto3.server;

// EchoServerMIDlet.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, August 2005 

/* The MIDlet starts the echo server.
 It displays the current number of handlers, and a
 scrollable text box of messages that it has received.

 Pressing the "exit" command causes the server and its handlers
 to terminate.
 */

import java.util.*;

import mobihoc.Mobihoc;
import mobihoc.api.*;
import mobihoc.network.*;
import mobihoc.network.server.*;
import mobihoc.exception.*;

import app.*;

public final class GUIServer implements IHostConfigListener, IMobihocServerListener, IMobihocListener {
// 	private Display display;
// 
// 	private Form form;

// 	private StringItem numHandlersSI;

//	private ScrollableMessagesBox scroller;

// 	private Command exitCmd;

	private int numHandlers = 0; // number of active connections

	private MainMIDlet _parent;

	private MobihocServer _server;

// 	public MessageUI messageui;
	
	public static GUIServer _instance;
	
	NetworkFinder nf;
	
	public GUIServer(MainMIDlet parent) {
		_parent = parent;

		_instance = this;
	
 		Mobihoc.setListener(this);

		UserInterface ui = new ConsoleUserInterface();
		OutputComposer oc = new OutputComposer(); 
		
		// Escolher método de ligação
		List<ConnectionInfo> conns = ConnectionInfoFactory.getAvailableConnectionInfos();
		int i = 0;
		oc.addLine("Please choose method of connection:");
		for (ConnectionInfo ci : conns) {
			oc.addLine(i + " " + ci.toString());
			i++;
		}
		String req = oc.flush();

		while (true) {
			try {
			int res = ui.getIntFromUser(req);
			nf = conns.get(res).getHostConfig(this);
			break;
			} catch (IndexOutOfBoundsException e) { }
		}
		
		nf.run();
	}

	public void callbackClientPublished(int address) {
		// called when a new handler is created
		numHandlers++;
// 		numHandlersSI.setText("" + numHandlers);
	}

	public void callbackClientUnpublished(int address) {
		// called when a handler is about to terminate
		numHandlers--;
// 		numHandlersSI.setText("" + numHandlers);

	}

	public synchronized void callbackMessageIn(String msg) {
		log("Server got OOB message '" + msg + "'");
		// used to show the message received by a handler
//		scroller.addMessage(msg);
// 		form.append(msg);
	}

	  public void log(String s)
	  {
	    System.err.println("app3 log:: " + s);
	  }
	  
	public void callbackNeedInfo(List<InfoItem> infoItems) {
		UserInterface ui = new ConsoleUserInterface();
		for (InfoItem i : infoItems) {
			i.setResult(ui.getStringFromUser("Mobihoc requested info (type=" + i.getInfoType() + "), " + i.getUserText() + ":\n"));
		}
		try {
			nf.requestedInfoFilled();
		} catch (InfoItemResultException e) {
			log("InfoItemResultException");
		}
	}
	
	public void callbackConfigResults(HostRecord rc) {
		/*
		 * State management
		 */

		_server = new MobihocServer(this);
		_server.open(rc);
	}

}

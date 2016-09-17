package mobihoc.network.server;
// EchoServer.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, August 2005

/* EchoService is the top-level echo server: a RFCOMM service
 with the specified UUID and name. As a client
 connects, a ThreadedEchoHandler thread is spawned to
 deal with the client.

 The waiting for client connections is done in a separate
 thread so that the creation of the EchoServer doesn't 
 cause the top-level MIDlet to block.

 The server, and handlers, are terminated by calling closeDown().
 */

import java.io.*;
import java.util.*;

// Remover depois de abstrair operações de rede
import java.net.*;
import mobihoc.network.*;

import mobihoc.Mobihoc;


public class NetworkManager extends Thread implements INetworkServices {

	private INetworkListener ecm;

//	private StreamConnectionNotifier server;

	private Vector<ConnectionHandler> handlers; // stores the ThreadedEchoHandler objects

	private boolean isRunning = false;
	
	private int _cliIDs = 0;
	
	private HostRecord hostRecord;

	public NetworkManager(INetworkListener ecm)
	// Create a server connection
	{
		this.ecm = ecm;
		handlers = new Vector<ConnectionHandler>();
	}

	/**
	 * Wait for client connections, creating a handler for each one
	 */
	public void run() {
	
		// Here goes the server activation code (whatever the server type is)
		NetworkConnection nc = hostRecord.getNetworkConnection();
		try {
			nc.startServer(hostRecord);
		} catch (IOException e) {
			Mobihoc.log("[S] Error opening port; aborting\n" + e);
			return;
		}

		Mobihoc.log("[S] Service was setup and is waiting.");

		// Main server loop (wait for and accept new connections)
		isRunning = true;
		try {
			while (isRunning) {
				// wait for a client connection
				NetworkStream client = nc.accept();
				Mobihoc.log("[S] Client " + client.getFriendlyName() + " has connected.");

				// create client handler
				ConnectionHandler hand = new ConnectionHandler(ecm, this, _cliIDs++);
				handlers.addElement(hand);
				hand.enable(client);
 			}
 		} catch (IOException e) {
 			Mobihoc.log("[S] Exception accepting client connection.\n"+e);
 		}
	}

	public void enable(HostRecord config)
	{
		this.hostRecord = config;

		// Waits for client connections
		this.start();
		Mobihoc.log("[S] Accepting client connections.");
	}
	
	/**
	 * Stop accepting any further client connections, and close down
	 * all the handlers.
	 */
	public void disable()	{
		System.err.println("NetworkManager::disable code Disabled!");
// 		Mobihoc.log("Close down server");
// 		if (isRunning) {
// 			isRunning = false;
// 
// 			// close connection, and remove service record from SDDB
// 			try {
// 				server.close();
// 			} catch (IOException e) {
// 				Mobihoc.log("[S] Exception closing connection.\n"+e);
// 			}
// 
// 			// close down the handlers
// 			ConnectionHandler hand;
// 			for (int i = 0; i < handlers.size(); i++) {
// 				hand = (ConnectionHandler) handlers.elementAt(i);
// 				hand.disable();
// 			}
// 			handlers.removeAllElements();
// 		}
	}

	public boolean send(int address, Object data) {
		Enumeration<ConnectionHandler> e = handlers.elements();
		while(e.hasMoreElements()) {
			ConnectionHandler hand = e.nextElement();
			if (address == hand.getClientID()) {
				return hand.send(data);
			}
		}
		return false;
	}

	public boolean broadcast(Object data) {
		boolean status = true;
		Enumeration<ConnectionHandler> e = handlers.elements();
		while(e.hasMoreElements()) {
			ConnectionHandler hand = e.nextElement();
				status &= hand.send(data);
		}
		return status;
	}
	
	public boolean broadcastExcept(int address, Object data) {
		boolean status = true;
		Enumeration<ConnectionHandler> e = handlers.elements();
		while(e.hasMoreElements()) {
			ConnectionHandler hand = e.nextElement();
			if (address != hand.getClientID()) {
				status &= hand.send(data);
			}
		}
		return status;
	}
}

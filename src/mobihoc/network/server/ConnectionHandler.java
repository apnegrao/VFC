package mobihoc.network.server;

// ThreadedEchoHandler.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, August 2005

/* A threaded handler, called by EchoServer to deal with a client.

 When a message comes in, it is sent back converted to uppercase.

 The handler also communicates with the top-level MIDlet (ecm)
 to add and decrement itself from the count of handlers, and
 to display a received message.

 closeDown() terminates the handler.

 ThreadedEchoHandler uses the same readData() and sendMessage()
 methods as EchoClient.
 */

import java.io.*;

import mobihoc.Mobihoc;
import mobihoc.network.NetworkStream;

public class ConnectionHandler extends Thread {
	private INetworkListener ecm;
	private INetworkServices _netsvc;

	private NetworkStream conn;
	private InputStream in;
	private OutputStream out;
	private ObjectOutputStream writer;
	private ObjectInputStream reader;
	
	private int _clientID = 0;

	private boolean isRunning = false;

	private String clientName;

	public ConnectionHandler(INetworkListener ecm, INetworkServices netsvc, int clientID) {
		this.ecm = ecm;
		_netsvc = netsvc;
		_clientID = clientID;
	}

	/**
	 * Return the 'friendly' name of the device being examined,
	 *  or "Device ??"
	 *  
	 * @return
	 */
	public String getClientName() {
		return clientName;
	}

	public int getClientID() {
		return _clientID;
	}
	
	public void enable(NetworkStream conn)
	{
		// Get I/O streams from the stream connection
		try{
			this.conn = conn;
			in = conn.getInputStream();
			out = conn.getOutputStream();
			writer = new ObjectOutputStream(out);
			reader = new ObjectInputStream(in);
		} catch (IOException e) {
			Mobihoc.log("[S] Exception openning IO streams. E=" + e.toString());
			return;
		}
		ecm.callbackConnOpened(_netsvc, _clientID);

		// Retrieve client name
		try {
			clientName = conn.getFriendlyName();
			Mobihoc.log("[S] Client name: " + clientName);
		} catch (Exception e) {
			Mobihoc.log("[S] Exception retrieving client name. E=" + e.toString());
		}

		// Process input client messages.
		this.start();
		Mobihoc.log("[S] Client handler thread spawned.");
	}
	
	public void run() {
		isRunning = true;
		while (isRunning) {
			Object data = null;

			// read message
			try {
				data = reader.readObject();
			} catch (Exception e) {
				Mobihoc.log("[S] Connection (client "+_clientID+"). E=" + e.toString());
				break;
			}

			// dispatch message
			try {
				ecm.callbackMsgReceived(_netsvc, _clientID, data);
			} catch (Exception e) {
				Mobihoc.log("[S] Connection msg handler exception. E=" + e.toString());
				//Mobihoc.log(e.getMessage());
			}
		}

		disable();
		Mobihoc.log("[S] Connection (client "+_clientID+") closed.");

		// notify disconnection
		try {
			ecm.callbackConnClosed(_netsvc, _clientID);
		} catch (Exception e) {
			Mobihoc.log("[S] Connection handler exception. E=" + e.toString());
			//Mobihoc.log(e.getMessage());
		}
	}

	public boolean send(Object data) {
		if (isRunning == true) {
			try {
				writer.writeObject(data);
				writer.flush();
				return true;
			} catch (IOException e) {
				Mobihoc.log("[S] Error sending object. E=" + e.toString());
				disable();
				return false;
			}
		} else {
			return false;
		}
	}

	public void disable()
	{
		isRunning = false;
		try {
			if (conn != null) {
				in.close();
				out.close();
				conn.close();
				writer.close();
				reader.close();
			}
		} catch (IOException e) {
			Mobihoc.log("[S] Exception closing connection. E=" + e.toString());
			Mobihoc.log(e.getMessage());
		}
	}
}
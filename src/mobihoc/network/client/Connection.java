package mobihoc.network.client;

// EchoClient.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, August 2005

/* Open a link to the server, and create an InputStream and
 OutputStream.

 Communication is triggered by echoMessage() being called,
 which sends a message to the server, and waits for an
 answer. 

 closeDown() closes the server link.

 EchoClient uses the same readData() and sendMessage()
 methods as ThreadedEchoHandler.
 */

import mobihoc.network.*;
import mobihoc.network.connection.*;

import java.net.*;
import java.io.*;

public class Connection extends Thread {

	public enum Status {
		DISCONNECTED_ORDERLY,
		CONNECTED,
		IDLE,
		CONNECTING,
		NO_SERVICE_FOUND,
		UNABLE_TO_CONTACT_SERVER,
		EXCEPTION_RECEIVED
	}

	private ServerRecord servRecord;
	
	private IConnectionListener clientForm;

	private NetworkStream conn;

	private InputStream in;
	
	private OutputStream out;

	private ObjectOutputStream writer;

	private ObjectInputStream reader;

	private Status _state;


	public Connection(IConnectionListener cf) {
		clientForm = cf;
		_state = Status.IDLE;
	}

	/**
	 * The server connection is set up in this thread since
	 the call to Connector.open() may block for a long period,
	 and we don't want that to affect the ClientForm GUI.
	 */
	public void run() {

		// Connect to the server
 		try {
			NetworkConnection nc = servRecord.getNetworkConnection();
			try {
				conn = nc.connect(servRecord);
			} catch (IOException e) {
				disconnect();
				clientForm.callbackConnectionStatus(Status.UNABLE_TO_CONTACT_SERVER);
				return;
			}

			// get I/O streams from the stream connection
			in = conn.getInputStream();
			out = conn.getOutputStream();
			writer = new ObjectOutputStream(out);
			reader = new ObjectInputStream(in);


			_state = Status.CONNECTED;
 
			// notify upper layer
			clientForm.callbackConnectionStatus(Status.CONNECTED);
 
		} catch (IOException ex) {
			System.out.println("[C] Exception in connection.\n"+ex);
			clientForm.callbackConnectionStatus(Status.EXCEPTION_RECEIVED);
			disconnect();
			return;
		}

		// Server message read cycle
		Status constatus = Status.CONNECTED;	//Estava cá constatus = 0...
		try {
			while (_state == Status.CONNECTED) {
				Object data = reader.readObject();

				if (data != null) {
//					System.out.println("[C] 	Class:"+data.getClass().getName());

					// dispatch message
					try {
						clientForm.callbackMsgReceived(this,data);
					} catch (Exception e) {
						System.out.println("[C] Exception handling message.");
					}

				} else {
					constatus = Status.DISCONNECTED_ORDERLY;
					break;
				}
			}
		} catch (Exception ex) {
			System.out.println("[C] Exception in connection.\n"+ex);
			constatus = Status.EXCEPTION_RECEIVED;
		}

		disconnect();

		// notify disconnection
		try {
			clientForm.callbackConnectionStatus(constatus);
		} catch (Exception e) {
			System.out.println("[C] Exception notifying connection closed.");
		}
	}

	public boolean send(Object data) {
		if (_state == Status.CONNECTED) {
			try {
				writer.writeObject(data);
				writer.flush();
				return true;
			}catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
	
	public boolean isClosed() {
		return _state == Status.IDLE;
	}

	public void connect(ServerRecord sr) {
		if (sr == null || _state != Status.IDLE) {
			return;
		}
		_state = Status.CONNECTING;
		servRecord = sr;
		this.start();
		System.out.println("[C] Client connector spawned.");
	}
	
	public void disconnect() {
		if (_state == Status.IDLE) {
			return;
		}
		servRecord = null;
		try {
			if (in != null) {
				in.close();
				in = null;
			}
			if (out != null) {
				out.close();
				out = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
			if (writer != null) {
				writer.close();
				writer = null;
			}
			if (reader != null) {
				reader.close();
				reader = null;
			}
		} catch (IOException e) {
			System.out.println("[C] Exception disconnecting.\n"+e);
		}
		_state = Status.IDLE;
	}
}

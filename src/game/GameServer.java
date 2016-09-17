package game;

import java.util.*;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import mobihoc.Mobihoc;
import mobihoc.api.*;
import mobihoc.network.*;
import mobihoc.network.server.*;
import mobihoc.exception.*;

public class GameServer implements IMobihocServerListener, IMobihocListener {

	private ConnectionSetupDialog dialog;
	private MobihocServer server;
	
	private int numClients = 0; // number of active connections
	
	public GameServer() {
		this(null);
	}

	public GameServer(String connectionHint) {
		Mobihoc.setListener(this);
	
		// Obter ligação
		dialog = new ConnectionSetupDialog(null, false);
		if (connectionHint != null) {
			dialog.setConnectionHint(connectionHint);
		}
		dialog.accepted.connect(this, "connectionSelected()");
		dialog.rejected.connect(this, "terminate()");
		if (dialog.getResultHostRecord() == null) {
			dialog.exec();
		} else {
			connectionSelected();
		}
	}
	
	public void connectionSelected() {
		HostRecord record = dialog.getResultHostRecord();
		if (record == null) {
			QMessageBox.information(null, "Error", "No results were returned by the backend.", QMessageBox.StandardButton.Close);
			System.exit(1);
		}

		startGameServer(record);
	}
	
	private void startGameServer(HostRecord record) {
		server = new MobihocServer(this);
		server.open(record);
	}
	
	public void terminate() {
		System.exit(0);
	}

	// IMobihocServerListener
	public void callbackMessageIn(String desc) {
		System.out.println("Received Out-Of-Band Message: '" + desc + "'");
		//log("Received Out-Of-Band Message: '" + desc + "'");
	}

	public void callbackClientPublished(int address) {
		numClients++;
	}

	public void callbackClientUnpublished(int address) {
		numClients--;
	}
	
	// IMobihocListener
	public void log(String log) {
		//System.out.println("MobihocLogServer: " + log);
	}

}

package game;

import java.util.*;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import mobihoc.Mobihoc;
import mobihoc.api.IMobihocListener;
import mobihoc.network.client.*;
import mobihoc.session.UserAgent;

public class GameMain extends QWidget implements IMobihocListener {

	private QLayout mainLayout;
	private ConnectionSetupDialog dialog;
	private QListWidget logWidget;
	private QWidget otherWidget = null;

	public GameMain() {
		// Criar layout que vai conter widgets
		mainLayout = new QVBoxLayout();
		setLayout(mainLayout);
		
		// Obter ligação
		dialog = new ConnectionSetupDialog(this, true);
		dialog.accepted.connect(this, "connectionSelected()");
		dialog.rejected.connect(this, "terminate()");
		dialog.exec();
		
		// Iniciar log widget
		/*logWidget = new QListWidget();
		mainLayout.addWidget(logWidget);*/
		Mobihoc.setListener(this);
		
		show();
	}
	
	public void connectionSelected() {
		List<ServerRecord> records = dialog.getResultServerRecords();
		if (records.isEmpty()) {
			QMessageBox.information(null, "Error", "No results were returned by the backend.", QMessageBox.StandardButton.Close);
			System.exit(1);
		}

		ServerRecord connectRecord;
		if (records.get(0).isDirect()) { // ServerRecord directo, podemos prosseguir
			connectRecord = records.get(0);
		} else {
			List<String> recordStrings = new ArrayList<String>();
			for (ServerRecord record : records) recordStrings.add(record.toString());
			String item = QInputDialog.getItem(this, "Please select server to use", "Please select server to use", recordStrings, 0, false);
			int pos = recordStrings.indexOf(item);
			if (pos == -1) {
				System.out.println("FIXME GameMain::connectionSelected");
				terminate();
				return;
			} else {
				connectRecord = records.get(pos);
			}
		}
		
		GameWidget gw = new GameWidget(null, connectRecord);
		mainLayout.addWidget(gw);
		gw.restartRequested.connect(this, "restart()");
		setWindowTitle(tr("Game - User: " + gw.getUserAgent().getNickname()));
		QApplication.instance().lastWindowClosed.connect(this, "terminate()");
	}
	
	public void terminate() {
		System.out.println("Todas as janelas foram fechadas, a sair...");
		System.exit(0);
	}
	
	public void restart() {
		QApplication.instance().lastWindowClosed.disconnect(this, "terminate()");
		otherWidget = new GameMain();
		hide();
	}
	
	// IMobihocListener
	public void log(String log) {
		//System.out.println("LOG " + log);
		//new QListWidgetItem(log, logWidget);
	}
	
	public static void main(String[] args) {
		QApplication.initialize(args);
		QApplication.setStyle("plastique"); // cleanlooks, ...
		
		if (args.length < 1) {
			System.err.println("Usage: java GameMain -client/-server");
			System.exit(1);
		}

		if (args[0].equalsIgnoreCase("-client")) {
			GameMain gm = new GameMain();
		} else if (args[0].equalsIgnoreCase("-server")) {
			GameServer gs = new GameServer();
		} else if (args[0].equalsIgnoreCase("-server-tcplocalhost")) {
			GameServer gs = new GameServer("Tcp Localhost");
		} else {
			System.err.println("Usage: java GameMain -client/-server");
			System.exit(1);
		}

		QApplication.exec();
	}

}

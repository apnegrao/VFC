package game;

import java.util.*;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class ReconnectDialog extends QDialog {

	private QLabel headerLabel;
	private QFrame frame;
	private QPushButton reconnButton;
	private QPushButton exitButton;
	private QPushButton selectButton;
	private QVBoxLayout frameInnerLayout;

	public Signal0 reconnect = new Signal0();
	public Signal0 select = new Signal0();

	public ReconnectDialog(QWidget parent) {
		super(parent);
		
		headerLabel = new QLabel(tr("<center><h2>Connection Lost</h2></center>"));
		headerLabel.setFixedHeight(headerLabel.sizeHint().height());
		
		reconnButton = new QPushButton(tr("&Reconnect"));
		reconnButton.clicked.connect(this, "reconnect()");
		
		exitButton = new QPushButton(tr("&Exit Game"));
		exitButton.clicked.connect(this, "terminate()");
		
		selectButton = new QPushButton(tr("&Choose Server"));
		selectButton.clicked.connect(this, "select()");

		QHBoxLayout bottomLayout = new QHBoxLayout();
		bottomLayout.addStretch();
		bottomLayout.addWidget(reconnButton);
		bottomLayout.addWidget(selectButton);
		bottomLayout.addWidget(exitButton);
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addWidget(headerLabel);
		mainLayout.addLayout(bottomLayout);
		setLayout(mainLayout);
		
		setWindowTitle(tr("Connection Options"));
	}

	private void reconnect() {
		reconnect.emit();
		accept();
	}
	
	private void terminate() {
		System.out.println("A sair, vindo do ReconnectDialog...");
		System.exit(0);
	}

	private void select() {
		select.emit();
		accept();
	}

}

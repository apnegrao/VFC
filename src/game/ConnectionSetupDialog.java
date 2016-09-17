package game;

import java.util.*;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import mobihoc.Mobihoc;
import mobihoc.api.IMobihocListener;
import mobihoc.network.*;
import mobihoc.network.client.*;
import mobihoc.network.server.*;
import mobihoc.exception.*;

public class ConnectionSetupDialog extends QDialog implements IServerFinderListener, IHostConfigListener/*, IMobihocListener*/ {

	private QLabel headerLabel;
	private QFrame frame;
	private QPushButton okButton;
	private QPushButton cancelButton;
	private QVBoxLayout frameInnerLayout;
	private QComboBox connTypeCombo;
	private NetworkFinder nf;
	// Usado para manter os lineedits que têm as propriedades pedidas pelo Mobihoc
	private List<QLineEdit> infoLineEdits;
	// Usado para mapear os lineedits às propriedades que vão preencher
	private Map<QLineEdit, InfoItem> infoLineEditsItems;
	private List<ServerRecord> resultServerRecords;
	private HostRecord resultHostRecord;
	private boolean infoNeeded;
	// Se estamos a procurar ligações para um cliente ou para um servidor
	private boolean client;
	private String connectionHint = null;

	public ConnectionSetupDialog(QWidget parent, boolean client) {
		super(parent);
		
		headerLabel = new QLabel(tr("<center><h2>Setup Connection</h2></center>"));
		headerLabel.setFixedHeight(headerLabel.sizeHint().height());
		
		frame = new QFrame();
		frame.setFrameShape(QFrame.Shape.StyledPanel);
		frame.setFrameShadow(QFrame.Shadow.Raised);
		frameInnerLayout = new QVBoxLayout();
		frame.setLayout(frameInnerLayout);
		
		okButton = new QPushButton(tr("&Ok"));
		okButton.setEnabled(false);
		
		cancelButton = new QPushButton(tr("&Cancel"));
		cancelButton.clicked.connect(this, "reject()");
		
		QHBoxLayout bottomLayout = new QHBoxLayout();
		bottomLayout.addStretch();
		bottomLayout.addWidget(okButton);
		bottomLayout.addWidget(cancelButton);
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addWidget(headerLabel);
		mainLayout.addWidget(frame);
		mainLayout.addLayout(bottomLayout);
		setLayout(mainLayout);
		
		setWindowTitle(tr("Connection Setup"));
		
		//Mobihoc.setListener(this);
		
		// Obter ligações disponíveis no Mobihoc
		List<ConnectionInfo> conns = ConnectionInfoFactory.getAvailableConnectionInfos();
		
		// Criar linha no layout dentro da frame
		QLabel connTypeLabel = new QLabel(tr("Connection Type"));
		connTypeCombo = new QComboBox();
		connTypeLabel.setBuddy(connTypeCombo);
		
		QHBoxLayout frameLine = new QHBoxLayout();
		frameLine.addWidget(connTypeLabel);
		frameLine.addWidget(connTypeCombo);
		frameInnerLayout.addLayout(frameLine);
		frameInnerLayout.addStretch();
		
		// Popular combobox
		connTypeCombo.addItem("");	// Item default, vazio
		for (ConnectionInfo ci : conns) {
			connTypeCombo.addItem(new QIcon(), ci.toString(), ci);
		}
		
		connTypeCombo.currentIndexChanged.connect(this, "connectionSelected(Integer)");
		
		this.client = client;
	}
	
	public ConnectionSetupDialog setConnectionHint(String connectionHint) {
		this.connectionHint = connectionHint;
		if (connectionHint != null) {
			int res = connTypeCombo.findText(connectionHint);
			if (res != -1) {
				connTypeCombo.setCurrentIndex(res);
			}
		}
		return this;
	}
	
	private void connectionSelected(Integer index) {
		// Limpar campos de info que já existam
		QtUtil.clearLayout(frameInnerLayout, 2);
		// Limpar sinais para o ok
		okButton.disconnect();
		// Limpar valores guardados
		resultServerRecords = null;
		resultHostRecord = null;

		ConnectionInfo ci = (ConnectionInfo) connTypeCombo.itemData(index);
		if (ci != null) {
			// Começar processo
			if (client) {
				nf = ci.getServerFinder(this);
			} else {
				nf = ci.getHostConfig(this);
			}
			infoNeeded = nf.isInfoNeeded();
			if (!infoNeeded) {
				okButton.setEnabled(true);
				// Desligar sinal que ia chamar o 
				okButton.clicked.connect(this, "accept()");
			} else {
				okButton.setEnabled(false);
				okButton.clicked.connect(this, "proceedWithDiscovery()");
			}
			nf.run();
		}
	}

	private void infoLineEditChanged() {
		for (QLineEdit le : infoLineEdits) {
			if (le.text().equals("")) {
				okButton.setEnabled(false);
				return;
			}
		}
		// Todos os campos têm algo, vamos ligar o botão de ok
		okButton.setEnabled(true);
	}
	
	// Ligado ao okButton
	private void proceedWithDiscovery() {
		try {
			// Preencher InfoItems
			for (QLineEdit le : infoLineEdits) {
				InfoItem i = infoLineEditsItems.get(le);
				i.setResult(le.text());
			}
			nf.requestedInfoFilled();
		} catch (InfoItemResultException e) {
			QMessageBox.information(this, "Error", "Network backend error. Check if information is correct.", QMessageBox.StandardButton.Close);
			okButton.setEnabled(false);
		}
	}
	
	public List<ServerRecord> getResultServerRecords() {
		return resultServerRecords;
	}
	
	public HostRecord getResultHostRecord() {
		return resultHostRecord;
	}
	
	// IServerFinderListener
	public void callbackNeedInfo(List<InfoItem> infoItems) {
		infoLineEdits = new ArrayList<QLineEdit>();
		infoLineEditsItems = new HashMap<QLineEdit, InfoItem>();
		// Usado para mapear os lineedits às propriedades que vão preencher
		// Pedida info, popular frame
		for (InfoItem  i : infoItems) {
			QHBoxLayout frameLine = new QHBoxLayout();
			QLabel label = new QLabel(i.getUserText());
			QLineEdit lineEdit = new QLineEdit();
			lineEdit.textChanged.connect(this, "infoLineEditChanged()");
			
			frameLine.addWidget(label);
			frameLine.addStretch();
			frameLine.addWidget(lineEdit);
			frameInnerLayout.addLayout(frameLine);
			
			infoLineEdits.add(lineEdit);
			infoLineEditsItems.put(lineEdit, i);
		}
	}
	
	public void callbackSetStatus(String msg) {
		System.out.println("FIXME callbackSetStatus");
	}
	
	public void callbackSearchError(String msg) {
		System.out.println("FIXME callbackSearchError");
	}
	
	public void callbackSearchResults(List<ServerRecord> servers) {
		System.out.println("Recebidos resultados do IServerFinder");
		resultServerRecords = servers;
		// Ja foi carregado ok antes
		if (infoNeeded) accept();
	}
	
	// IHostConfigListener
	public void callbackConfigResults(HostRecord record) {
		System.out.println("Recebidos resultados do HostConfig");
		resultHostRecord = record;
		// Ja foi carregado ok antes
		if (infoNeeded || (connectionHint != null)) accept();
	}
	
	// IMobihocListener
	/*public void log(String log) {
		System.out.println("MobihocLog (ConnectionSetupDialog): " + log);
	}*/
	
}

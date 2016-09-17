package game;

import game.entity.*;

import java.util.*;
import java.io.*;

import com.trolltech.qt.*;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import mobihoc.api.IMobihocApp;
import mobihoc.api.MobihocClient;
import mobihoc.api.MobihocClientListener;
import mobihoc.session.DataUnit;
import mobihoc.session.messages.PublishReqMessage;
import mobihoc.session.UserAgent;
import mobihoc.network.client.ServerRecord;
import mobihoc.annotation.*;
import mobihoc.session.Phi;


@PhiAnnotation(
	zones = 3,
	zoneRange = {40, 100, -1},
	theta = {3, 15, 30},
	sigma = {0, 3, 10},
	niu = {0, 200, 900}
)

public class GameWidget extends QWidget implements IMobihocApp {

	private final int blockSize = 20;

	private MobihocClient _client;
	private List<GameEntity> _gameEntities = new LinkedList<GameEntity>();
	private List<ScoreEntity> _scoreEntities = new LinkedList<ScoreEntity>();
	private GameEntity _playerEntity;
	private GameEntity _localFoodEntity;
	private ScoreEntity _playerScore;
	private QGraphicsScene scene;
	private Random random = new Random();
	private QListWidget _scoreListWidget = new QListWidget();
	private QGraphicsView view;
	private UserAgent ua;
	public Signal0 restartRequested = new Signal0();

	// FIXME: Atenção, mapa está (y,x)!
	private int[][] map = { {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
				{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
				{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
				{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
				{1,0,0,0,0,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,2,0,0,0,2,0,0,0,0,1},
				{1,0,0,0,0,2,0,0,0,2,0,0,0,0,0,0,0,0,0,0,2,0,0,0,2,0,0,0,0,1},
				{1,0,0,0,0,2,0,0,0,2,0,0,0,0,0,0,0,0,0,0,2,0,0,0,2,0,0,0,0,1},
				{1,0,0,0,0,2,0,0,0,2,0,0,0,0,0,0,0,0,0,0,2,0,0,0,2,0,0,0,0,1},
				{1,0,0,0,0,2,0,0,0,2,0,0,0,0,0,0,0,0,0,0,2,2,2,2,2,0,0,0,0,1},
				{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
				{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
				{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
				{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1} };
	
	
	public GameWidget(QWidget parent, ServerRecord sr) {
		super(parent);
		QPair<Integer, Integer> pos;

		_client = new MobihocClient(new MobihocClientListener(this));
		
		// Pedir nickname ao utilizador
		String nick = QInputDialog.getText(this, "Please insert your nickname", "Please insert your nickname", QLineEdit.EchoMode.Normal, "User " + new Long(random.nextInt(10000)).toString());
		ua = new UserAgent(nick);

		// Efectuar ligação
		_client.subscribe(sr, ua);
		
		// Gui stuff
		System.out.println("GameWidget criada");
		_scoreListWidget.setMaximumWidth(120);
		scene = new QGraphicsScene(this);
		scene.setSceneRect(0, 0, map[0].length * blockSize, map.length * blockSize);
		//scene.setSceneRect(0, 0, 300, 260);
		//scene.setSceneRect(0, 0, 700, 400);
		
		scene.setItemIndexMethod(QGraphicsScene.ItemIndexMethod.NoIndex);
		
		// Criar DataUnit que representa a GameEntity no Mobihoc
		{
		pos = getEmptySpace();
		GameEntity ent = new GameEntity(pos.first, pos.second, ua.getNickname(), (short)random.nextInt(256), (short)random.nextInt(256), (short)random.nextInt(256), _client);
		_playerEntity = ent;
		scene.addItem(ent);

		_gameEntities.add(ent);
		}

		// Criar FoodEntity
		{
		pos = getEmptySpace();
		GameEntity ent = new FoodEntity(pos.first, pos.second, "FoodDataUnit", (short)0, (short)0, (short)0, _client);
		_localFoodEntity = ent;
		scene.addItem(ent);
		
		_gameEntities.add(ent);
		}

		// Criar playerScore
		{
		ScoreEntity ent = new ScoreEntity(0, ua.getNickname(), _client);
		_playerScore = ent;

		ent.scoreChanged.connect(this, "updateScores()");

		_scoreEntities.add(ent);
		}

		populateMap(map);
		//generateMap(0, 0, 300, 260);
		//generateMap(0, 0, 700, 400);
		
		view = new QGraphicsView(scene);
		view.setRenderHint(QPainter.RenderHint.Antialiasing);
		//view.setBackgroundBrush(new QBrush(new QPixMap("classpath:/file.png");
		view.setCacheMode(new QGraphicsView.CacheMode(QGraphicsView.CacheModeFlag.CacheBackground));
		view.setSceneRect(0, 0, map[0].length * blockSize, map.length * blockSize);
		view.setMaximumSize(map[0].length * blockSize + 6, map.length * blockSize + 6);
		view.setMinimumSize(map[0].length * blockSize + 6, map.length * blockSize + 6);
		
		// Criação de form
		QBoxLayout layout = new QVBoxLayout();
		layout.addWidget(view);
		setLayout(layout);

		QHBoxLayout hbox = new QHBoxLayout();
		QGridLayout grid = new QGridLayout();

		QPushButton mLeft = new QPushButton("<");
		mLeft.setMaximumSize(28,28);
		mLeft.clicked.connect(this, "moveLeft()");
		QPushButton mRight = new QPushButton(">");
		mRight.setMaximumSize(28,28);
		mRight.clicked.connect(this, "moveRight()");
		QPushButton mUp = new QPushButton("/\\");
		mUp.setMaximumSize(28,28);
		mUp.clicked.connect(this, "moveUp()");
		QPushButton mDown = new QPushButton("\\/");
		mDown.setMaximumSize(28,28);
		mDown.clicked.connect(this, "moveDown()");
		grid.addWidget(mLeft,1,0);
		grid.addWidget(mRight,1,2);
		grid.addWidget(mUp,0,1);
		grid.addWidget(mDown,2,1);
		
		QPushButton controlGameButton = new QPushButton(tr("Start &Game"));
		controlGameButton.clicked.connect(this, "startGame()");
		
		QPushButton saveGameButton = new QPushButton(tr("&Save Game"));
		saveGameButton.clicked.connect(this, "saveGame()");
		
		QPushButton loadGameButton = new QPushButton(tr("&Load Game"));
		loadGameButton.clicked.connect(this, "loadGame()");
		
		QPushButton showConsisButton = new QPushButton(tr("Show &Consist. Zones"));
		showConsisButton.toggled.connect(this, "showConsistencyZones(Boolean)");
		showConsisButton.setCheckable(true);

		hbox.addLayout(grid);
		hbox.addStretch();
		hbox.addWidget(_scoreListWidget);
		QVBoxLayout vbox = new QVBoxLayout();
		vbox.addWidget(controlGameButton);
		vbox.addWidget(saveGameButton);
		vbox.addWidget(loadGameButton);
		vbox.addWidget(showConsisButton);
		hbox.addLayout(vbox);
		layout.addLayout(hbox);
		
		setWindowTitle(tr("Game-teste"));

		// Necessário para fazer com que o publish seja feito de uma só vez, enquanto o Mobihoc não suporta
		// múltiplos publishes
		_client.flushDelayedPublish();
	}
	
	public void startGame() {
		_client.enable();
	}
	
	public void saveGame() {
		String s = QFileDialog.getSaveFileName();
		_client.save(s);
	}

	public void loadGame() {
		String s = QFileDialog.getOpenFileName();
		_client.load(s);
	}

	public void reconnectGame() {
		_client.reconnect();
	}

	private boolean isEmptySpace(int x, int y) {
		// FIXME: Atenção, mapa está (y,x)!
		if (map[(int)(y / blockSize)][(int)(x / blockSize)] == 0) return true;
		return false;
	}

	private QPair<Integer, Integer> getEmptySpace() {
		int x;
		int y;
		do {
			x = blockSize + (random.nextInt(map[0].length - 2) * blockSize);
			y = blockSize + (random.nextInt(map.length - 2) * blockSize);
		} while (!isEmptySpace(x,y));
		return new QPair<Integer, Integer>(new Integer(x), new Integer(y));
	}
	
	private void moveEntity(int relX, int relY) {

		int newX = _playerEntity.getPosX() + relX;
		int newY = _playerEntity.getPosY() + relY;

		if (!isEmptySpace(newX, newY)) {
			System.out.println("Collision!");
			return;
		}

		// Testar por objectos
		for (GameEntity ent : _gameEntities) {
			if (ent == _playerEntity) continue;
			if ((ent.getPosX() == newX) && (ent.getPosY() == newY)) {
				System.out.println("Collision with Entity!");

				if (ent instanceof FoodEntity) {
					// Actualizar o score
					_playerScore.increaseScore(1);

					// Recolocar FoodEntity
					QPair<Integer, Integer> pos = getEmptySpace();
					ent.setPosition(pos.first, pos.second);
				} else {	// Não permitir movimento
					return;
				}
			}
		}

		_playerEntity.setPosition(newX, newY);
	}
	
	public void moveLeft() {
		moveEntity(-blockSize,0);
	}
	
	public void moveRight() {
		moveEntity(+blockSize,0);
	}
	
	public void moveUp() {
		moveEntity(0,-blockSize);
	}
	
	public void moveDown() {
		moveEntity(0,+blockSize);
	}

	private boolean addEntity(Object o) {
		if (o instanceof GameEntity) {
			GameEntity ent = (GameEntity)o;
			ent.setAdversary(true);
			
			scene.addItem(ent);
			_gameEntities.add(ent);
			return true;
		}
		/*if (o instanceof FoodEntity) {
			FoodEntity_DataUnit edu = (FoodEntity_DataUnit)du;
			GameEntity ent;
			ent = new FoodEntity(edu, _client);

			scene.addItem(ent);
			_gameEntities.add(ent);
			return true;
		}*/
		if (o instanceof ScoreEntity) {
			ScoreEntity ent = (ScoreEntity)o;
			ent.scoreChanged.connect(this, "updateScores()");
			
			_scoreEntities.add(ent);
			updateScores();
			return true;
		}

		System.out.println("Error: Received an unknown Game Object");
		return false;
	}
	
	public void handleNewDataObjects(List<Object> objs) {
		System.out.println(" HandleNewData!");
		for (Object o : objs) {
			addEntity(o);
		}
	}
	
	public void handleLoadStateObjects(List<Object> myObjs, List<Object> objs) {
		System.out.println(" HandleLoadState!");
		// Limpar GameEntities
		for (GameEntity ge : _gameEntities) {
			scene.removeItem(ge);	// Remover GE da cena
			ge.dispose();		// Marcar objecto para ser delete'd pela Qt
		}
		_gameEntities.clear();
		// Limpar ScoreEntities
		_scoreEntities.clear();

		// Outros objectos
		handleNewDataObjects(objs);

		// Objectos nossos
		for (Object o : myObjs) {
			if (addEntity(o)) {
				// Objecto é nosso
				// Nota: Assumimos aqui que só temos um objecto de cada tipo neste momento
				if (o instanceof ScoreEntity) {
					_playerScore = (ScoreEntity)o;
				} else if (o instanceof FoodEntity) {
					_localFoodEntity = (FoodEntity)o;
				} else if (o instanceof GameEntity) {
					_playerEntity = (GameEntity)o;
					_playerEntity.setAdversary(false);
				}
			}
		}
	}

	// Itera sobre os scores na lista de scores e coloca-os numa qlistwidget
	public void updateScores() {
		//System.out.println(" updateScores()");
		List<ScoreEntity> lst = new LinkedList<ScoreEntity>(_scoreEntities);
		Collections.sort(lst);
		_scoreListWidget.clear();
		for (int i = lst.size()- 1; i >= 0; i--) {
			ScoreEntity ent = lst.get(i);
			QListWidgetItem item = new QListWidgetItem(ent.getNick() + ": " + ent.getScore(), _scoreListWidget);
			if (ent == _playerScore) { // Por o item do player local a bold
				QFont font = item.font();
				font.setBold(true);
				item.setFont(font);
				QBrush brush = item.foreground();
				brush.setStyle(Qt.BrushStyle.SolidPattern);
				brush.setColor(QColor.blue);
				item.setForeground(brush);
			} /*else {
				QBrush brush = item.foreground();
				brush.setStyle(Qt.BrushStyle.SolidPattern);
				brush.setColor(QColor.red);
				item.setForeground(brush);
			}*/
		}
	}
	
	public void handleStateUpdated() {
		for (GameEntity ent : _gameEntities) {
			ent.dataUnitChanged();
		}
		for (ScoreEntity ent : _scoreEntities) {
			ent.dataUnitChanged();
		}
		// Forçar update ao QGraphicsView, para tentar resolver problema que às vezes não actualiza
		view.repaint();
	}

	public void callbackStateUpdated() {
		final GameWidget gw = this;
		QApplication.invokeLater(new Runnable() {public void run() { gw.handleStateUpdated(); }});
	}
	
	// Necessário para ir ter à thread que tem o event loop
	@CallOnNewData
	public void callbackNewDataObjects(List<Object> objs) {
		final List<Object> objsToGo = objs;
		final GameWidget gw = this;
		QApplication.invokeLater(new Runnable() {public void run() { gw.handleNewDataObjects(objsToGo); }});
	}

	// Necessário para ir ter à thread que tem o event loop
	@CallOnLoadState
	public void callbackLoadStateObjects(List<Object> myObjs, List<Object> objs) {
		final List<Object> myObjsToGo = myObjs;
		final List<Object> objsToGo = objs;
		final GameWidget gw = this;
		QApplication.invokeLater(new Runnable() {public void run() { gw.handleLoadStateObjects(myObjsToGo, objsToGo); }});
	}

	@InjectedMethod
	public void callbackNewData(List<DataUnit> dus) {
		//callbackNewDataObjects(mobihoc.asm.DuConverter.dataUnitToClientObj(dus, getMobihocClient()));
	}

	@InjectedMethod
	public void callbackLoadState(List<DataUnit> myDus, List<DataUnit> dus) {
		//callbackLoadStateObjects(mobihoc.asm.DuConverter.dataUnitToClientObj(myDus, getMobihocClient()), mobihoc.asm.DuConverter.dataUnitToClientObj(dus, getMobihocClient()));
	}

	public void callbackError(String error) {
		final GameWidget gw = this;
		final String errorMsg = error;
		QApplication.invokeLater(new Runnable() {public void run() { QMessageBox.information(gw, "Error", errorMsg, QMessageBox.StandardButton.Close); }});
	}

	public void callbackConnClosed(String error) {
		final GameWidget gw = this;
		final String errorMsg = error;
		QApplication.invokeLater(new Runnable() {public void run() { gw.decideFuture(); }});

	}

	public MobihocClient getMobihocClient() {
		return _client;
	}

	private void decideFuture() {
		ReconnectDialog dialog = new ReconnectDialog(this);
		dialog.reconnect.connect(this, "reconnectGame()");
		dialog.select.connect(this, "restart()");
		dialog.exec();
	}

	private void restart() {
		//System.out.println("FIXME: Voltar ao inicio, escolher a coneccao, ...");
		//System.exit(1);
		restartRequested.emit();
	}

	/**
	* Types of tiles:
	* 0 - space
	* 1 - brick
	* 2 - crate
	* 3 - eatable
	* 4 - unknown
	**/
	public void populateMap(int[][] map) {
		int length = map[0].length;
		int height = map.length;
		System.out.println("Populate map called for a map with the following dimensions: " + length + " by " + height);
		// FIXME: Atenção, mapa está (y,x)!
 		for (int i = 0; i < height; i++) {
			for (int j = 0; j < length; j++) {
				addTileToScene(map[i][j], j*blockSize, i*blockSize);
			}
		}
	}
	
	public void addTileToScene(int type, int x, int y) {
		QPen basicPen = new QPen();
		basicPen.setStyle(Qt.PenStyle.SolidLine);
		basicPen.setWidth(4);
		basicPen.setColor(new QColor("black"));
		basicPen.setCapStyle(Qt.PenCapStyle.RoundCap);
		basicPen.setJoinStyle(Qt.PenJoinStyle.RoundJoin);
		QBrush brickBrush = new QBrush(new QColor("darkRed"));
		QBrush crateBrush = new QBrush(new QColor("yellow"));
		QBrush eatableBrush = new QBrush(new QColor("cyan"));
		QBrush unknownBrush = new QBrush(new QColor("darkBlue"));
		
		switch (type) {
			case 1: scene.addRect(new QRectF(x, y, blockSize, blockSize), basicPen, brickBrush); break;
			case 2: scene.addRect(new QRectF(x, y, blockSize, blockSize), basicPen, crateBrush); break;
			case 3: scene.addRect(new QRectF(x, y, blockSize, blockSize), basicPen, eatableBrush); break;
			case 4: scene.addRect(new QRectF(x, y, blockSize, blockSize), basicPen, unknownBrush); break;
			default: break;
		}
	}

	public int[][] renderMap(String filePath) {
		List<List<Integer>> integerMap = new ArrayList<List<Integer>>();
		try {
			FileReader reader = new FileReader(filePath);
			BufferedReader br = new BufferedReader(reader);
			for (String fileLine = br.readLine(); fileLine != null; fileLine = br.readLine()) {
				StringTokenizer st = new StringTokenizer(fileLine);
				List<Integer> mapLine = new ArrayList<Integer>();
				while (st.hasMoreTokens()) {
					mapLine.add(new Integer(st.nextToken()));
				}
				integerMap.add(mapLine);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.out.println("renderMap " + e);
		} catch (IOException e) {
			System.out.println("renderMap " + e);
		}

		int[][] map = new int [integerMap.size()][];
		for (int i = 0; i < integerMap.size() ; i++) {
			int[] aux = new int[integerMap.get(i).size()];
			for (int j = 0; j < integerMap.get(i).size() ; j++) {
				aux[j] = integerMap.get(i).get(j).intValue();
			}
			map[i] = aux;
		}
		return map;
	}
	
	public UserAgent getUserAgent() {
		return ua;
	}
	
	public void showConsistencyZones(Boolean opt) {
		// Obter raio de consistência
		PhiAnnotation anot = getClass().getAnnotation(PhiAnnotation.class);
		/*int cRadius = 0;
		if (anot.zoneRange().length >= 2) cRadius = anot.zoneRange()[anot.zoneRange().length - 2];
		else cRadius = anot.zoneRange()[0];
		
		_playerEntity.setShowConsistencyZone(opt, cRadius);
		_localFoodEntity.setShowConsistencyZone(opt, cRadius);
		*/
		_playerEntity.setShowConsistencyZone(opt, anot.zoneRange());
		_localFoodEntity.setShowConsistencyZone(opt, anot.zoneRange());
	}

}

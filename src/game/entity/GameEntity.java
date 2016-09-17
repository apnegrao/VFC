package game.entity;

import game.*;

import java.util.*;
import java.lang.Math;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import mobihoc.annotation.*;

@Data(
	pivot = true,
	positionable = true
)
public class GameEntity extends QGraphicsItem {

	private QFontMetricsF metrics;
	private QFont font;
	private boolean _adversary = false;
	private QPolygon triangle;
	private boolean _showConsistencyZone = false;
	private int _consistencyRadius = 0;
	private int[] _consistencyZones;
	protected QColor color;

	private final int blockSize = 20;

	@DataField
	private int _posX;
	@DataField
	private int _posY;
	@DataField
	private String _label;
	@DataField
	private	short _colorR;
	@DataField
	private short _colorG;
	@DataField
	private short _colorB;

	@InstanceInitializer
	protected void initializeInstance() {
		triangle = new QPolygon();
		triangle.add(blockSize/2 , 0);
		triangle.add(0, blockSize);
		triangle.add(blockSize, blockSize);

		color = new QColor(getColorR(), getColorG(), getColorB());
		setPos(getPosX(), getPosY());
		font = QApplication.font();
		font.setBold(true);
		font.setPointSize(font.pointSize() + 2);
		metrics = new QFontMetricsF(font);
		// A entity deve ficar por cima de tudo
		setZValue(1000);
	}

	private QRectF textRect() {
		int Padding = 4;
		QRectF rect = metrics.boundingRect(fullLabel());
		rect.adjust(-Padding, -Padding, +Padding, +Padding);
		// -rect.center() , sem operator overloading
		rect.translate(rect.center().multiply(-1));
		return rect;
	}

	private QRectF shapeRect() {
		return new QRectF(0, 0, blockSize, blockSize);
	}

	private QRectF consistencyZoneRect(int radius) {
		return new QRectF(-radius, -radius, blockSize + 2*radius, blockSize + 2*radius);
	}

	public QRectF boundingRect() {
		if (!_showConsistencyZone) {
			return textRect().united(shapeRect());
		} else {
			return textRect().united(shapeRect()).united(consistencyZoneRect(_consistencyRadius));
		}
	}

	// Usado para detecção de colisões
	public QPainterPath shape() {
		QPainterPath p = new QPainterPath();
		p.addRect(shapeRect());
		return p;
	}
	
	public void paint(QPainter painter, QStyleOptionGraphicsItem styleOptionGraphicsItem, QWidget widget) {
		if (!_adversary) { // Player local
			painter.setBrush(color);
			painter.drawRect(shapeRect());
			painter.setFont(font);
			painter.setPen(QColor.white);
			painter.drawText(textRect().translated(+1,+1), fullLabel(), new QTextOption(new Qt.Alignment(Qt.AlignmentFlag.AlignCenter)));
			painter.setPen(QColor.blue);
			painter.drawText(textRect(), fullLabel(), new QTextOption(new Qt.Alignment(Qt.AlignmentFlag.AlignCenter)));
		} else {
			painter.setBrush(color);
			painter.drawPolygon(triangle);
			painter.setFont(font);
			painter.setPen(QColor.white);
			painter.drawText(textRect().translated(+1,+1), fullLabel(), new QTextOption(new Qt.Alignment(Qt.AlignmentFlag.AlignCenter)));
			painter.setPen(QColor.red);
			painter.drawText(textRect(), fullLabel(), new QTextOption(new Qt.Alignment(Qt.AlignmentFlag.AlignCenter)));
		}
		drawConsistencyZone(painter, styleOptionGraphicsItem, widget);
	}

	protected void drawConsistencyZone(QPainter painter, QStyleOptionGraphicsItem styleOptionGraphicsItem, QWidget widget) {
		if (_showConsistencyZone) {
			painter.setBrush(new QColor(0, 0, 255, 64));
			for (int i = 0; i < _consistencyZones.length - 1; i++) {
				painter.drawRect(consistencyZoneRect(_consistencyZones[i]));
			}
			
		}
	}

	public void setAdversary(boolean adversary) {
		_adversary = adversary;
	}
	
	@CallOnUpdate
	public void dataUnitChanged() {
		synchronized(this) {
			/*String newLabel = fullLabel();
			if (!newLabel.equals(label)) {
				label = newLabel;*/
				prepareGeometryChange();
			/*}*/
			// x() e y() vêm do QGraphicsItem
			if ((getPosX() == x()) && (getPosY() == y())) {
				//System.out.println("dataUnitChanged() -- no new data");
				return;
			}
			System.out.println("dataUnitChanged() -- position changed");
			setPos(getPosX(), getPosY());
			// Tentar forçar update
			update();
		}
	}

	private String fullLabel() {
		return "[" + getLabel() + "]";
	}

	// Atenção que o QGraphicsItem já tem um setPos
	public void setPosition(int newX, int newY) {
		if (newX != getPosX()) {
			setPosX(newX);
		}
		if (newY != getPosY()) {
			setPosY(newY);
		}
	}

	public void setShowConsistencyZone(Boolean opt, int[] zones) {
		prepareGeometryChange();
		_showConsistencyZone = opt;
		if (zones.length >= 2) _consistencyRadius = zones[zones.length - 2];
		else _consistencyRadius = zones[0];
		_consistencyZones = zones;
		update();
	}
	
	// Método usado para comparar os DataUnits que são gerados
	// Os parametros de entrada têm que estar na mesma ordem e mesmo numero que estão declarados no ficheiro
	@NiuComparator
	private static float niuComparator(int posX1, int posY1, String l1, short r1, short g1, short b1, int posX2, int posY2, String l2, short r2, short g2, short b2) {
		float distance = (float)(Math.sqrt(Math.pow((posX1 - posX2), 2) + Math.pow((posY1 - posY2), 2)));
		return (distance / 20) * 100;
	}

	@InjectedMethod	public int getPosX() { return _posX; }
	@InjectedMethod	public int getPosY() { return _posY; }
	@InjectedMethod	public String getLabel() { return _label; }
	@InjectedMethod	public short getColorR() { return _colorR; }
	@InjectedMethod	public short getColorG() { return _colorG; }
	@InjectedMethod	public short getColorB() { return _colorB; }

	@InjectedMethod	public void setPosX(int newX) { }
	@InjectedMethod	public void setPosY(int newY) { }
	@InjectedMethod	public void setLabel(String newLabel) { }
	@InjectedMethod	public void setColorR(short newColorR) { }
	@InjectedMethod	public void setColorG(short newColorG) { }
	@InjectedMethod	public void setColorB(short newColorB) { }

}

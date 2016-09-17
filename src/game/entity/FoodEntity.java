package game.entity;

import game.*;

import java.util.*;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import mobihoc.annotation.*;

@Data(
	pivot = true,
	positionable = true
)
public class FoodEntity extends GameEntity {

	@InstanceInitializer
	protected void initializeInstance() {
		super.initializeInstance();
		color = new QColor(255, 255, 128);
		setZValue(999); // Fica sempre por baixo de GameEntity's
	}

	private QRectF foodRect() {
		return new QRectF(5, 5, 10, 10);
	}
	
	public void paint(QPainter painter, QStyleOptionGraphicsItem styleOptionGraphicsItem, QWidget widget) {
		//System.out.println("PAINT!");
		painter.setBrush(color);
		painter.drawEllipse(foodRect());
		drawConsistencyZone(painter, styleOptionGraphicsItem, widget);
	}
	
	@NiuComparator
	private static float niuComparator(int posX1, int posY1, String l1, short r1, short g1, short b1, int posX2, int posY2, String l2, short r2, short g2, short b2) {
		float distance = (float)(Math.sqrt(Math.pow((posX1 - posX2), 2) + Math.pow((posY1 - posY2), 2)));
		return (distance / 20) * 100;
	}

}

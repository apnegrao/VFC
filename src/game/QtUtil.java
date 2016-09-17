package game;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

/** Classe com alguns métodos genéricos para trabalhar com a Qt **/
public class QtUtil {

	public static void clearLayout(QLayout layout, int startFrom) {
		QLayoutItemInterface child;
		QLayout nested;
		QWidget widget;
		while ((child = layout.takeAt(startFrom)) != null) {
			widget = child.widget();
			if (widget != null) {
				widget.disposeLater();
				// System.out.println("widget retirada");
			}
			
			nested = child.layout();
			
			if (nested != null) {
				// System.out.println("nested layout");
				clearLayout(nested, 0);
				nested.disposeLater();
			}
		}
	}

}

package game.entity;

import game.*;

import java.util.*;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.*;

import mobihoc.annotation.*;

@Data(
	pivot = false,
	positionable = false
)
public class ScoreEntity extends QSignalEmitter implements Comparable<ScoreEntity> {

	public Signal0 scoreChanged = new Signal0();
	
	@DataField
	private int _score;
	@DataField
	private String _nick;

	@CallOnUpdate
	public void dataUnitChanged() {
		scoreChanged.emit();
	}
	
	public int compareTo(ScoreEntity se) {
		return this.getScore()-se.getScore();
	}
	
	public boolean equals(Object o) {
		return compareTo((ScoreEntity)o) == 0;
	}

	public void increaseScore(int mult) {
		setScore(getScore() + 100*mult);
	}
	
	// Método usado para comparar os DataUnits que são gerados
	// Os parametros de entrada têm que estar na mesma ordem e mesmo numero que estão declarados no ficheiro
	@NiuComparator
	private static float niuComparator(int score1, String nick1, int score2, String nick2) {
		if (score2 == 0) return 0;
		return Math.abs((score2 - score1)/score1 * 100);
	}

	@InjectedMethod	public int getScore() { return _score; }
	@InjectedMethod	public String getNick() { return _nick; }

	@InjectedMethod	public void setScore(int score) { }

}

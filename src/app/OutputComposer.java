package app;

import java.util.*;

/**
 * Classe OutputComposer.
 * Permite preparar um texto, juntando varios excertos e linhas, para depois o obter como uma só string.
 * Principalmente útil em conjunção com a UserInterface, que só aceita strings para apresentar ao utilizador.
 *
 * @author Stoyan Garbatov (nº 55437)
 * @author Ivo Anjo (nº 55460)
 * @author Hugo Rito (nº 55470)
 **/

public class OutputComposer {

	private LinkedList<String> strings = new LinkedList<String>();

	public void addLine(String text) {
		strings.add(text + "\n");
	}

	public void add(String text) {
		strings.add(text);
	}

	public String output() {
		String output = "";
		for (String s : strings) {
			output += s;
		}
		return output;
	}

	public String flush() {
		String output = output();
		strings.clear();
		return output;
	}

}

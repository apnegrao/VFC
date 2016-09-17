package app;

import java.io.*;

/**
 * Classe ConsoleUserInterface.
 * Implementação de interface com o utilizador usando um terminal.
 *
 * @author Stoyan Garbatov (nº 55437)
 * @author Ivo Anjo (nº 55460)
 * @author Hugo Rito (nº 55470)
 **/

public class ConsoleUserInterface extends UserInterface {
	protected static PrintStream out = System.err;
	protected static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	public void messageUser(String message) {
		if (message.trim().compareTo(message) != 0) {	// Não imprimir linhas vazias, ou whitespace extra
			out.print(message);
		} else {
			out.println(message);
		}
	}

	public String getStringFromUser(String message) {
		return getStringFromUser(message, ":");
	}

	public String getStringFromUser(String message, String prompt) {
		try {
			out.print(message + prompt + " ");
			return in.readLine();
		} catch (IOException e) {
			reportError(e);
			return "";
		}
	}

	public void printSeparator() {
		out.println();
	}

	public Integer getIntegerFromUser(String message) {
		return new Integer(getStringFromUser(message));
	}

	public void reportError(String Error) {
		out.println("Error: " + Error);
	}

}

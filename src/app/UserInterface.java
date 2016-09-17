package app;

/**
 * Classe UserInterface.
 * Superclasse das interfaces com o utilizador existentes na aplicação.
 * Permite interagir com o utilizador, enviando-lhe mensagens, pedindo valores e reportando erros.
 *
 * @author Stoyan Garbatov (nº 55437)
 * @author Ivo Anjo (nº 55460)
 * @author Hugo Rito (nº 55470)
 **/

public abstract class UserInterface {

	/** Apresenta mensagem de erro ao utilizador **/
	public abstract void reportError(String Error);
	/** Pede informação, na forma de uma string, ao utilizador **/
	public abstract String getStringFromUser(String message);
	/** Pede informação, na forma de uma string, ao utilizador, permitindo especificar a prompt
        (um valor que pode ser colocado a terminar a string, como ':', '.', etc) **/
	public abstract String getStringFromUser(String message, String prompt);
	/** Pede informação, na forma de um número inteiro, ao utilizador **/
	public abstract Integer getIntegerFromUser(String message);
	/** Apresenta mensagem ao utilizador **/
	public abstract void messageUser(String message);

	/** Apresenta mensagem de erro ao utilizador **/
	public void reportError(Exception e) { reportError(e.toString()); }
	/** Pede informação, na forma de um número inteiro, ao utilizador **/
	public int getIntFromUser(String message) {
		while (true) {
			try {
				return getIntegerFromUser(message).intValue();
			} catch (NumberFormatException e) {
				reportError("Input not valid. A number is expected.");
			}
		}
	}

	/** Permite, em algumas interfaces, como a linha de comandos, imprimir um separador **/
	public void printSeparator() { }

}

package mobihoc.javassist;

/**
 * Classe Log.
 * Funções usadas para logging das acções efectuadas e para debug.
 * Colocar DEBUGMODE a true para ligar output.
 *
 * @author Stoyan Garbatov (nº 55437)
 * @author Ivo Anjo (nº 55460)
 * @author Hugo Rito (nº 55470)
 **/

public class Log {

	public static boolean DEBUGMODE = true;

	private static void output(String type, String str) {
		if (DEBUGMODE) System.out.println(" [" + type + "] " + str);
	}

	public static void debug(String str) {
		output("DEBUG", str);
	}

	public static void code(String str) {
		output("CODE",
			"\n" + str.replace("{","{\n").replace("}","}\n").replace(";",";\n") + " [/CODE]"
		);
	}
	
	public static void error(String str) {
		output("ERROR", str);
	}
	
	public static void exception(Throwable t, String where) {
		output("EXCEPTION", "Caught exception: " + t + " in " + where);
	}
	
	public static void blank(int n) {
		if (DEBUGMODE) for (; n > 0; n--) System.out.println();
	}

}

package mobihoc.asm;

import java.util.*;

/**
 * Classe Slib.
 * Biblioteca de manipulação de Strings *decente*.
 * Versão v0.01
 *
 * @author Ivo Anjo (nº 55460)
 **/

public class Slib {

	public static String capitalize(String s) {
		return s.substring(0,1).toUpperCase() + s.substring(1,s.length());
	}

	public static String plural(int qty) {
		return (qty > 1 ? "s" : "");
	}

	public static String trimBeginning(String s) {
		if (s.charAt(0) == ' ') return (trimBeginning(s.substring(1)));
		return s;
	}
	
	public static List<String> split(String s, String regexp) {
		// Devolver uma ArrayList, para termos implementações das operações opcionais
		// como o remove
		return new ArrayList<String>(Arrays.asList(s.split(regexp)));
	}
	
	public static String join(List<String> lst, String sep) {
		String s = "";
		int lastPos = lst.size()-1;
		for (int i = 0; i < lastPos; i++) {
			s += lst.get(i) + sep;
		}
		s += lst.get(lastPos);
		return s;
	}
	
	public static <T> T last(List<T> lst) {
		return lst.get(lst.size() - 1);
	}
	
	public static <T> List<T> removeLast(List<T> lst) {
		if (lst.size() > 0) lst.remove(lst.size() - 1);
		return lst;
	}

}

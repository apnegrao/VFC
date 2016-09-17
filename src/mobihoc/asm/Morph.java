package mobihoc.asm;

import java.util.*;
import java.io.*;

import mobihoc.javassist.Log;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

public abstract class Morph implements Opcodes {
	private byte ARGLENGTH = 2;

	public void handleMain(String[] args) {
		String targetDir;

		if (args.length != ARGLENGTH) {
			System.err.println("Usage: java " + getName() + " \"<class1> <class2> ... <classn>\" <targetDir>");
			System.exit(1);
		} else {
			targetDir = args[1] + "/";

			// Desligar output de debug
			//Log.DEBUGMODE = false;

			//Log.debug("EntityMorph [targetDir=" + targetDir + ";classes=" + args[0] + "]");
			String[] classes = fileToClass(args[0]).split(" ");
			
			int i = 0;
			Log.blank(1);
			for (String className : classes) {
				System.out.println(getName() + ": Analysing Class " + className);
				try {
					ClassReader cr = new ClassReader(className);
					byte[] output = morph(cr).toByteArray();

					FileOutputStream fos = new FileOutputStream(targetDir + cr.getClassName() + ".class");
					fos.write(output);
					fos.close();
				} catch (IOException e) {
					Log.exception(e, getName() + ":main");
				} catch (InstrumentationException e) {
					Log.exception(e, getName() + ":main");
				}
			}
			Log.blank(1);
		}
	}

	private static String fileToClass(String filename) {
		return filename.replace("/", ".").replace(".class", "");
	}

	abstract protected ClassWriter morph(ClassReader cr) throws IOException, InstrumentationException;

	abstract protected String getName();

	/** Método que obtem nome simples da classe (sem qualificação de package **/
	protected static String getSimpleClassName(String className) {
		return Slib.last(Slib.split(className, "/"));
	}
	
	protected static String getSimpleClassName(ClassReader cr) { return getSimpleClassName(cr.getClassName()); }
	
	/** Método que obtem package de uma classe **/
	protected static String getPackage(String className) {
		List<String> lst = Slib.split(className, "/");
		if (lst.size() <= 1) return "";
		Slib.removeLast(lst);
		return Slib.join(lst, ".");
	}

	protected static String getPackage(ClassReader cr) { return getPackage(cr.getClassName()); }
	
	/** Método que converte um nome de uma classe (ex: java.lang.String) para o formato que o
	  * java utiliza no bytecode (ex: Ljava/lang/String;).
	  *
	  * Nota: Implementação incompleta, não considera arrays ou semelhantes
	  **/
	public static String classNameToBytecodeClassName(String className) {
		return "L" + className.replace(".", "/") + ";";
	}

	/** Método que faz o inverso do classNameToBytecodeClassName, convertendo o nome de uma classe
	  * que o java utiliza no bytecode (ex: Ljava/lang/String;) para o nome normal (ex: java.lang.String).
	  *
	  * Nota: Implementação incompleta, não considera arrays ou semelhantes
	  **/
	public static String bytecodeClassNameToClassName(String bcName) {
		bcName = bcName.substring(1, bcName.length());
		bcName = bcName.substring(0, bcName.length()-1);
		return bcName.replace("/", ".");
	}
	
	/** Método que converte um nome de uma classe em bytecode (ex: Ljava/lang/String;) para um nome no
	  * formato do ASM (ex: java/Lang/String)
	  **/
	public static String bytecodeClassNameToASMClassName(String bcName) {
		return bytecodeClassNameToClassName(bcName).replace(".", "/");
	}

	/** Método que devolve uma lista de todos os métodos de uma classe que estão anotados com a anotação recebida
	  * e que tenham a assinatura especificada, para a classe recebida.
	  **/
	protected static List<InfoMethod> getAnnotatedMethodsWithSignature(String annot, String sig, InfoClass currentClass)
		throws InstrumentationException {
		List<InfoMethod> annotatedMethods = new ArrayList<InfoMethod>();
		for (InfoMethod method : currentClass.getAllMethods()) {
			if (method.hasAnnotation(annot, InfoClass.ClassNameFormat.Normal)) {
				// Verificar se já não achamos outra versão deste método numa subclasse
				boolean alreadyFound = false;
				for (InfoMethod subM : annotatedMethods) {
					if (subM.getName().equals(method.getName())) {
						alreadyFound = true;
						break;
					}
				}
				// Se é a primeira vez, vamos verificar a assinatura e adicionar
				if (!alreadyFound) {
					if (!method.getDesc().equals(sig)) {
						Log.debug("method'" + method.getDesc() + "' sig='" + sig + "'");
						System.err.println("Error: Method '" + method.getName() + "' in class '" + method.getInfoClass().getName() + "' is annotated with @" + annot + " but its signature isn't '" + sig + "'");
						throw new InstrumentationException();
					}
					annotatedMethods.add(method);
				}
			}
		}
		return annotatedMethods;
	}
	
	/** Método que devolve uma lista dos campos (fields) da classe pedida, e das suas superclasses **/
	protected static InfoClass getSuperclassInfoClass(String className) throws IOException {
		if (className.equals("java/lang/Object")) return null;

		ClassReader cr = new ClassReader(className);
		InfoClass currentClass = new InfoClass(className, cr.getSuperName(), InfoClass.ClassNameFormat.ASM);
		
		InfoClassAdapter ca = new InfoClassAdapter(new EmptyVisitor(), currentClass);
		cr.accept(ca, 0);
		currentClass.setSuperclass(getSuperclassInfoClass(cr.getSuperName()));
		
		return currentClass;
	}
	
	/** Método que "normaliza" o nome da variável, ou seja, caso tenha _ no inicio remove-o, e converte
	  * o nome para usar camel casing no inicio.
	  **/
	protected static String normalizeName(String name) {
		if (name.startsWith("_")) {
			name = name.substring(1, name.length());
		}
		return Slib.capitalize(name);
	}
	
	/** Método que retorna o opcode certo para o tipo de variável que se quer fazer return.
	  * Opcodes disponíveis:
	  * - IRETURN para boolean, byte, char, short, int
	  * - LRETURN para long
	  * - FRETURN para float
	  * - DRETURN para double
	  * - ARETURN para referência (objecto ou array)
	  * -  RETURN para métodos void
	  **/
	protected static int getReturnInsn(String fieldType) throws InstrumentationException {
		char c = fieldType.charAt(0);
		switch (c) {
			case 'Z': // boolean
			case 'B': // byte
			case 'C': // char
			case 'S': // short
			case 'I': // int
				return IRETURN;
			case 'J': // long
				return LRETURN;
			case 'F': // float
				return FRETURN;
			case 'D': // double
				return DRETURN;
			case '[': // Algum tipo de array
			case 'L': // objecto
				return ARETURN;
			case 'V': // void
				return RETURN;
		}
		throw new InstrumentationException("Unknown fieldType in getReturnInsn");
	}

	/** Método que retorna o opcode certo para o tipo de variável que se quer fazer load.
	  * Opcodes disponíveis:
	  * - ILOAD para boolean, byte, char, short, int
	  * - LLOAD para long
	  * - FLOAD para float
	  * - DLOAD para double
	  * - ALOAD para referência (objecto ou array?) FIXME: Arrays precisam de instrucção especial?
	  **/
	protected static int getLoadInsn(String fieldType) throws InstrumentationException {
		char c = fieldType.charAt(0);
		switch (c) {
			case 'Z': // boolean
			case 'B': // byte
			case 'C': // char
			case 'S': // short
			case 'I': // int
				return ILOAD;
			case 'J': // long
				return LLOAD;
			case 'F': // float
				return FLOAD;
			case 'D': // double
				return DLOAD;
			case '[': // Algum tipo de array
				throw new InstrumentationException("No support for arrays on getLoadInsn yet");
			case 'L': // objecto
				return ALOAD;
		}
		throw new InstrumentationException("Unknown fieldType in getLoadInsn");
	}
	
	/** Método que retorna o opcode certo para o tipo de variável que se quer fazer pop na stack.
	  * Opcodes disponíveis:
	  * - POP2 para long, double
	  * - POP para todos os restantes
	  **/
	protected static int getPopInsn(String fieldType) {
		char c = fieldType.charAt(0);
		switch (c) {
			case 'J': // long
			case 'D': // double
				return POP2;
			default:
				return POP;
		}
	}

}

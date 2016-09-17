package mobihoc.asm;

import java.util.*;
import java.io.*;

import mobihoc.javassist.Log;

import org.objectweb.asm.*;

/** Adapter que não propaga a visita os métodos que estiverem na lista que recebeu, efectivamente "apagando"
  * os métodos da classe.
  **/
public class RemoveMethodsAdapter extends ClassAdapter {

	private List<String> _methodNames;

	public RemoveMethodsAdapter(ClassVisitor cv, List<String> methodNames) {
		super(cv);
		_methodNames = methodNames;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (_methodNames.contains(name)) {
			// Log.debug("Classe ja tem metodo " + name + ", vai ser removido");
			return null;
		}
		return cv.visitMethod(access, name, desc, signature, exceptions);
	}

}

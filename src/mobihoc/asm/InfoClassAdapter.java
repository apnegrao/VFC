package mobihoc.asm;

import java.util.*;
import java.io.*;

import mobihoc.javassist.Log;

import org.objectweb.asm.*;

public class InfoClassAdapter extends ClassAdapter {

	private InfoClass _infoClass;

	public InfoClassAdapter(ClassVisitor cv, InfoClass infoClass) {
		super(cv);
		_infoClass = infoClass;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		for (String s : interfaces) {
			_infoClass.addInterface(s);
		}
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		_infoClass.addAnnotation(desc);
		return cv.visitAnnotation(desc, visible);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		InfoMethod method = new InfoMethod(access, name, desc, signature, exceptions, _infoClass);
		_infoClass.addMethod(method);
		return new ReadAnnotationsMethodVisitor(method);
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		InfoField field = new InfoField(access, name, desc, signature, value, _infoClass);
		_infoClass.addField(field);
		return new ReadAnnotationsFieldVisitor(field);
	}

	public InfoClass getInfoClass() {
		return _infoClass;
	}

}

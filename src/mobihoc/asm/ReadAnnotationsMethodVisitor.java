package mobihoc.asm;

import java.util.*;
import java.io.*;

import mobihoc.javassist.Log;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

public class ReadAnnotationsMethodVisitor extends EmptyVisitor {

	InfoMethod _method;

	public ReadAnnotationsMethodVisitor(InfoMethod method) {
		_method = method;
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		_method.addAnnotation(desc);
		return this;
	}

	public AnnotationVisitor visitAnnotation(String name, String desc) {
		return this;
	}

	public AnnotationVisitor visitAnnotationDefault() {
		return this;
	}
	
	public AnnotationVisitor visitArray(String name) {
		return this;
	}

}

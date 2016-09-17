package mobihoc.asm;

import java.util.*;
import java.io.*;

import mobihoc.javassist.Log;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

public class ReadAnnotationsFieldVisitor extends EmptyVisitor {

	InfoField _field;

	public ReadAnnotationsFieldVisitor(InfoField field) {
		_field = field;
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		_field.addAnnotation(desc);
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

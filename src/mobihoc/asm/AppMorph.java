package mobihoc.asm;

import java.util.*;
import java.io.*;

import mobihoc.javassist.Log;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

public class AppMorph extends Morph {

	public static void main(String[] args) {
		new AppMorph().handleMain(args);
	}

	protected String getName() { return "AppMorph"; }
	
	protected ClassWriter morph(ClassReader cr) throws IOException, InstrumentationException {
		FieldVisitor fv;
		MethodVisitor mv;

		// Para não ter que estar sempre a escrever
		InfoClass.ClassNameFormat fASM = InfoClass.ClassNameFormat.ASM;
		InfoClass.ClassNameFormat fNormal = InfoClass.ClassNameFormat.Normal;
		
		InfoClass currentClass = new InfoClass(cr.getClassName(), cr.getSuperName(), fASM);

		InfoClassAdapter ca = new InfoClassAdapter(new EmptyVisitor(), currentClass);

		cr.accept(ca, 0);

		if (!currentClass.hasInterface("mobihoc.api.IMobihocApp", fNormal)) {
			System.err.println("Error: Class '" + currentClass.getName() + "' doesn't implement mobihoc.api.IMobihocApp, not making any changes.");
			throw new InstrumentationException();
		}

		// Verificar que o método callbackNewData e callbackLoadState estão anotados com @mobihoc.annotation.InjectedMethod
		List<InfoMethod> methods = currentClass.getAllMethods();
		String callbackNewData = "callbackNewData";
		String callbackLoadState = "callbackLoadState";
		for (InfoMethod m : methods) {
			if ((m.getName().equals(callbackNewData) || m.getName().equals(callbackLoadState))
				&& !m.hasAnnotation("mobihoc.annotation.InjectedMethod", fNormal)) {
				System.err.println("Warning: Method '" + m.getName() + "' is going to be replaced by Mobihoc, but it is not annotated with @InjectedMethod.");
			}
		}

		String listType = classNameToBytecodeClassName("java.util.List");
		String mobihocClient = classNameToBytecodeClassName("mobihoc.api.MobihocClient");

		// Criar lista com métodos anotados com @CallOnNewData, e verifica se têm o formato necessário
		List<InfoMethod> annotatedCallOnNewDataMethods = getAnnotatedMethodsWithSignature("mobihoc.annotation.CallOnNewData", "(" + listType + ")V", currentClass);
		if (annotatedCallOnNewDataMethods.size() > 1) {
			System.err.println("Error: Found multiple methods annotated with @CallOnNewData.");
			throw new InstrumentationException();
		} else if (annotatedCallOnNewDataMethods.size() == 0) {
			System.err.println("Error: Didn't find a method annotated with @CallOnNewData.");
			throw new InstrumentationException();
		}
		InfoMethod callOnNewDataMethod = annotatedCallOnNewDataMethods.get(0);
		// Criar lista com métodos anotados com @CallOnLoadState, e verifica se têm o formato necessário
		List<InfoMethod> annotatedCallOnLoadStateMethods = getAnnotatedMethodsWithSignature("mobihoc.annotation.CallOnLoadState", "(" + listType + listType + ")V", currentClass);
		if (annotatedCallOnLoadStateMethods.size() > 1) {
			System.err.println("Error: Found multiple methods annotated with @CallOnLoadState.");
			throw new InstrumentationException();
		} else if (annotatedCallOnLoadStateMethods.size() == 0) {
			System.err.println("Error: Didn't find a method annotated with @CallOnLoadState.");
			throw new InstrumentationException();
		}
		InfoMethod callOnLoadStateMethod = annotatedCallOnLoadStateMethods.get(0);

		// Criar lista com os nomes dos getters e setters a serem adicionados, para utilizar juntamente
		// com um ClassAdapter, de forma a remover os metodos, se já existirem
		List<String> methodsToBeRemoved = new ArrayList<String>();
		methodsToBeRemoved.add(callbackNewData);
		methodsToBeRemoved.add(callbackLoadState);
		
		// Verificações terminadas, começar processo de geração
		ClassWriter cw = new ClassWriter(0);
		RemoveMethodsAdapter rma = new RemoveMethodsAdapter(cw, methodsToBeRemoved);
		cr.accept(rma, 0);

		{
		mv = cw.visitMethod(ACC_PUBLIC, callbackNewData, "(" + listType + ")V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, cr.getClassName(), "getMobihocClient", "()" + mobihocClient);
		mv.visitMethodInsn(INVOKESTATIC, "mobihoc/asm/DuConverter", "dataUnitToClientObj", "(" + listType + mobihocClient + ")" + listType);
		mv.visitMethodInsn(INVOKEVIRTUAL, cr.getClassName(), callOnNewDataMethod.getName(), "(" + listType + ")V");
		mv.visitInsn(RETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", "L" + cr.getClassName() + ";", null, l0, l1, 0);
		mv.visitLocalVariable("dus", listType, null, l0, l1, 1);
		mv.visitMaxs(3, 2);
		mv.visitEnd();
		}

		{
		mv = cw.visitMethod(ACC_PUBLIC, callbackLoadState, "(" + listType + listType + ")V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 0);
		// Obter 1a lista
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, cr.getClassName(), "getMobihocClient", "()" + mobihocClient);
		mv.visitMethodInsn(INVOKESTATIC, "mobihoc/asm/DuConverter", "dataUnitToClientObj", "(" + listType + mobihocClient + ")" + listType);
		// Obter 2a lista
		mv.visitVarInsn(ALOAD, 2);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, cr.getClassName(), "getMobihocClient", "()" + mobihocClient);
		mv.visitMethodInsn(INVOKESTATIC, "mobihoc/asm/DuConverter", "dataUnitToClientObj", "(" + listType + mobihocClient + ")" + listType);
		// Chamar @CallOnLoadState
		mv.visitMethodInsn(INVOKEVIRTUAL, cr.getClassName(), callOnLoadStateMethod.getName(), "(" + listType + listType + ")V");
		mv.visitInsn(RETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", "L" + cr.getClassName() + ";", null, l0, l1, 0);
		mv.visitLocalVariable("myDus", listType, null, l0, l1, 1);
		mv.visitLocalVariable("dus", listType, null, l0, l1, 2);
		mv.visitMaxs(4, 3);
		mv.visitEnd();
		}

		return cw;
	}

}

package mobihoc.asm;

import java.util.*;
import java.io.*;

import mobihoc.javassist.Log;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

public class EntityMorph extends Morph {

	public static void main(String[] args) {
		new EntityMorph().handleMain(args);
	}

	protected String getName() { return "EntityMorph"; }
	
	protected ClassWriter morph(ClassReader cr) throws IOException, InstrumentationException {
		FieldVisitor fv;
		MethodVisitor mv;
		
		// Para não ter que estar sempre a escrever
		InfoClass.ClassNameFormat fASM = InfoClass.ClassNameFormat.ASM;
		InfoClass.ClassNameFormat fNormal = InfoClass.ClassNameFormat.Normal;

		//Log.debug("EntityMorph::morph class=" + getSimpleClassName(cr) + "(superclass=" + cr.getSuperName() + ")");

		InfoClass currentClass = new InfoClass(cr.getClassName(), cr.getSuperName(), fASM);

		InfoClassAdapter ca = new InfoClassAdapter(new EmptyVisitor(), currentClass);
		
		cr.accept(ca, 0);
		
		// Testar se a classe a examinar tem a anotação @mobihoc.annotation.Data
		if (!currentClass.hasAnnotation("mobihoc.annotation.Data", fNormal)) {
			System.err.println("Error: Class '" + currentClass.getName() + "' is not annotated with @mobihoc.annotation.Data, not making any changes.");
			throw new InstrumentationException();
		}
		
		// Popular informação das superclasses
		currentClass.setSuperclass(getSuperclassInfoClass(cr.getSuperName()));
		
		// Verificar que fields estão anotados na classe
		List<InfoField> fields = currentClass.getAllFields();
		List<InfoField> annotatedFields = new ArrayList<InfoField>();
		String annotatedFieldsSig = "";
		for (InfoField field : fields) {
			if ((field.getAnnotations().size() > 0) &&
				(field.hasAnnotation("mobihoc.annotation.DataField", fNormal))) {

				if (!field.getInfoClass().hasAnnotation("mobihoc.annotation.Data", fNormal)) {
					System.err.println("Warning: Field '" + field.getName() + "' from class '" + field.getInfoClass().getName() + "' is annotated with @DataField, but the class is not annotated with @Data.");
				}
				annotatedFields.add(field);
				annotatedFieldsSig += field.getDesc();
			}
		}
		
		// Verificar que os getters e setters, se existirem, estão anotados com @mobihoc.annotation.InjectedMethod
		for (InfoField field : annotatedFields) {
			String name = normalizeName(field.getName());
			List<InfoMethod> methods = currentClass.getAllMethods();
			String setter = "set" + name;
			String getter = "get" + name;
			for (InfoMethod m : methods) {
				if ((m.getName().equals(setter) || m.getName().equals(getter))
					&& !m.hasAnnotation("mobihoc.annotation.InjectedMethod", fNormal)) {
					System.err.println("Warning: Method '" + m.getName() + "' is going to be replaced by Mobihoc, but it is not annotated with @InjectedMethod.");
				}
			}
		}

		// Verificar Fields e Métodos
		if (
			currentClass.existsAllFields("injected_dataUnit") ||
			currentClass.existsAllFields("injected_client") ||
			currentClass.existsMethod("injected_entityUpdated") ||
			currentClass.existsMethod("injected_createDataUnit") ||
			currentClass.existsMethod("getId")
			) {
			System.err.println("Error: Class '" + currentClass.getName() + "' seems to be instrumented already, not making any changes.");
			throw new InstrumentationException();
		}
		
		// Criar lista com métodos anotados com @CallOnUpdate, e verifica se têm o formato necessário
		List<InfoMethod> annotatedCallOnUpdateMethods = getAnnotatedMethodsWithSignature("mobihoc.annotation.CallOnUpdate", "()V", currentClass);
		// Criar lista com métodos anotados com @InstanceInitializer, e verifica se têm o formato necessário
		List<InfoMethod> annotatedInstanceInitializerMethods = getAnnotatedMethodsWithSignature("mobihoc.annotation.InstanceInitializer", "()V", currentClass);
		
		// Criar lista com os nomes dos getters e setters a serem adicionados, para utilizar juntamente
		// com um ClassAdapter, de forma a remover os metodos, se já existirem
		List<String> methodsToBeRemoved = new ArrayList<String>();
		for (InfoField field : annotatedFields) {
			String name = normalizeName(field.getName());
			String setter = "set" + name;
			String getter = "get" + name;
			methodsToBeRemoved.add(setter);
			methodsToBeRemoved.add(getter);
		}
		
		// Verificações terminadas, começar processo de geração
		ClassWriter cw = new ClassWriter(0);
		RemoveMethodsAdapter rma = new RemoveMethodsAdapter(cw, methodsToBeRemoved);
		cr.accept(rma, 0);
		
		// Tipos mais comuns
		String dataUnitType = classNameToBytecodeClassName(cr.getClassName() + "_DataUnit");
		String mobihocDataUnit = classNameToBytecodeClassName("mobihoc.session.DataUnit");
		String mobihocClient = classNameToBytecodeClassName("mobihoc.api.MobihocClient");

		{
		// Adicionar Campo do tipo NomeClasse_DataUnit chamado injected_dataUnit
		fv = cw.visitField(ACC_PRIVATE, "injected_dataUnit", dataUnitType, null, null);
		fv.visitEnd();
		}
		
		{
		// Adicionar Campo do tipo MobihocClient chamado injected_client
		fv = cw.visitField(ACC_PRIVATE, "injected_client", mobihocClient, null, null);
		fv.visitEnd();
		}
		
		{
		// Adicionar "dummy" variável getId para ser gerado um getter para ela
		InfoField dummyId = new InfoField(0, "id", "I", null, null, null);
		annotatedFields.add(dummyId);
		// Adicionar getters para as variáveis que tinham sido anotadas com mobihoc.annotation.DataField
		for (InfoField field : annotatedFields) {
			String getter = "get" + normalizeName(field.getName());
			mv = cw.visitMethod(ACC_PUBLIC, getter, "()" + field.getDesc(), null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitVarInsn(ALOAD, 0);
			//Log.debug("cr.getClassName()=" + cr.getClassName() + ";dataUnitType=" + dataUnitType + ";fielddesc=" + field.getDesc());
			mv.visitFieldInsn(GETFIELD, cr.getClassName(), "injected_dataUnit", dataUnitType);
			// Chamar metodo sobre injected_dataUnit
			// Nota: formato do segundo argumento é "package/package/class", não deve levar
			// L e ; primeiro
			mv.visitMethodInsn(INVOKEVIRTUAL, bytecodeClassNameToASMClassName(dataUnitType), getter, "()" + field.getDesc());
			// Instrucção de retorno varia conforme o tipo de retorno (ver documentação getReturnInsn)
			mv.visitInsn(getReturnInsn(field.getDesc()));
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "L" + cr.getClassName() + ";", null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		// Remover id dummy da lista
		annotatedFields.remove(dummyId);
		}
		
		{
		// Adicionar setters para as variáveis que tinham sido anotadas com mobihoc.annotation.DataField
		for (InfoField field : annotatedFields) {
			String setter = "set" + normalizeName(field.getName());
			int stackMaxSize = 0;
			// Metodo setter: (tipodados)V -- recebe tipo de dados, devolve void
			mv = cw.visitMethod(ACC_PUBLIC, setter, "(" + field.getDesc() + ")" + "V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			// Colocar this duas vezes na pilha, porque vamos invocar this.entityUpdated(this.createDataUnit(...))
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 0);
			stackMaxSize += 2;
			// Agora temos que colocar o argumento recebido e os restantes argumentos, na pilha, por ordem
			for (InfoField argField : annotatedFields) {
				stackMaxSize++;
				if (field == argField) {
					// Argumento recebido
					mv.visitVarInsn(getLoadInsn(field.getDesc()), 1);
				} else {
					// Invocar this.getField()
					String getter = "get" + normalizeName(argField.getName());
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKEVIRTUAL, cr.getClassName(), getter, "()" + argField.getDesc());
				}
			}
			// Chamar this.createDataUnit
			mv.visitMethodInsn(INVOKEVIRTUAL, cr.getClassName(), "injected_createDataUnit", "(" + annotatedFieldsSig + ")" + dataUnitType);
			// Temos um DataUnit na pilha, chamar entityUpdated
			mv.visitMethodInsn(INVOKEVIRTUAL, cr.getClassName(), "injected_entityUpdated", "(" + mobihocDataUnit + ")V");
			mv.visitInsn(RETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "L" + cr.getClassName() + ";", null, l0, l1, 0);
			mv.visitLocalVariable(field.getName(), field.getDesc(), null, l0, l1, 1);
			// Maxs, 1º valor tamanho máximo da stack local, 2º valor número máximo de variáveis locais
			mv.visitMaxs(stackMaxSize, 2);
			mv.visitEnd();
		}
		}
		
		{
		// Adicionar método injected_createDataUnit(...)
		int stackMaxSize = 0;
		mv = cw.visitMethod(ACC_PROTECTED, "injected_createDataUnit", "(" + annotatedFieldsSig + ")" + dataUnitType, null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitTypeInsn(NEW, bytecodeClassNameToASMClassName(dataUnitType));
		mv.visitInsn(DUP);
		stackMaxSize += 2;
		// Colocar todos os tipos recebidos na pilha
		int pos = 1;
		for (InfoField field : annotatedFields) {
			mv.visitVarInsn(getLoadInsn(field.getDesc()), pos);
			pos++;
			stackMaxSize++;
		}
		mv.visitMethodInsn(INVOKESPECIAL, bytecodeClassNameToASMClassName(dataUnitType), "<init>", "(" + annotatedFieldsSig + ")V");
		mv.visitInsn(ARETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", "L" + cr.getClassName() + ";", null, l0, l1, 0);
		pos = 1;
		for (InfoField field : annotatedFields) {
			//Log.debug("visitLocal field=" + field.getName() + ";desc=" + field.getDesc() + ";pos=" + pos);
			mv.visitLocalVariable(field.getName(), field.getDesc(), null, l0, l1, pos);
			pos++;
		}
		mv.visitMaxs(stackMaxSize, pos);
		mv.visitEnd();
		}
		
		{
		// Adicionar o método injected_entityUpdated(DataUnit du)
		mv = cw.visitMethod(ACC_PRIVATE, "injected_entityUpdated", "(" + mobihocDataUnit + ")V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		// Obter argumento recebido
		mv.visitVarInsn(ALOAD, 1);
		// Obter id do injected_dataUnit
		mv.visitVarInsn(ALOAD, 0); 	// this
		mv.visitFieldInsn(GETFIELD, cr.getClassName(), "injected_dataUnit", dataUnitType); // dataUnit na pilha
		mv.visitMethodInsn(INVOKEVIRTUAL, bytecodeClassNameToASMClassName(mobihocDataUnit), "getId", "()I"); // id na pilha
		// Invocar recebido.setId(...)
		mv.visitMethodInsn(INVOKEVIRTUAL, bytecodeClassNameToASMClassName(mobihocDataUnit), "setId", "(I)V");
		// Tentar agora fazer escrita
		// Obter injected_client
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, cr.getClassName(), "injected_client", mobihocClient);
		// Colocar du na pilha
		mv.visitVarInsn(ALOAD, 1);
		// chamar write(du)
		mv.visitMethodInsn(INVOKEVIRTUAL, bytecodeClassNameToASMClassName(mobihocClient), "write", "(" + mobihocDataUnit + ")Z");
		// Resposta agora na pilha
		Label l1 = new Label();
		mv.visitJumpInsn(IFNE, l1);
		Label l2 = new Label();
		// Branch true if
		mv.visitLabel(l2);
		// Fazer System.out.println("Write Failed (@ Nome classe)");
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		mv.visitLdcInsn("Write Failed (@ " + currentClass.getName() + ")");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
		mv.visitInsn(RETURN);
		// Branch false if
		mv.visitLabel(l1);
		// Isto é uma grande complicação, mas creio-me que está correcto, mantemos a stack frame, não temos variáveis locais nem nada na stack de operandos
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		// dataUnit na pilha
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, cr.getClassName(), "injected_dataUnit", dataUnitType); // dataUnit na pilha
		mv.visitVarInsn(ALOAD, 1); // Obter argumento recebido
		mv.visitMethodInsn(INVOKEVIRTUAL, bytecodeClassNameToASMClassName(dataUnitType), "merge", "(" + mobihocDataUnit + ")V");
		// Chamar funções anotadas com @CallOnUpdate, se existirem
		for (InfoMethod method : annotatedCallOnUpdateMethods) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, bytecodeClassNameToASMClassName(method.getInfoClass().getBytecodeName()), method.getName(), "()V"); // Podemos assumir que assinatura é esta porque isto é verificado lá atrás
		}
		mv.visitInsn(RETURN);
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitLocalVariable("this", "L" + cr.getClassName() + ";", null, l0, l3, 0);
		mv.visitLocalVariable("dataUnit", dataUnitType, null, l0, l3, 1);
		mv.visitMaxs(2, 2);
		mv.visitEnd();
		}
		
		{
		// Adicionar o constructor que recebe um DataUnit e o MobihocClient
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(" + dataUnitType + mobihocClient + ")V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		// Invocar constructor <init>()V, ou seja, this()
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, cr.getClassName(), "<init>", "()V");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitFieldInsn(PUTFIELD, cr.getClassName(), "injected_dataUnit", dataUnitType);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitFieldInsn(PUTFIELD, cr.getClassName(), "injected_client", mobihocClient);
		// Chamar funções anotadas com @InstanceInitializer, se existirem
		for (InfoMethod method : annotatedInstanceInitializerMethods) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, bytecodeClassNameToASMClassName(method.getInfoClass().getBytecodeName()), method.getName(), "()V"); // Podemos assumir que assinatura é esta porque isto é verificado lá atrás
		}
		mv.visitInsn(RETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", "L" + cr.getClassName() + ";", null, l0, l1, 0);
		mv.visitLocalVariable("dataUnit", dataUnitType, null, l0, l1, 1);
		mv.visitLocalVariable("mobihocClient", mobihocClient, null, l0, l1, 2);
		mv.visitMaxs(2, 3);
		mv.visitEnd();
		}

		{
		// Adicionar o constructor que recebe os tipos marcados como @DataField e o MobihocClient
		int stackMaxSize = 0;
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(" + annotatedFieldsSig + mobihocClient + ")V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		// Invocar constructor <init>()V, ou seja, this()
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, cr.getClassName(), "<init>", "()V");
		// Fazer injected_dataUnit = injected_createDataUnit(...)
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 0);
		stackMaxSize += 2;
		// Agora temos que colocar o argumento recebido e os restantes argumentos, na pilha, por ordem
		for (int pos = 0 ; pos < annotatedFields.size() ; pos++) {
			stackMaxSize++;
			mv.visitVarInsn(getLoadInsn(annotatedFields.get(pos).getDesc()), pos+1);
		}
		// Chamar this.createDataUnit
		mv.visitMethodInsn(INVOKEVIRTUAL, cr.getClassName(), "injected_createDataUnit", "(" + annotatedFieldsSig + ")" + dataUnitType);
		// Atribuir DataUnit ao injected_dataUnit
		mv.visitFieldInsn(PUTFIELD, cr.getClassName(), "injected_dataUnit", dataUnitType);

		// Fazer injected_dataUnit.setId(mobihocClient.getNextId())
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, cr.getClassName(), "injected_dataUnit", dataUnitType);
		// Cliente que recebemos está na pilha nesta posição (depois de todos os outros)
		mv.visitVarInsn(ALOAD, annotatedFields.size() + 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, bytecodeClassNameToASMClassName(mobihocClient), "getNextId", "()I");
		// Invocar setId
		mv.visitMethodInsn(INVOKEVIRTUAL, bytecodeClassNameToASMClassName(mobihocDataUnit), "setId", "(I)V");

		// Guardar cliente recebido
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, annotatedFields.size() + 1);
		mv.visitFieldInsn(PUTFIELD, cr.getClassName(), "injected_client", mobihocClient);

		// Fazer mobihocClient.addDelayedPublish(injected_dataUnit)
		mv.visitVarInsn(ALOAD, annotatedFields.size() + 1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, cr.getClassName(), "injected_dataUnit", dataUnitType);
		mv.visitMethodInsn(INVOKEVIRTUAL, bytecodeClassNameToASMClassName(mobihocClient), "addDelayedPublish", "(" + mobihocDataUnit + ")V");

		// Chamar funções anotadas com @InstanceInitializer, se existirem
		for (InfoMethod method : annotatedInstanceInitializerMethods) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, bytecodeClassNameToASMClassName(method.getInfoClass().getBytecodeName()), method.getName(), "()V"); // Podemos assumir que assinatura é esta porque isto é verificado lá atrás
		}
		mv.visitInsn(RETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", "L" + cr.getClassName() + ";", null, l0, l1, 0);
		int pos = 1;
		for (InfoField field : annotatedFields) {
			mv.visitLocalVariable(field.getName(), field.getDesc(), null, l0, l1, pos);
			pos++;
		}
		mv.visitLocalVariable("mobihocClient", mobihocClient, null, l0, l1, pos);
		pos++;
		mv.visitMaxs(stackMaxSize, pos);
		mv.visitEnd();
		}

		return cw;
	}

}

package mobihoc.asm;

import java.util.*;

public class InfoClass {

	public enum ClassNameFormat { Normal, Bytecode, ASM }

	private String _bytecodeName;
	private String _superclassBytecodeName;
	private List<String> _annotations = new ArrayList<String>();
	private List<InfoField> _fields = new ArrayList<InfoField>();
	private List<InfoMethod> _methods = new ArrayList<InfoMethod>();
	private List<String> _interfaces = new ArrayList<String>();
	private InfoClass _superclass = null;

	public InfoClass(String name, String superclassName, ClassNameFormat format) {
		if (format == ClassNameFormat.ASM) {
			name = name.replace("/", ".");
			superclassName = superclassName.replace("/", ".");
			format = ClassNameFormat.Normal;
		}
		if (format == ClassNameFormat.Normal) {
			_bytecodeName = EntityMorph.classNameToBytecodeClassName(name);
			_superclassBytecodeName = EntityMorph.classNameToBytecodeClassName(superclassName);
		} else {
			_bytecodeName = name;
			_superclassBytecodeName = superclassName;
		}
	}
	
	public String getBytecodeName()		{ return _bytecodeName; }
	public String getName()			{ return EntityMorph.bytecodeClassNameToClassName(_bytecodeName); }
	public String getSuperclassBytecodeName(){return _superclassBytecodeName; }
	public String getSuperclassName()	{ return EntityMorph.bytecodeClassNameToClassName(_superclassBytecodeName); }
	public InfoClass getSuperclass()	{ return _superclass; }
	public void setSuperclass(InfoClass sc)	{ _superclass = sc; }

	public void addAnnotation(String desc) {
		_annotations.add(desc);
	}
	
	public List<String> getAnnotations() { return new ArrayList<String>(_annotations); }

	public void addInterface(String iface) {
		_interfaces.add(normalizeFormat(iface, ClassNameFormat.ASM));
	}
	
	public void addField(InfoField field) {
		_fields.add(field);
	}
	
	public List<InfoField> getFields() { return new ArrayList<InfoField>(_fields); }
	
	public List<InfoField> getAllFields() {
		List<InfoField> fields = getFields();
		if (_superclass != null) {
			// Adicionar fields da classe actual DEPOIS dos das superclasses,
			// para manter a ordem de declaração original,
			// que é utilizada para gerar alguns dos métodos
			fields = _superclass.getAllFields();
			fields.addAll(getFields());
		}
		return fields;
	}
	
	public boolean existsAllFields(String fieldName) {
		List<InfoField> fields = getAllFields();
		for (InfoField f : fields) {
			if (f.getName() == fieldName) return true;
		}
		return false;
	}
	
	public void addMethod(InfoMethod method) {
		_methods.add(method);
	}
	
	public List<InfoMethod> getMethods() { return new ArrayList<InfoMethod>(_methods); }
	
	public List<InfoMethod> getAllMethods() {
		List<InfoMethod> methods = getMethods();
		if (_superclass != null) {
			// Ordem Inversa do getAllFields, queremos da subclasse para a superclasse
			methods.addAll(_superclass.getAllMethods());
		}
		return methods;
	}
	
	public boolean existsMethod(String methodName) {
		for (InfoMethod m : _methods) {
			if (m.getName() == methodName) return true;
		}
		return false;
	}
	
	public boolean existsAllMethods(String methodName) {
		List<InfoMethod> methods = getAllMethods();
		for (InfoMethod m : methods) {
			if (m.getName() == methodName) return true;
		}
		return false;
	}
	
	public String toString() {
		return "InfoClass [name=" + getName() + ";bytecodeName=" + getBytecodeName() + ";annotations="	+ getAnnotations() + "]";
	}
	
	/** Verifica se existe a anotação com nome annotClass **/
	public boolean hasAnnotation(String annotClass, ClassNameFormat format) {
		return _annotations.contains(normalizeFormat(annotClass, format));
	}

	/** Verifica se esta classe implementa a interface com o nome iface **/
	public boolean hasInterface(String iface, ClassNameFormat format) {
		return _interfaces.contains(normalizeFormat(iface, format));
	}

	/** Converte qualquer formato para o formato Bytecode **/
	private String normalizeFormat(String s, ClassNameFormat format) {
		if (format == ClassNameFormat.ASM) {
			s = s.replace("/", ".");
			format = ClassNameFormat.Normal;
		}
		if (format == ClassNameFormat.Normal) {
			s = Morph.classNameToBytecodeClassName(s);
		}
		return s;
	}

}

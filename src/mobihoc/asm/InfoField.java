package mobihoc.asm;

import java.util.*;

public class InfoField {

	private int _access;
	private String _name;
	private String _desc;
	private String _signature;
	private Object _value;
	private List<String> _annotations = new ArrayList<String>();
	private InfoClass _infoClass;

	public InfoField(int access, String name, String desc, String signature, Object value, InfoClass infoClass) {
		_access = access;
		_name = name;
		_desc = desc;
		_signature = signature;
		_value = value;
		_infoClass = infoClass;
	}
	
	public int getAccess()		{ return _access; }
	public String getName()		{ return _name; }
	public String getDesc()		{ return _desc; }
	public String getSignature()	{ return _signature; }
	public Object getValue()	{ return _value; }
	public InfoClass getInfoClass()	{ return _infoClass; }
	
	public void addAnnotation(String desc) {
		_annotations.add(desc);
	}
	
	public List<String> getAnnotations() { return Collections.unmodifiableList(_annotations); }
	
	public String toString() {
		return "InfoField [name=" + getName() + ";desc=" + getDesc()
			/*+ ";signature=" + getSignature() + ";value=" + getValue()*/ + ";annotations="
			+ getAnnotations() + ";class=" + getInfoClass().getName() + "]";
	}

	public boolean hasAnnotation(String annotClass, InfoClass.ClassNameFormat format) {
		if (format == InfoClass.ClassNameFormat.ASM) {
			annotClass = annotClass.replace("/", ".");
			format = InfoClass.ClassNameFormat.Normal;
		}
		if (format == InfoClass.ClassNameFormat.Normal) {
			annotClass = EntityMorph.classNameToBytecodeClassName(annotClass);
		}
		return _annotations.contains(annotClass);
	}

}

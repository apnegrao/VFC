package mobihoc.javassist;

import javassist.*;
import java.util.*;
import java.lang.reflect.*;
import java.io.*;
import mobihoc.annotation.Data;
import mobihoc.annotation.DataField;
import mobihoc.annotation.PhiAnnotation;
import mobihoc.annotation.NiuComparator;
import mobihoc.asm.InstrumentationException;

/**
 * @author Stoyan Garbatov (nº 55437)
 * @author Ivo Anjo (nº 55460)
 * @author Hugo Rito (nº 55470)
 **/

public class EntityAnalyser {
	private static final byte ARGLENGTH = 2;
	public static String targetDir;

	// Nota: O classLoader.run throws Throwable, logo isto tem mesmo que estar assim
	public static void main(String[] args) throws Throwable {
		if (args.length != ARGLENGTH) {
			System.err.println("Usage: java EntityAnalyser \"<class1> <class2> ... <classn>\" <targetDir>" /*+ " -pass1/" + "-pass2" + "/-pass3\n\t-pass1: Generate DataUnits" + "\n\t-pass2: Instrument original classes to use DataUnits" + "\n\t-pass3: Instrument main application class"*/);
			System.exit(1);
		} else {
			targetDir = args[1] + "/";
			
			// Desligar output de debug
			Log.DEBUGMODE = false;

			Log.debug("EntityAnalyser::entering main (targetDir=" + targetDir /*+ ";mode=" + args[2]*/ + ")");
			//ClassPool pool = new ClassPool();
			ClassPool pool = ClassPool.getDefault();

			String[] classes = fileToClass(args[0]).split(" ");
			CtClass[] classesToAnalyse = pool.get(classes);
			
			Log.debug("There are " + classesToAnalyse.length + " classes to work on.");
			//if (args[2].equalsIgnoreCase("-pass1")) {
				for (CtClass cls : classesToAnalyse) {
					System.out.println("EntityAnalyser: Analysing Class " + cls.getName() + " (Pass1)");
					generateDataUnit(cls, pool);
				}
				Log.blank(1);
				return;
			//}
			/*if (args[2].equalsIgnoreCase("-pass2")) {
				for (CtClass cls : classesToAnalyse) {
					System.out.println("EntityAnalyser: Analysing Class " + cls.getName() + " (Pass2)");
					instrument(cls, pool);
				}
				Log.blank(1);
				return;
			}*/
			/*if (args[2].equalsIgnoreCase("-pass3")) {
				for (CtClass cls : classesToAnalyse) {
					System.out.println("EntityAnalyser: Analysing Class " + cls.getName() + " (Pass3)");
					instrumentMain(cls);
				}
				Log.blank(1);
				return;
			}*/
			//System.err.println("Invalid argument '" + args[2] + "', run without arguments to see usage.");
		}
	}
	
	private static String fileToClass(String filename) {
		return filename.replace("/", ".").replace(".class", "");
	}
	
	// Estas chamadas passaram a ser feitas com reflexão pela plataforma em si
	/*public static void instrumentMain(CtClass ctClass) {
		boolean toInject = false;
		String getDefinedPhiBody = "";
		String finishBody = "";
		
		try {
			Object[] annotations = ctClass.getAnnotations();
			
			
			for (Object annotation : annotations) {
				if (!(annotation instanceof PhiAnnotation)) continue;
				
				toInject = true;
				PhiAnnotation phi = (PhiAnnotation) annotation;
				
				int zones = phi.zones();
				
				getDefinedPhiBody = "public mobihoc.session.Phi getDefinedPhi(){";
				getDefinedPhiBody += "int[] zones = new int[" + zones + "];";
				getDefinedPhiBody += "mobihoc.session.KVec[] vectors = new mobihoc.session.KVec[" + zones + "];";
				
				for(int i = 0; i < zones; i++) {
					getDefinedPhiBody += "zones[" + i + "] = " + phi.zoneRange()[i] + ";";
					getDefinedPhiBody += "vectors[" + i + "] = new mobihoc.session.KVec(" + 
											phi.theta()[i] + ", " + 
											phi.sigma()[i] + ", " + 
											"(float)" + phi.niu()[i] + ");";
				}
				
				getDefinedPhiBody += "return new mobihoc.session.Phi(null, zones, vectors);}";
				
				CtMethod getDefinedPhi = CtNewMethod.make(getDefinedPhiBody, ctClass);
				ctClass.addMethod(getDefinedPhi);
				
				finishBody = "public void finish(){ " +
						"_client.sendPhi(getDefinedPhi());" +
						"_client.flushDelayedPublish(); }";
				CtMethod finish;
				try{
					finish = ctClass.getDeclaredMethod("finish");
					ctClass.removeMethod(finish);
				} catch (NotFoundException ex) {
					//ignore
				}
				finish = CtNewMethod.make(finishBody, ctClass);
				ctClass.addMethod(finish);
				
			}
		
		if (toInject) ctClass.writeFile(targetDir);
		
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CannotCompileException e) {
			throw new RuntimeException(e);
		}
		
	}*/
	
	// Esta fase do processamento foi completamente passada para o mobihoc.asm.EntityMorph
	/*
	public static void instrument(CtClass ctClass, ClassPool pool) {
		String baseName = ctClass.getName();
		String simpleBaseName = ctClass.getSimpleName();
		Vector<String> fieldTypes = new Vector<String>();
		Vector<String> fieldNames = new Vector<String>();
		Log.debug("EntityAnalyser::instrument - " + baseName);
		
		try {
			Object[] annotations;
			String duClassName = baseName + "_DataUnit";
			String duFieldName = "injected_dataUnit";
			String duMobihocName = "mobihoc.session.DataUnit";
			//ctClass.setName(baseName + "_Copy");
			
			CtField dataUnitField = CtField.make("private " + duClassName + " " + duFieldName + ";", ctClass);
			//System.out.println("____info:" + dataUnitField.getName() + " sig: "  + dataUnitField.getSignature());
			ctClass.addField(dataUnitField);
			
			String def = "";
			String getterBody = "";
			String setterName = "";
			String setterBody = "";
			
			String signature = "";
			String invocation = "(";
			
			CtClass currentClass = ctClass;
			
			while (currentClass.getName().compareToIgnoreCase("java.lang.Object") != 0) {
				CtField[] fields = currentClass.getDeclaredFields();
				for (CtField field : fields) {
					annotations = field.getAnnotations();
					for (Object annotation : annotations) {
						if (!(annotation instanceof DataField)) continue;
						fieldNames.add(capitalize(field.getName().replace("_", "")));
						fieldTypes.add(field.getType().getName());
						String getterName = "get" + capitalize(field.getName().replace("_", ""));
						
						getterBody = "public " + field.getType().getName() + " "+ getterName + "() { "+
								"return " + duFieldName + "." + getterName + "(); }";
						
						CtMethod getter;
						try{
							getter = ctClass.getDeclaredMethod(getterName);
							ctClass.removeMethod(getter);
						} catch (NotFoundException e) {
							//do nothing, since the class did not have its own definition of the method
						}
						getter = CtMethod.make(getterBody, ctClass);
						ctClass.addMethod(getter);
						
						signature += field.getType().getName() + " " + field.getName() + ", ";
						invocation += " " + field.getName() + ", ";
					}
				}
				currentClass = currentClass.getSuperclass();
			}
			signature = signature.substring(0, signature.length() - 2);
			invocation = invocation.substring(0, invocation.length() - 2) + ")";
			
			//protected EntityDataUnit createDataUnit(int posX, int posY, String label) {
			def = "protected " + duClassName +" injected_createDataUnit(" + signature + ") {" + "\n" +
				"return new " + duClassName + invocation + ";" + "}";
			Log.code(def);
			
			CtMethod createDataUnit;
			try{
				createDataUnit = ctClass.getDeclaredMethod("createDataUnit");
				ctClass.removeMethod(createDataUnit);
			} catch (NotFoundException e) {
				//do nothing, since the class did not have its own definition of the method
			}
			createDataUnit = CtMethod.make(def, ctClass);
			ctClass.addMethod(createDataUnit);
			
			
			//public GameEntity(QObject parent, EntityDataUnit du, MobihocClient client) {
			def = "public " + simpleBaseName + "(" + 
				duClassName + " " + duFieldName + ", mobihoc.api.MobihocClient client) {" +
				"this();" +
				"this." + duFieldName + " = " + duFieldName + ";" +
				"injected_client = client;" + 
				"initializeInstance();" + "}";
			CtConstructor constructor = CtNewConstructor.make(def, ctClass);
			ctClass.addConstructor(constructor);
			
			//public GameEntity(QObject parent, int posX, int posY, short r, short g, short g, String label, MobihocClient client) {
			def = "public " + simpleBaseName + "(" + 
				signature + ", mobihoc.api.MobihocClient client) {" +
				"this();" +
				"this." + duFieldName + " = injected_createDataUnit" + invocation + ";" +
				"this." + duFieldName + ".setId(client.getNextId());" +
				"injected_client = client;" + 
				"client.addDelayedPublish(" + duFieldName + ");" +
				"initializeInstance();" + "}";
			CtConstructor constructor = CtNewConstructor.make(def, ctClass);
			ctClass.addConstructor(constructor);
			
			def = "public " + duClassName + " getDataUnit(){ return " + duFieldName + ";}";
			CtMethod getDataUnit;
			try {
				getDataUnit = ctClass.getDeclaredMethod("getDataUnit");
				ctClass.removeMethod(getDataUnit);
			} catch (NotFoundException e) {
				//do nothing, since the class did not have its own definition of the method
			}
			getDataUnit = CtMethod.make(def, ctClass);
			ctClass.addMethod(getDataUnit);
			
			CtMethod getId;
			try {
				getId = ctClass.getDeclaredMethod("getId");
				ctClass.removeMethod(getId);
			} catch (NotFoundException e) {
				//do nothing, since the class did not have its own definition of the method
			}
			def = "public int getId(){ return " + duFieldName + ".getId();}";
			getId = CtMethod.make(def, ctClass);
			ctClass.addMethod(getId);
			
			CtMethod entityUpdated;
			try {
				entityUpdated = ctClass.getDeclaredMethod("entityUpdated");
				ctClass.removeMethod(entityUpdated);
			} catch (NotFoundException e) {
				//do nothing, since the class did not have its own definition of the method
			}
			def = "private void entityUpdated(" + duMobihocName + " du) {" + 
				"du.setId(injected_dataUnit.getId());" +
				"if (injected_client.write(du) == false) {" +
					"System.out.println(\"Write Failed (@" + baseName + "\");" +
					"return;" +
				"}" +
				"injected_dataUnit.merge(du);" +
				"dataUnitChanged();" +
				"}";
			entityUpdated = CtMethod.make(def, ctClass);
			ctClass.addMethod(entityUpdated);
			
			
			//public void setColorB(short newColorB) {
			////entityUpdated(createDataUnit(getPosX(), getPosY(), getLabel(), getColorR(), getColorG(), newColorB));
			int i = 0;
			for (String fld : fieldNames) {
				setterName = "set" + fld;
				setterBody = "entityUpdated(createDataUnit(";
				for (String fldIn : fieldNames) {
					if (fldIn.compareToIgnoreCase(fld) == 0) setterBody += "new" + fldIn + ", ";
					else setterBody += "get" + fldIn + "(), ";
				}
				setterBody = "public void " + setterName + "(" + fieldTypes.get(i) + " new" + fld + "){" + setterBody.substring(0, setterBody.length() - 2) + "));}";
				CtMethod setter;
				try{
					setter = ctClass.getDeclaredMethod(setterName);
					ctClass.removeMethod(setter);
				} catch (NotFoundException e) {
					//do nothing, since the class did not have its own definition of the method
				}
				setter = CtMethod.make(setterBody, ctClass);
				ctClass.addMethod(setter);
				i++;
			}
			
			ctClass.writeFile(targetDir);
			
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (NotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CannotCompileException e) {
			throw new RuntimeException(e);
		}
	}*/
	
	
	public static void generateDataUnit(CtClass ctClass, ClassPool pool) throws InstrumentationException {
		String baseName = ctClass.getName();
		String simpleBaseName = ctClass.getSimpleName();
		Log.debug("EntityAnalyser::generateDataUnit - " + baseName);
		try {
			Object[] annotations = ctClass.getAnnotations();
			CtClass dataUnit = pool.get("mobihoc.session.DataUnit");
			CtClass positionableDataUnit = pool.get("mobihoc.session.PositionableDataUnit");
			String newClassName = baseName + "_DataUnit";
			String newClassNameSimple = simpleBaseName + "_DataUnit";
			Log.debug("going to try to generate a new dataUnit class called " + newClassName);
			//makeClass(java.lang.String classname, CtClass superclass)
			CtClass newDataUnit = null;
			String def = "";
			String toStringBody = "";
			String mergeBody = "";
			String getterBody = "";
			String setterBody = "";
			String constructorSignature = "";
			String constructorBody = "";
			String niuBody = "";
			String niuP1 = "";
			String niuP2 = "";
			
			
			for (Object annotation : annotations) {
				if (!(annotation instanceof Data)) continue;
				
				Data data = (Data) annotation;
				
				
				if (data.positionable()) {
					newDataUnit = ClassPool.getDefault().makeClass(newClassName, positionableDataUnit);
				} else {
					newDataUnit = ClassPool.getDefault().makeClass(newClassName, dataUnit);
				}
				
				
				//public abstract boolean isPivot();
				
				if (data.pivot()) def = "public boolean isPivot(){ return true;}";
				else def = "public boolean isPivot(){ return false;}";
				
				CtMethod isPivot = CtNewMethod.make(def, newDataUnit);
				newDataUnit.addMethod(isPivot);
				
			}
			
			//public abstract float compareNiu (DataUnit du);
			niuBody = "public float compareNiu(mobihoc.session.DataUnit du) {" +
				newClassName + " newDu = (" + newClassName + ") du;" + 
				"if (newDu == null) return (float)0;";
			
			
			
			constructorSignature = "public " + newClassNameSimple + "(";
			constructorBody = "{ super();";
			
			mergeBody = "public void merge(mobihoc.session.DataUnit du) { " +
					"synchronized(this) {" +
					// Descomentar próxima linha para fazer output de debug no merge
					//"System.out.println(\"" + newClassName + "::Merge (current = \" + toString() + \"; new = \" + du.toString() + \")\");" +
					newClassName + " update = (" + newClassName + ") du;";
			
			
			toStringBody = "public String toString() {return \"ToString::" + newClassName + ": \"";
			
			CtClass currentClass = ctClass;
			
			while (currentClass.getName().compareToIgnoreCase("java.lang.Object") != 0) {
				CtField[] fields = currentClass.getDeclaredFields();
				for (CtField field : fields) {
					annotations = field.getAnnotations();
					for (Object annotation : annotations) {
						if (!(annotation instanceof DataField)) continue;
						
						int mod = field.getModifiers();//get the modifiers of the field
						CtField newField = CtField.make("private " + field.getType().getName() + " " + field.getName() + ";", newDataUnit);
						toStringBody += "+\"" + field.getName() + " = \" + " + field.getName() + " + \"; \"";
						//mergeBody += "this." + field.getName() + " = " + "update." + field.getName() + ";";
						mergeBody += "this." + field.getName() + " = " + "update.get" + capitalize(field.getName().replace("_", "")) + "();";
						constructorSignature += field.getType().getName() + " " + field.getName() + ", ";
						constructorBody += "this." + field.getName() + " = " + field.getName() + "; ";
						//newField.setModifiers(mod);//set the correct modifiers
						newDataUnit.addField(newField);//insert the field into the newClass
						
						//getter method
						getterBody = "public " + field.getType().getName() + " get" + capitalize(field.getName().replace("_", "")) + "() { " +
								"return " + field.getName() + "; }";
						CtMethod getter = CtNewMethod.make(getterBody, newDataUnit);
						newDataUnit.addMethod(getter);
						
						//compareNiu
						niuP1 += "this." + field.getName() + ", ";
						niuP2 += "newDu.get" + capitalize(field.getName().replace("_", "")) + "()" + ", ";
					}
				}
				currentClass = currentClass.getSuperclass();
			}
			
			constructorSignature = constructorSignature.substring(0, constructorSignature.length() - 2) + ")";
			constructorBody += "}";

			CtConstructor constructor = CtNewConstructor.make(constructorSignature + constructorBody, newDataUnit);
			newDataUnit.addConstructor(constructor);
			
			// Procurar método anotado com @NiuComparator
			CtMethod niuComparator = null;
			CtMethod[] methods = ctClass.getDeclaredMethods();
			for (CtMethod method : methods) {
				for (Object annotation : method.getAnnotations()) {
					if (annotation instanceof NiuComparator) {
						niuComparator = method;
						break;
					}
				}
				if (niuComparator != null) break;
			}

			if (niuComparator == null) {
				throw new InstrumentationException("Error: Could not find a method on class '" + ctClass.getName() + "' annotated with @NiuComparator.");
			}

			CtMethod duComparator = CtNewMethod.copy(niuComparator, newDataUnit, null);
			duComparator.setName("niuComparator");
			newDataUnit.addMethod(duComparator);
			
			//compareNiu
			niuBody += "return niuComparator(" + niuP1 + niuP2.substring(0, niuP2.length() - 2) + ");}";
			CtMethod compareNiu = CtNewMethod.make(niuBody, newDataUnit);
			newDataUnit.addMethod(compareNiu);
			
			//toString method
			toStringBody += ";}";
			CtMethod toString = CtNewMethod.make(toStringBody, newDataUnit);
			newDataUnit.addMethod(toString);
			
			//merge method
			mergeBody += "}" + "return; }";
			CtMethod merge = CtNewMethod.make(mergeBody, newDataUnit); 
			newDataUnit.addMethod(merge); 
			
			
			//public abstract String print();
			def = "public String print() {return toString();}";
			CtMethod print = CtNewMethod.make(def, newDataUnit);
			newDataUnit.addMethod(print);
			
			newDataUnit.writeFile(targetDir);
			
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (NotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CannotCompileException e) {
			throw new RuntimeException(e);
		}
		
		return;
	}
	
	public static String capitalize(String s) {
		return s.substring(0,1).toUpperCase() + s.substring(1,s.length());
	}

}

package mobihoc.asm;

import java.util.*;
import java.lang.reflect.*;
import mobihoc.api.MobihocClient;
import mobihoc.session.DataUnit;

public class DuConverter {

	/** Método que usa reflection para criar os objectos correspondentes aos dus recebidos **/
	public static List<Object> dataUnitToClientObj(List<DataUnit> dus, MobihocClient client) {
		List<Object> lst = new ArrayList<Object>();
		//System.out.println("DataUnitToClientObj!");
		for (DataUnit du : dus) {
			String duClassName = du.getClass().getName();
			if (!duClassName.endsWith("_DataUnit")) {
				System.err.println("Unknown DataUnit in dataUnitToClientObj");
				continue;
			}
			String className = duClassName.substring(0, duClassName.lastIndexOf("_DataUnit"));
			//System.out.println(" Tentar invocar constructor de um " + className);

			try {
				Class c = Class.forName(className);
				Constructor ctor = c.getConstructor(du.getClass(), MobihocClient.class);
				Object o = ctor.newInstance(du, client);
				lst.add(o);
			} catch (ClassNotFoundException e) {
				System.err.println("Error in dataUnitToClientObj");
			} catch (NoSuchMethodException e) {
				System.err.println("Error in dataUnitToClientObj");
			} catch (InstantiationException e) {
				System.err.println("Error in dataUnitToClientObj");
			} catch (IllegalAccessException e) {
				System.err.println("Error in dataUnitToClientObj");
			} catch (InvocationTargetException e) {
				System.err.println("Error in dataUnitToClientObj");
			}
		}
		return lst;
	}

}

package mobihoc.annotation;

import java.lang.annotation.*;

/**
 * @author Stoyan Garbatov (nº 55437)
 * @author Ivo Anjo (nº 55460)
 * @author Hugo Rito (nº 55470)
 **/
 
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Data {
	boolean pivot();
	boolean positionable();
}

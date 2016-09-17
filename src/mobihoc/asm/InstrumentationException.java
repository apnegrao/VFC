package mobihoc.asm;

public class InstrumentationException extends mobihoc.exception.MobihocException {

	private static final long serialVersionUID = 1L;
	
	public InstrumentationException() {
		this("InstrumentationException");
	}
	
	public InstrumentationException(String s) {
		super(s);
	}

}

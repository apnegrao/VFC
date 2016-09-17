package mobihoc.exception;

public class MobihocException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public MobihocException() {
		this("MobihocException");
	}
	
	public MobihocException(String s) {
		super(s);
	}

}

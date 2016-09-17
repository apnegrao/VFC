package mobihoc.exception;

public class InfoItemResultException extends MobihocException {

	private static final long serialVersionUID = 1L;
	
	public InfoItemResultException() {
		this("InfoItemResultException");
	}
	
	public InfoItemResultException(String s) {
		super(s);
	}

}

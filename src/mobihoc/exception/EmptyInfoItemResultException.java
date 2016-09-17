package mobihoc.exception;

public class EmptyInfoItemResultException extends InfoItemResultException {

	private static final long serialVersionUID = 1L;
	
	public EmptyInfoItemResultException() {
		this("EmptyInfoItemResultException");
	}
	
	public EmptyInfoItemResultException(String s) {
		super(s);
	}

}

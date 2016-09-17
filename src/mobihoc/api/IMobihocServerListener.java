package mobihoc.api;

public interface IMobihocServerListener {

	public void callbackMessageIn(String desc);

	public void callbackClientPublished(int address);

	public void callbackClientUnpublished(int address);
}

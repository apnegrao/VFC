package mobihoc.session.server;

public interface IRoundListener {
	public void callbackRoundTriggered(RoundTrigger trigger, long period, long tickno);
}

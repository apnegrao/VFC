/**
 * 
 */
package mobihoc.session.server;

/**
 * @author Administrator
 *
 */
public class RoundTrigger extends Thread {

	private long _period;
	private boolean _on;
	private long _count;
	private IRoundListener _listener;

	public RoundTrigger(IRoundListener listener) {
		_on = false;
		_listener = listener;
	}

	public void run() {
		_on = true;
		_count = 1;
		while(_on) {
			try {
				Thread.sleep(_period);
				_listener.callbackRoundTriggered(this, _period, _count);
			} catch(InterruptedException e) {
			}
			_count++;
		}
	}

	public void disable() {
		_on = false;
	}
	
	public void enable(long period) {
		_period = period;
		this.start();
	}
}
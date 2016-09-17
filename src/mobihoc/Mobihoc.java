package mobihoc;

import mobihoc.api.IMobihocListener;

public class Mobihoc {

	private static IMobihocListener _listener;
	
	public static void log(String msg) {
		if (_listener != null) {
			_listener.log(msg);
		}
		System.out.println("MobihocLog::" + msg);
	}

	public static void setListener(IMobihocListener listener) {
		_listener = listener;
	}
}

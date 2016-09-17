package mobihoc.api;

import mobihoc.session.DataUnit;

public interface IMobihocClientListener {

	public void callbackSubscribeResult(boolean status, String desc);
	
	public void callbackPublishResult(boolean status, DataUnit[] dus, int[] ids);

	public void callbackUpdateState(DataUnit[] dus);
	
	public void callbackNewData(DataUnit[] dus);
	
	public void callbackEnable(boolean status);

	public void callbackDisable();

	public void callbackLoadState(int[] ids, DataUnit[] dus);

	public void callbackError(String error);
	
	public void callbackConnClosed();
}

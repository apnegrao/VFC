package mobihoc.api;

import java.util.List;

import mobihoc.session.DataUnit;
import mobihoc.api.MobihocClient;

public interface IMobihocApp {

	//public List<DataUnit> getState();
	public void callbackStateUpdated();
	public void callbackNewData(List<DataUnit> dus);
	public void callbackLoadState(List<DataUnit> myDus, List<DataUnit> dus);
	public void callbackError(String error);
	public void callbackConnClosed(String error);

	public MobihocClient getMobihocClient();

}

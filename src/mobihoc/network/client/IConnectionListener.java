package mobihoc.network.client;

public interface IConnectionListener {
	
	public void callbackMsgReceived(Connection net, Object data);

	public void callbackConnectionStatus(Connection.Status status);
}
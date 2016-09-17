package mobihoc.network.server;
/**
 * 
 */

/**
 * @author Administrator
 *
 */
public interface INetworkListener {

	public void callbackConnOpened(INetworkServices net, int client);
	
	public void callbackMsgReceived(INetworkServices net, int address, Object data);
	
	public void callbackConnClosed(INetworkServices net, int client);
}

package mobihoc.network.server;

public interface INetworkServices {

	public boolean send(int address, Object data);

	public boolean broadcast(Object data);

	public boolean broadcastExcept(int address, Object data);
}

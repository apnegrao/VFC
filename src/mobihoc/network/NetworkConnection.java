package mobihoc.network;

import java.io.*;
import mobihoc.network.client.ServerRecord;
import mobihoc.network.server.HostRecord;

/** Classe NetworkConnection.
 * Superclasse de classes que representam ligações na rede
 **/
public abstract class NetworkConnection {
	
	/** Efectua ligação ao servidor recebido **/
	abstract public NetworkStream connect(ServerRecord server) throws IOException;
	
	/** Faz set-up de alguma configuração que o servidor necessite **/
	abstract public void startServer(HostRecord config) throws IOException;
	/** Aceita ligações vindas dos clientes **/
	abstract public NetworkStream accept() throws IOException;

}

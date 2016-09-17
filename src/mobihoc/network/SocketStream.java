package mobihoc.network;

import java.io.*;
import java.net.*;

/** Classe SocketStream
 * Esta classe serve de adaptador entre um Socket e a interface NetworkStream
 **/
public class SocketStream implements NetworkStream {

	private Socket _socket;
	
	public SocketStream(Socket socket) {
		_socket = socket;
	}
	
	public InputStream getInputStream() throws IOException {
		return _socket.getInputStream();
	}
	
	public OutputStream getOutputStream() throws IOException {
		return _socket.getOutputStream();
	}
	
	public String getFriendlyName() throws IOException {
		return "user@" + _socket.getInetAddress().getHostAddress();
	}
	
	public void close() throws IOException {
		_socket.close();
	}

}

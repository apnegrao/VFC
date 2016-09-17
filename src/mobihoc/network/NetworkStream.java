package mobihoc.network;

import java.io.*;

/** Interface NetworkStream.
 * Interface de um objecto que contem Input e Ouput streams, assim como alguma informação sobre o cliente.
 * (O objectivo é isolar o mobihoc do tipo de ligação, seja socket, seja streamconnection do midp.)
 **/
public interface NetworkStream {
	
	public InputStream getInputStream() throws IOException;
	public OutputStream getOutputStream() throws IOException;
	public void close() throws IOException;

	public String getFriendlyName() throws IOException;

}

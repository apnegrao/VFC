/**
 * 
 */
package mobihoc.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Administrator
 *
 */
public class NetworkIO {

	/**
	 * Read a message in the form "<length> msg".
	 * The length allows us to know exactly how many bytes to read
	 * to get the complete message. Only the message part (msg) is
	 * returned, or null if there's been a problem.
	 * 
	 * @param in
	 * @return
	 */
	  public static byte[] readBytes(InputStream in) {
		  byte[] data = null;   
	    try {       	      	
	      int len = in.read();    // get the message length
	      System.out.println("[READ] Message to read size "+len);
	      if (len <= 0) {
	        System.out.println("[READ] Message Length Error");
	        return null;
	      }
	   
	      data = new byte[len];
	      len = 0;
	      // read the message, perhaps requiring several read() calls 
	      while (len != data.length) {     
	        int ch = in.read(data, len, data.length - len);
	        if (ch == -1) {
	          System.out.println("[READ] Message Read Error");
	          return null;
	        }
	        len += ch;
	      }      
	    } 
	    catch (IOException e) 
	    {  System.out.println("[READ] readData(): " + e); 
	       return null;
	    }
	    return data;
	  }
	  
	  /**
	   * the message format is "<length> msg" in byte form
	   */
	  public static boolean writeBytes(OutputStream out, byte[] data)
	  {
	    System.out.println("[WRITE] Message to write ("+((data!=null)?data.length:-1)+" bytes): "+data);
	    try {
	      out.write(data.length);
	      out.write(data);
	      return true;
	    }
	    catch (Exception e) 
	    {  System.out.println("sendMessage(): " + e);  
	       return false;
	    }
	  }

//	  public static boolean writeObject(OutputStream out, Serializable obj){
//			try {
//				ObjectWriter serializer = new ObjectWriter(out);
//				serializer.writeObject(obj);
//			} catch (IOException e) {
//				System.err.println("SendObject: " + e);
//				e.printStackTrace();
//				return false;
//			}
//	  }
	  
	  
}
package mobihoc.session.messages;

import mobihoc.session.DataUnit;
import mobihoc.session.Message;

/* [Serializable] */
public class PublishAllMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	public byte[] data;
	
	public Object[] dus;
	
	public PublishAllMessage(){
		type = MType.PUBLISH_ALL;
	}
	
	//we assume the key is a DataUnit Array
	public PublishAllMessage(byte[] key){
		type = MType.PUBLISH_ALL;
		data = key;
	}
	
	public PublishAllMessage(DataUnit[] dataUnits){
		type = MType.PUBLISH_ALL;
		dus = dataUnits;
	}
	
	public DataUnit[] getDataUnits() {
		if (dus == null) {
			return null;
		} else {
			DataUnit[] d = new DataUnit[dus.length];
			for(int i = 0; i < dus.length; i++) {
				d[i] = (DataUnit)dus[i];
			}
			return d;
		}
	}
}

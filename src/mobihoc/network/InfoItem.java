package mobihoc.network;

import java.util.*;
import mobihoc.exception.EmptyInfoItemResultException;

/** Classe InfoItem.
 * Representa um item de informação que o MobiHoc pede da aplicação, para configurar a sua ligação de rede.
 **/
public class InfoItem {

	private String _infoType;
	private String _userText;
	private String _result = null;

	public InfoItem(String infoType, String userText) {
		_infoType = infoType;
		_userText = userText;
	}
	
	public String getInfoType() {
		return _infoType;
	}

	public String getUserText() {
		return _userText;
	}
	
	public void setResult(String result) {
		_result = result;
	}
	
	public String getResult() throws EmptyInfoItemResultException {
		if (_result == null) throw new EmptyInfoItemResultException();
		return _result;
	}
	
}

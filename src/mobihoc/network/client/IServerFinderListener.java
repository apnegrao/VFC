package mobihoc.network.client;

import mobihoc.network.InfoItem;
import java.util.*;

public interface IServerFinderListener {

	public void callbackSetStatus(String msg);
	
	public void callbackSearchError(String msg);
	
	public void callbackSearchResults(List<ServerRecord> servers);
	
	public void callbackNeedInfo(List<InfoItem> infoItems);

}

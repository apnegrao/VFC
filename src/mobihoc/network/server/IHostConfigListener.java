package mobihoc.network.server;

import mobihoc.network.InfoItem;
import java.util.*;

public interface IHostConfigListener {

	public void callbackConfigResults(HostRecord record);

	public void callbackNeedInfo(List<InfoItem> infoItems);

}

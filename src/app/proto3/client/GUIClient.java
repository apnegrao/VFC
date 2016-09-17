package app.proto3.client;

// EchoClientMIDlet.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, August 2005

/* This screen starts the device and services search, and reports 
   its ongoing status. At the end of the search, a services 
   list screen (ServicesList) is displayed.

   A service is chosen from the list, and a form allows the user to 
   send messages and receive echoed responses from the server.
   The echoed messages are changed to uppercase.
*/

// import javax.microedition.lcdui.*;

import app.*;

import java.util.*;

import mobihoc.Mobihoc;
import mobihoc.api.IMobihocListener;
import mobihoc.network.*;
import mobihoc.network.client.*;
import mobihoc.exception.*;

public class GUIClient implements IServerFinderListener,
	IMobihocListener {

  // GUI elements
//   private Form form;
//   private Gauge searchGauge;
  private int gaugeIndex;
//   private StringItem statusSI;
//   private Command exitCmd;
//   private Command stopSearchCmd;

//   private Display display;

  private MainMIDlet _parent;

  private static GUIClient _instance;
  
// 	public MessageUI messageui;

 NetworkFinder nf;

  public GUIClient(MainMIDlet parent) 
  {
	  _parent = parent;
	  

		_instance = this;
	  
    // build GUI
//    form = new Form("Searching");
//
//    exitCmd = new Command("Exit", Command.EXIT, 1);
//    stopSearchCmd = new Command("StopSearch", Command.SCREEN, 1);
//    form.addCommand(exitCmd);
//    form.addCommand(stopSearchCmd);
// 
//    statusSI = new StringItem("Status: ", "Starting...");
//    searchGauge = new Gauge("Bluetooth Service Search", false,
//                         Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
//    form.append(statusSI);
//    gaugeIndex = form.append(searchGauge); 
//
//    form.setCommandListener(this);
//
//    startApp();
    
//     display = Display.getDisplay(_parent);
  
    Mobihoc.setListener(this);
    
//     messageui = new MessageUI(this);
//     exitCmd = new Command("Exit", Command.EXIT, 1);
//     messageui.addCommand(exitCmd);    
//    messageui.addCommand(stopSearchCmd);
//     display.setCurrent(messageui);
    log("Esta em cima");
    UserInterface ui = new ConsoleUserInterface();
    OutputComposer oc = new OutputComposer(); 
    
    // Escolher método de ligação
    List<ConnectionInfo> conns = ConnectionInfoFactory.getAvailableConnectionInfos();
    int i = 0;
    oc.addLine("Please choose method of connection:");
    for (ConnectionInfo ci : conns) {
	   oc.addLine(i + " " + ci.toString());
	   i++;
    }
    String req = oc.flush();

    while (true) {
	try {
	    int res = ui.getIntFromUser(req);
	    nf = conns.get(res).getServerFinder(this);
	    break;
	} catch (IndexOutOfBoundsException e) { }
    }

	nf.run();

  
  }


//  protected void startApp() 
//  {
//	  display = Display.getDisplay(_parent);
//	  display.setCurrent(form);
//  }

//   protected void pauseApp() {}


//   public void commandAction(Command c, Displayable d) 
//   { 
// //	  if (c == stopSearchCmd) {
// //		  log("Stopping...");
// ////		  serviceFinder.stopSearchDevices();
// //		  
// //	  }
// 	  if (c == exitCmd)
// 	  _parent.notifyDestroyed();
//   }

//   public void destroyApp(boolean inconditional) {
// 	  _parent.notifyDestroyed();
//   }

  // ----------------- reporting from ServiceFinder --------------

  public void searchError(String msg) {
	  // show error message and remove gauge
//	  statusSI.setText(msg);
//	    searchGauge.setValue(Gauge.CONTINUOUS_IDLE);
//	    form.delete(gaugeIndex);   
  }

  
  public void callbackSearchError(String msg) {
//	  searchError(msg);
  }

	public void callbackSearchResults(List<ServerRecord> servers) {
		if (servers.size() == 1) {
			GUIClientForm cf = new GUIClientForm(servers.get(0), this);
		} else {
			log("FIXME: Multiple matches / No match on GUIClient::callbackSearchResults");
		}
	}
	
	public void callbackNeedInfo(List<InfoItem> infoItems) {
		UserInterface ui = new ConsoleUserInterface();
		for (InfoItem i : infoItems) {
			i.setResult(ui.getStringFromUser("Mobihoc requested info (type=" + i.getInfoType() + "), " + i.getUserText() + ":\n"));
		}
		try {
			nf.requestedInfoFilled();
		} catch (InfoItemResultException e) {
			log("InfoItemResultException");
		}
	}

	public void callbackSetStatus(String msg) {
//		statusSI.setText(msg);
	} 
	
	  public void log(String s)
	  {
	    System.err.println("app3 log:: " + s);
// 	    _instance.messageui.addMsg(s);
// 	    _instance.messageui.repaint();
	  }
}

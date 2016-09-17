package app;

// import javax.microedition.lcdui.Command;
// import javax.microedition.lcdui.CommandListener;
// import javax.microedition.lcdui.Display;
// import javax.microedition.lcdui.Displayable;
// import javax.microedition.lcdui.List;
// import javax.microedition.midlet.MIDlet;

import app.proto3.client.GUIClient;
import app.proto3.server.GUIServer;

/**
 * Contains the Bluetooth API demo, that allows to download
 * the specific images from the other devices.
 *
 * @version ,
 */
public final class MainMIDlet {

    public static void main(String[] args) {
	new MainMIDlet().doStuff();
    }

    public void doStuff() {
	UserInterface ui = new ConsoleUserInterface();
	int choice = ui.getIntFromUser("Input 0: Servidor ; 1: Cliente:\n");
        switch (choice) {
	    case 0:
		imageServer = new GUIServer(this);
                break;
	    case 1:
		imageClient = new GUIClient(this);
		break;
	    default:
		System.err.println("Unexpected choice...");
		break;
        }
    }


    /** The messages are shown in this demo this amount of time. */
    static final int ALERT_TIMEOUT = 2000;

    /** A GUI part of server that publishes images. */
    private GUIServer imageServer;

    /** A GUI part of client that receives image from client */
    private GUIClient imageClient;

    /** value is true after creating the server/client */
    private boolean isInit = false;
}

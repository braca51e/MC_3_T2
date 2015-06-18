package vlr;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;



public class VLRServer extends Thread  {

    private ServerSocket s;
    private volatile Boolean running = true;
    private ObjectInputStream objectInput;
    private ObjectOutputStream objectOutput;
    private VLR parent;

    VLRServer(VLR parent) {
	this.parent = parent;
    }

    @Override
    public void run() {
	try {

	    // Wait on connection from base client
	    s = new ServerSocket(parent.vlrport);
	    Socket clientConnection = s.accept();

	    objectOutput = new ObjectOutputStream(clientConnection.getOutputStream());
	    objectOutput.flush();
	    objectInput = new ObjectInputStream(clientConnection.getInputStream());

	    while (running) {
		//TODO: Handle Messages from base client thread
	    }
	} catch (IOException ioe) {

	}
    }

 

}

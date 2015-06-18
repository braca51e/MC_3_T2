package hlr;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


public class VLRHandlerThread extends Thread  {
    private Socket socket;
    HLRServer parent;

    ObjectInputStream objectInput = null;
    ObjectOutputStream objectOutput = null;

    ArrayList<Rectangle2D> serviceArea = null;

    volatile Boolean running = true;

    public VLRHandlerThread(Socket socket, HLRServer hlr) {
	try {
	    this.socket = socket;
	    this.parent = hlr;

	    objectInput = new ObjectInputStream(this.socket.getInputStream());
	    objectOutput = new ObjectOutputStream(this.socket.getOutputStream());
	} catch (IOException e) {

	}
    }


    @Override
    public void run() {
	    while (running) {
		//TODO: handle messages from VLR
	    }
    }



}

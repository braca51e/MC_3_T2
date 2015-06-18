package baseclient;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import common.InitializationMsg;
import common.MobileClientBinding;

public class BaseClientThread extends Thread  {


    //ip and port of VLR to whom this client connects
    String serverIP = null;
    int port = -1;
    
    //socket and stream for connection with VLR
    public Socket socket = null;
    ObjectInputStream objectInput;
    ObjectOutputStream objectOutput;
    
    //set of LAs for whom the connected VLR is responsible
    ArrayList<Rectangle2D> vlrArea = null;   
    
    //unique id of this thread
    int id = -1;
    
    //parent BaseClient
    BaseClient parent = null;

    public BaseClientThread(int id, String serverIP, int port, ArrayList<Rectangle2D> vlrArea,
	    BaseClient parent) {
	this.serverIP = serverIP;
	this.vlrArea = vlrArea;
	this.parent = parent;
	this.port = port;
    }

    @Override
    public boolean equals(Object object) {
	boolean same = false;

	if (object != null && object instanceof BaseClientThread) {
	    same = (this.id == ((BaseClientThread) object).id);
	}

	return same;
    }


    @Override
    public void run() {
	// connect to HLR (server)
	try {

	    System.out.println("Base Client Thread running");

	    // Client connection to VLR
	    socket = new Socket(serverIP, port);
	    objectOutput = new ObjectOutputStream(socket.getOutputStream());
	    objectOutput.flush();
	    objectInput = new ObjectInputStream(socket.getInputStream());

	    // Initialize VLR with location areas for which the VLR is responsible
	    InitializationMsg im = new InitializationMsg();
	    im.locationAreas = vlrArea;
	    objectOutput.writeObject(im);
	    
	    while (!interrupted()) {
		//TODO: handle messages from VLR
	    }

	} catch (IOException e) {	    
	}
    }

    public void setServiceAreas(ArrayList<Rectangle2D> las) {
	this.vlrArea = las;
    }

    public void stopBaseClientThread() {
	//TODO - close connection to VLR ...
    }

    public void sendLocationUpdate(String id2, Rectangle2D currentLA, Rectangle2D previousLA) {
	// TODO - update location of vehicle with id2, from previousLA to currentLA
	// NOTE: previousLA is null for very first location update	
    }

    public void sendRemoveMessage(String id2) {
	// TODO - Inform VLR that vehicle with id2 has connected to another VLR and thus needs to be removed
	
    }

    public void sendSearch(String id2, String id3) {
	// TODO - initiate a search message	
    }
}

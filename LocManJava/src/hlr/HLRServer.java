package hlr;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import common.Constants;

public class HLRServer extends Thread {

    // constant for properties file
    private static final String propfile = "hlr.properties";

    // HLR port
    int port = -1;

    //Accepts connections from VLRs
    private ServerSocket s;
    
    private volatile Boolean running;
    
    public HLRServer() {
	try {
	    System.out.println("Starting of HLR Server");

	    running = true;
	    s = null;

	    //Load properties
	    Properties props = System.getProperties();
	    props.load(new BufferedInputStream(new FileInputStream(propfile)));
	    
	    //listen port of HLR
	    this.port = Integer.parseInt(props.getProperty(Constants.PORTHLR));
	} catch (FileNotFoundException e) {
	} catch (IOException e) {
	}

    }


    @Override
    public void run() {
	try {
	    s = new ServerSocket(port);
	    while (running) {
		try {
		    Socket clientConnection = s.accept();
		    new VLRHandlerThread(clientConnection, this).start();
		} catch (IOException ioe) {

		}
	    }
	} catch (IOException ioe) {
	}
    }
}

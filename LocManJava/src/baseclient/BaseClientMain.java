package baseclient;

public class BaseClientMain {
    public static void main(String[] args) {
	System.out.println("Starting Base Client");
	try {

	    //initializes base client (thread) and starts it
	    BaseClient bc = new BaseClient();
	    bc.start();

	    //waits until vehicle simulation finished
	    bc.join();

	} catch (InterruptedException e) {
	    e.printStackTrace();
	}


    }
}

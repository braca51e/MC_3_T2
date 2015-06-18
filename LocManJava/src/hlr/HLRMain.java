package hlr;

public class HLRMain {
    public static void main(String[] args) {

	try {

	    //Initialize and start HLR
	    HLRServer hlr_main = new HLRServer();
	    Thread hlr_thread = new Thread(hlr_main);
	    hlr_thread.start();

	    //wait until HLR stopped
	    hlr_thread.join();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }
}

package vlr;

public class VLRMain {
    public static void main(String[] args) {
	try {
	    System.out.println("Starting VLR Server");

	    //initialize and start VLR (thread)
	    VLR vlr = new VLR();
	    Thread t = new Thread(vlr);
	    t.start();

	    // wait until VLR closes by joining its thread
	    t.join();

	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }
}

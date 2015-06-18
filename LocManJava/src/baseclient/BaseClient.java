package baseclient;

import it.polito.appeal.traci.SumoTraciConnection;
import it.polito.appeal.traci.Vehicle;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import common.Constants;


public class BaseClient extends Thread {

    // constant for property file
    private static final String propfile = "baseclient.properties";
    // constant for number of cells (16 * 16 = 256 cells)
    private static final int numCellsPerSide = 16;
    
    // number of simulated seconds of traffic simulation
    private static int numSimulationSteps = 10;

    //map bounds of simulated street map
    Rectangle2D serviceArea = null;

    //running is false after simulation of vehicles terminated    
    volatile Boolean running = true;

    //a probability with which the clients search for random vehicles
    double searchProbability = 0.0;
    
    //reference to all started threads of type BaseClientThread 
    ArrayList<BaseClientThread> baseclients = new ArrayList<BaseClientThread>();
    //store which BaseClientThread is connected to a VLR that is responsible for a certain (rectangular) LA 
    HashMap<Rectangle2D, BaseClientThread> areaToVLRMap = new HashMap<Rectangle2D, BaseClientThread>();
    //store previous location of each vehicle
    HashMap<String, Point2D> previousLocationVehicle = new HashMap<String, Point2D>();
    
    //connection to SUMO
    SumoTraciConnection conn = null;
    
    //timer that controls simulation of vehicles with SUMO
    Timer timer = new Timer();

    //number of VLRs
    int numVLR = 0;
    
    //square root of number of LAs per VLR
    int laPerVLR = 0;
    
    //ip addresses of all VLRs
    ArrayList<String> vlrServer = new ArrayList<String>();
    
    //port of VLRs
    int port = -1;

    void readProperties() {
	try {

	    Properties props = System.getProperties();
	    props.load(new BufferedInputStream(new FileInputStream(propfile)));

	    // a probability with which the clients search for random vehicles
	    searchProbability = Double.parseDouble(props.getProperty(Constants.SEARCHPROBABILITY));

	    // read number of VLRs --- this number describes to how many VLRs this client has to
	    // connect to and how many ip-addresses are provided in property file
	    numVLR = Integer.parseInt(props.getProperty(Constants.NUMVLR));

	    // read ip-addresses of all numVLR VLRs
	    for (int i = 0; i < numVLR; i++) {
		vlrServer.add(props.getProperty(Constants.VLRPREFIX + i));
	    }

	    // port of VLRs
	    port = Integer.parseInt(props.getProperty(Constants.PORTVLR));
	    
	    // duration (in seconds) of simulation
	    numSimulationSteps = Integer.parseInt(props.getProperty(Constants.NUMSIMULATIONSTEPS));

	    // we store only for one edge of the quadratic area of an VLR the number of LAs
	    // ==> actual number of location areas for whom VLR is responsible = laPerVLR*laPerVLR 
	    double sizeLA = Math.pow(4, Integer.parseInt(props.getProperty(Constants.NUMIDXLA)));
	    double sidelengthLA = (Math.sqrt(sizeLA));	    
	    laPerVLR = (int) (numCellsPerSide / sidelengthLA / Math.sqrt(numVLR));
	    
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    void initTrafficSimulation() {
	//start traffic simulator SUMO
	conn = new SumoTraciConnection(System.getProperties().getProperty(Constants.CONFIGFILE), // config
												  // file
		12345 // random seed
	);
	try {

	    conn.runServer();
	    
	    // query for coordinates of the simulated map
	    serviceArea = conn.queryBounds();

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    void setupConnections() {
	
	// determine for each VLR the coordinates of its service-area
	double stepx = (double) (serviceArea.getWidth() / (Math.sqrt(numVLR)));
	double stepy = (double) (serviceArea.getHeight() / (Math.sqrt(numVLR)));
	int i = 0;

	// start VLRs 
	for (double x = serviceArea.getX(); x < serviceArea.getX() + serviceArea.getWidth(); x += stepx) {
	    for (double y = serviceArea.getY(); y < serviceArea.getY() + serviceArea.getHeight(); y += stepy) {
				
		// create threads for base clients	
		BaseClientThread bt = new BaseClientThread(i, vlrServer.get(i), port, null, this);
		baseclients.add(bt);

		// determine coordinates of LAs of VLRs
		ArrayList<Rectangle2D> las = new ArrayList<Rectangle2D>();
		for (double lx = x; lx < x + stepx; lx += (stepx / laPerVLR)) {
		    for (double ly = y; ly < y + stepy; ly += (stepy / laPerVLR)) {
			Rectangle2D r = new Rectangle();
			r.setRect(lx, ly, (stepx / laPerVLR), (stepy / laPerVLR));
			System.out.println("LA: " + r);
			las.add(r);
			areaToVLRMap.put(r, bt);
		    }
		}

		// initialize base client with information about coordinates of LA 
		bt.setServiceAreas(las);
		bt.start();
		i++;
	    }
	}
    }

    void stopTrafficSimulation() {
	try {
	    timer.cancel();
	    timer.purge();
	    conn.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void run() {
	// initialization 
	
	//read properties from .properties file
	readProperties();
	//start SUMO simulator	
	initTrafficSimulation();
	//connect to VLRs
	setupConnections();
	
	//run simulation
	runSimulation();
	
	//busy waiting until simulation finished
	while (running) {};
	
	//close connection to traffic simulation	
	stopTrafficSimulation();
    }

    
    // Method starts regular timer that controls the traffic simulation:
    // - upon timeout all locations are queried from the cars to create location updates and random search requests
    void runSimulation() {
	
	running = true;
	
	// Start simulation after an initial delay of 1 second and then repeat task every second
	timer.scheduleAtFixedRate(new TimerTask() {
	    @Override
	    public void run() {
		try {
		    if ((conn != null) && !conn.isClosed()) {

			// trigger next simulation step (i.e. next location of cars is determined)
			conn.nextSimStep();
			
			// stop simulation after numSimulationSteps
			if (numSimulationSteps < conn.getCurrentSimStep()) {
			    
			    System.out.println("Simulation of vehicles terminates!");
			    
			    for (BaseClientThread bc : baseclients) {
				bc.stopBaseClientThread();
			    }
			    
			    running = false;
			    
			    this.cancel();
			    			    			    
			    return;
			}
			

			//access vehicles on street map
			Collection<Vehicle> vehicles = conn.getVehicleRepository().getAll()
				.values();

			for (Vehicle aVehicle : vehicles) {

			    // find current LA of vehicle and LA in previous simulation step
			    Rectangle2D currentLA = findResponsibleLA(aVehicle.getPosition());
			    Point2D previousLoc = previousLocationVehicle.get(aVehicle.getID());
			    Rectangle2D previousLA = null;
			    if (previousLoc != null) {
				previousLA = findResponsibleLA(previousLoc);
			    }

			    
			    // find the VLR that is responsible for current LA
			    BaseClientThread currentVLR = areaToVLRMap.get(currentLA);

			    // client needs to send a location update iff
			    // 1) no previous Location Area is visited 
			    // 2) previous Location Area does not equal the current Location Area
			    if ((previousLA == null) || !previousLA.equals(currentLA)) {
				
				
				currentVLR.sendLocationUpdate(aVehicle.getID(), currentLA, previousLA);
				
				// client needs to remove its entry from previous VLR iff
				// vehicle was previously managed by another VLR 
				if (previousLA != null) {
				    BaseClientThread previousVLR = areaToVLRMap.get(previousLA);
				    if (!previousVLR.equals(currentVLR)) {
					previousVLR.sendRemoveMessage(aVehicle.getID());
				    }
				}
			    }

			    // update buffer of previous locations of vehicles
			    previousLocationVehicle.put(aVehicle.getID(), aVehicle.getPosition());

			    
			    //initiate randomly a search for another vehicle
			    if (Math.random() < searchProbability) {
				Vehicle bVehicle = (Vehicle) vehicles.toArray()[(int) (Math
					.random() * conn.getVehicleRepository().getAll().size() - 1)];
				//disallow a vehicle to search for itself
				if (!bVehicle.getID().equals(aVehicle.getID())) {
				    currentVLR.sendSearch(aVehicle.getID(), bVehicle.getID());
				}
			    }
			}

		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }

	    private Rectangle2D findResponsibleLA(Point2D position) {
		Rectangle2D result = null;

		Iterator<Entry<Rectangle2D, BaseClientThread>> it = areaToVLRMap.entrySet()
			.iterator();
		while (it.hasNext() && (result == null)) {
		    Map.Entry<Rectangle2D, BaseClientThread> pairs = (Map.Entry<Rectangle2D, BaseClientThread>) it
			    .next();
		    if (pairs.getKey().contains(position)) {
			result = pairs.getKey();
		    }
		}

		return result;
	    }
	}, 1000, 1000);
    }
    
}

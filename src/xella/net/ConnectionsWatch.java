
package xella.net;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This is a thread that drives all async i/o connections
 * It also connects to new hosts when needed
 *
 */

class ConnectionsWatch extends Thread {
	
    private GnutellaEngine engine;
    private HostCatcher hostCatcher;
    private ConnectionGroup connectionGroup;
    private boolean stopping;
    
    public ConnectionsWatch(GnutellaEngine engine) {
	this.engine = engine;
	this.hostCatcher = engine.getHostCatcher();
	this.connectionGroup = engine.getConnectionGroup();
	this.stopping = false;
	this.start();
    }
    
    public void run() {
	while (!stopping) {

	    while (connectionGroup.isSmallerThanMinSize()) {
		GnutellaConnection connection = getNewConnection();
		
		if (connection != null) {
		    try {
			connectionGroup.addConnection(connection);
		    } catch (IOException e) {
			System.out.println("cant add connection to group: " + e);
			connection.disconnect(e);
		    }
		} else {
		    try {
			if (connectionGroup.size() == 0) {
			    Thread.sleep(1000);
			}
		    } catch(InterruptedException e) {}
		    break;
		}
	    }

	    try {
		Iterator iter = connectionGroup.getReadyConnections().iterator();
		while (iter.hasNext()) {
		    GnutellaConnection con = (GnutellaConnection) iter.next();
		    con.pumpConnection();
		}
	    } catch (IOException e) {
		System.out.println("cant fetch ready connections: " + e);
	    }
	}
    }
    
    public void shutdown() {
	this.stopping = true;
    }
    
    /**
     * Fetch a new connection from the list of catched hosts
     */
    public GnutellaConnection getNewConnection() {
	
	Host host = hostCatcher.getNextHost();
	if (host == null) {
	    return null;
	}

	return new GnutellaConnection(engine, host.getHostname(), host.getPort());
    }
}

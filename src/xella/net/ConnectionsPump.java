
package xella.net;

import java.io.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;

/**
 * This is a thread that drives all async i/o connections
 * It also connects to new hosts when needed
 *
 */

class ConnectionsPump extends Thread {
	
    private GnutellaEngine engine;
    private HostCatcher hostCatcher;
    private ConnectionGroup connectionGroup;
    private boolean stopping;

    private ServerSocketChannel serverSocketChannel;

    public ConnectionsPump(GnutellaEngine engine) {
	this.engine = engine;
	this.hostCatcher = engine.getHostCatcher();
	this.connectionGroup = engine.getConnectionGroup();
	this.stopping = false;
	this.start();
    }
    
    public void run() {
	    
	try {
	    openServerSocket();
	} catch (IOException e) {
	    System.out.println("Cant open serversocket: " + e);
	}
	    
	while (!stopping) {
	    
	    /*
	     * Try opening new connections if we are below the min limit of the group
	     */

	    if (connectionGroup.isSmallerThanMinSize()) {
		
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
			    Thread.sleep(100);
			}
		    } catch(InterruptedException e) {}
		}
	    }

	    /*
	     * Check for incoming connections
	     */
	    checkIncomingConnections();	    
	    
	    /*
	     * Pump all connections that are ready for io
	     */

	    //try {
		Iterator iter = connectionGroup.iterator();
		while (iter.hasNext()) {
		    GnutellaConnection con = (GnutellaConnection) iter.next();
		    con.pumpConnection();
		}
		//} catch (IOException e) {
		//System.out.println("cant fetch ready connections: " + e);
		//}
	}
    }
    
    public void shutdown() {
	this.stopping = true;
    }
    
    /**
     * Fetch a new connection from the list of catched hosts
     */
    GnutellaConnection getNewConnection() {
	
	Host host = hostCatcher.getNextHost();
	if (host == null) {
	    return null;
	}

	return new GnutellaConnection(engine, host.getHostname(), host.getPort());
    }

    /**
     * Fire up the asychronous server socket
     */
    void openServerSocket() throws IOException {
	serverSocketChannel = ServerSocketChannel.open();
	serverSocketChannel.configureBlocking(false);
	serverSocketChannel.socket().bind(new InetSocketAddress(this.engine.getListenPort()), 50);
    }

    /**
     * Check and connect incoming connections if allowed
     */

    void checkIncomingConnections() {

	try {
	    Socket incomingSocket = serverSocketChannel.accept();
	    if (incomingSocket != null) {
		System.out.println("Incoming connection...");

		/* We have someone connecting, are we busy? */
		if (connectionGroup.hasReachedMaxSize()) {
		    
		    incomingSocket.close();
		} else {
		    
		    GnutellaConnection con = null;
		    try {
			con = new GnutellaConnection(this.engine, 
						     incomingSocket.getChannel());
			connectionGroup.addConnection(con);
		    } catch (IOException e) {
			System.out.println("cant add connection to group: " + e);
			con.disconnect(e);
		    }		
		}	    
	    }
	} catch (IOException e) {
	    System.out.println("Error connection to incoming servent: " + e);
	}
    }
}

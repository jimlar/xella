
package xella.net;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This is a thread that accepts connections from the outside
 * if it's allowed by the connection group
 *
 */

class ServerSocketManager extends Thread {
	
    private ServerSocket serverSocket;
    private GnutellaEngine engine;
    private boolean stopping;
    
    public ServerSocketManager(int port,
			       GnutellaEngine engine)
	throws IOException 
    {
	this.serverSocket = new ServerSocket(port);
	this.engine = engine;
	this.stopping = false;
	this.start();
    }
    
    public void run() {
	while (!stopping) {
	    try {		
		System.out.println("Serversocket disabled!"); 
		Socket socket = serverSocket.accept();
		
		if (!engine.getConnectionGroup().hasReachedMaxSize()) {
// 		    GnutellaConnection connection = new GnutellaConnection(this.engine,
// 									   socket);
// 		    engine.getConnectionGroup().addConnection(connection);
		    

		}
	    } catch (IOException e) {
		System.out.println("Exception while accepting new connection: " + e);
		e.printStackTrace();
	    }
	}
    }

    
    public void shutdown() {
	this.stopping = true;
    }
}

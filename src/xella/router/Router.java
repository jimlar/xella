
package xella.router;

import java.io.*;
import java.util.*;

import xella.net.*;

/**
 * Message router, use this for routing of the message
 *
 * It uses a list of connections as route destinations
 */

public class Router {

    private RoutingPolicy policy;
    private Set connections;

    /**
     * Create a router with default routing policy
     */
    public Router() {
	this(new RoutingPolicy());
    }
    
    /**
     * Create a new router with custom routing policy
     */
    public Router(RoutingPolicy policy) {
	this.policy = policy;
	this.connections = new HashSet();
    }
    
    public void addConnection(GnutellaConnection connection) {
	connections.add(connection);
    }    

    public void removeConnection(GnutellaConnection connection) {
	connections.remove(connection);
    }


    public void route(PingMessage message) throws IOException {
	
    }
    
    public void route(PongMessage message) throws IOException {
	
    }

    public void route(QueryMessage message) throws IOException {
	
    }

    public void route(QueryResponseMessage message) throws IOException {
	
    }

    public void route(PushMessage message) throws IOException {
	
    }

    /**
     * Send message to all connections except the connection that 
     * sent this message (or to all if it was a new message)
     */
    private void broadcast(Message message) throws IOException {
	
	Iterator iter = connections.iterator();
	while (iter.hasNext()) {
	    GnutellaConnection connection = (GnutellaConnection) iter.next();
	    if (!message.receivedFrom(connection)) {
		connection.send(message);
	    }
	}
    }
}

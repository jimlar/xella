
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

    public void route(Message message) throws IOException {

	/* Fix hops and ttl */
	message.age();
	
	/* drop message if it is too old */
	if (message.getTTL() <= 0) {
	    message.drop();
	    return;
	}

	/* check policy for unwanted messages */
	if (!policyChecksOut(message)) {
	    message.drop();
	    return;
	}

	if (message instanceof PingMessage) {
	    route((PingMessage) message);
	} else if (message instanceof PongMessage) {
	    route((PongMessage) message);
	} else if (message instanceof PushMessage) {
	    route((PushMessage) message);
	} else if (message instanceof QueryMessage) {
	    route((QueryMessage) message);
	} else if (message instanceof QueryResponseMessage) {
	    route((QueryResponseMessage) message);
	}
    }

    public void route(PingMessage message) throws IOException {
	broadcast(message);
    }
    
    public void route(PongMessage message) throws IOException {
	System.out.println("routing pong message");	
    }

    public void route(PushMessage message) throws IOException {
	System.out.println("routing push message");	
    }

    public void route(QueryMessage message) throws IOException {
	System.out.println("routing query message");	
    }

    public void route(QueryResponseMessage message) throws IOException {
	System.out.println("routing query response message");	
    }


    private boolean policyChecksOut(Message message) {
	return (message.getTTL() <= policy.getMaxTTL());
    }

    /**
     * Send message to all connections except the connection that 
     * sent this message (or to all if it was a new message)
     */
    private void broadcast(Message message) throws IOException {
	
	System.out.println("Broadcasting: " + message);
	Iterator iter = connections.iterator();
	while (iter.hasNext()) {
	    GnutellaConnection connection = (GnutellaConnection) iter.next();
	    if (!message.receivedFrom(connection)) {
		connection.send(message);
	    }
	}
    }
}

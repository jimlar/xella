
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

    /** Cache to remember ping routes */
    private MessageCache pingCache;

    /** Cache to remember query routes */
    private MessageCache queryCache;

    /** Cache to remember query response routes */
    private MessageCache queryResponseCache;

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
	this.connections = Collections.synchronizedSet(new HashSet());
	this.pingCache = new MessageCache(policy.getRouterMessageHistorySize());
	this.queryCache = new MessageCache(policy.getRouterMessageHistorySize());
	this.queryResponseCache = new MessageCache(policy.getRouterMessageHistorySize());
    }
    
    public synchronized void addConnection(GnutellaConnection connection) {
	connections.add(connection);
    }    

    public synchronized void removeConnection(GnutellaConnection connection) {
	connections.remove(connection);
    }

    /**
     * Route a message, if message is too old, or do not conform to policy
     * the message is dropped (ie. message.drop() is called and message is not sent anywhere)
     *
     * The message is aged by this method.
     */
    public void route(Message message) throws IOException {

	/* Fix hops and ttl */
	message.age();
	
	/* drop message if it is too old */
	if (message.getTTL() <= 0) {
	    message.drop();
	    return;
	}

	/* check policy for unwanted messages */
	if (!isCompliantWithPolicy(message)) {
	    message.drop();
	    return;
	}

	if (message instanceof PingMessage) {
	    routePing((PingMessage) message);
	} else if (message instanceof PongMessage) {
	    routePong((PongMessage) message);
	} else if (message instanceof PushMessage) {
	    routePush((PushMessage) message);
	} else if (message instanceof QueryMessage) {
	    routeQuery((QueryMessage) message);
	} else if (message instanceof QueryResponseMessage) {
	    routeQueryResponse((QueryResponseMessage) message);
	}
    }

    /**
     * Ping is broadcasted
     */
    private void routePing(PingMessage message) throws IOException {
	System.out.println("routing ping message");	
	pingCache.add(message);
	broadcast(message);
    }
    
    /**
     * Pong is routed back the same way the ping came
     */
    private void routePong(PongMessage message) throws IOException {
	System.out.println("routing pong message");	
	routeBack(message, pingCache);
    }

    /**
     * Pushes are routed back the same way the query response message came
     */
    private void routePush(PushMessage message) throws IOException {
	System.out.println("routing push message");	
	routeBack(message, queryResponseCache);
    }

    /**
     * Queries are broadcasted
     */
    private void routeQuery(QueryMessage message) throws IOException {
	System.out.println("routing query message");
	queryCache.add(message);
	broadcast(message);
    }

    /**
     * Query responses are routed back the same way the query message came
     */
    private void routeQueryResponse(QueryResponseMessage message) throws IOException {
	System.out.println("routing query response message");	
	queryResponseCache.add(message);
	routeBack(message, queryCache);
    }


    /**
     * Check policy and return true if its ok to proceed with this message
     */
    private boolean isCompliantWithPolicy(Message message) {
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

    /**
     * Send message back through the path of a cached message. 
     * For instance: pongs should be routed back the way the original ping came. 
     *
     * If the connection for the path is not valid anymore the message is dropped.
     *
     * @param message the message to route
     * @param cache the messagecache to use when trying to find the path
     */
    private void routeBack(Message message, MessageCache cache) throws IOException {
	
	System.out.println("routingBack: " + message);

	Message parentMessage = cache.getByDescriptorId(message.getDescriptorId());
	if (parentMessage == null) {
	    message.drop();
	    System.out.println("message dropped: parent message not seen (or too late)");
	    return;
	} 

	Iterator iter = connections.iterator();
	while (iter.hasNext()) {
	    GnutellaConnection connection = (GnutellaConnection) iter.next();
	    if (parentMessage.receivedFrom(connection)) {
		/* successful route, send message */
		connection.send(message);
		return;
	    }
	}

	/* The path was not valid, drop message */ 
	message.drop();
	System.out.println("message dropped: connection of parent message no longer valid");
    }
}

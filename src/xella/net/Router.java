
package xella.net;

import java.io.*;
import java.util.*;

import xella.net.*;

/**
 * Message router, use this for routing of the message
 *
 * It uses a list of connections as route destinations
 */

public class Router {

    private static final int MESSAGE_HISTORY_SIZE = 10000;
    private static final int TTL_DROP_LIMIT = 10;

    /* The group of connections that messages are routed to */
    private ConnectionGroup connectionGroup;

    /** Cache to remember ping routes */
    private MessageCache pingCache;

    /** Cache to remember query routes */
    private MessageCache queryCache;

    /** Cache to remember query response routes */
    private MessageCache queryResponseCache;

    private int successfulRouteBacks;
    private int failedRouteBacks;

    /**
     * Create a new router
     *
     */
    public Router(ConnectionGroup connectionGroup) {
	this.connectionGroup = connectionGroup;
	this.pingCache = new MessageCache(MESSAGE_HISTORY_SIZE);
	this.queryCache = new MessageCache(MESSAGE_HISTORY_SIZE);
	this.queryResponseCache = new MessageCache(MESSAGE_HISTORY_SIZE);
    }
    
    void registerReceivedMessage(Message message) {
	if (message instanceof PingMessage) {
	    pingCache.add(message);	    
	} else if (message instanceof QueryMessage) {
	    queryCache.add(message);
	} else if (message instanceof QueryResponseMessage) {
	    queryResponseCache.add(message);
	}
    }

    /**
     * Route a message, if message is too old, or do not conform to policy
     * the message is dropped (ie. message.drop() is called and message is not sent anywhere)
     *
     * The message is aged by this method.
     */
    
    void routeReceivedMessage(Message message) {
	route(message);
    }
    
    void routeNewMessage(Message message) {
	route(message);
    }

    private void route(Message message) {

	/* Fix hops and ttl */
	message.age();
	
	/* drop message if it is too old */
	if (message.getTTL() <= 0) {
	    //System.out.println("dropping message (ttl <= 0): " + message);
	    //message.drop();
	    return;
	}

	/* check policy for unwanted messages */
	if (message.getTTL() > TTL_DROP_LIMIT) {
	    //System.out.println("dropping message (ttl too high): " + message);
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
    private void routePing(PingMessage message) {
	pingCache.add(message);
	broadcast(message);
    }
    
    /**
     * Pong is routed back the same way the ping came
     */
    private void routePong(PongMessage message) {
	routeBack(message, pingCache);
    }

    /**
     * Pushes are routed back the same way the query response message came
     */
    private void routePush(PushMessage message) {
	routeBack(message, queryResponseCache);
    }

    /**
     * Queries are broadcasted
     */
    private void routeQuery(QueryMessage message) {
	queryCache.add(message);
	broadcast(message);
    }

    /**
     * Query responses are routed back the same way the query message came
     */
    private void routeQueryResponse(QueryResponseMessage message) {
	queryResponseCache.add(message);
	routeBack(message, queryCache);
    }

    /**
     * Send message to all connections except the connection that 
     * sent this message (or to all if it was a new message)
     */
    private void broadcast(Message message) {
	
	Iterator iter = connectionGroup.iterator();
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
    private void routeBack(Message message, MessageCache cache) {
	
	Message parentMessage = cache.getByMessageId(message.getMessageId());
	if (parentMessage == null) {
	    message.drop();
	    failedRouteBacks++;
	    //System.out.println("routeBack: message dropped: parent message not seen (or too late)."
	    //		       + " s=" + successfulRouteBacks + ", f=" + failedRouteBacks);
	    return;
	} 

	Iterator iter = connectionGroup.iterator();
	while (iter.hasNext()) {
	    GnutellaConnection connection = (GnutellaConnection) iter.next();
	    if (parentMessage.receivedFrom(connection)) {
		/* successful route, send message */
		connection.send(message);
		successfulRouteBacks++;
		return;
	    }
	}

	/* The path was not valid, drop message */ 
	message.drop();
	failedRouteBacks++;
	//System.out.println("message dropped: connection of parent message no longer valid."
	//		   + " s=" + successfulRouteBacks + ", f=" + failedRouteBacks);
    }
}

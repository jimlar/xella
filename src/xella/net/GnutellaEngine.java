
package xella.net;

import java.io.*;
import java.util.*;

/**
 * The main class of this gnutella protocol implementation
 * Use this class to connect to the network, then add message listeners to start
 * receiving messages.
 */

public class GnutellaEngine {

    private ConnectionGroup connectionGroup;
    private Router router;
    private Collection messageListeners;
    private Collection connectionListeners;
    private ConnectionsWatch connectionsWatch;
    private int port;
    private ServerSocketManager serverSocketManager;

    /**
     * @param numberOfConnections the number of connections to keep up
     * @param port the port for incoming connections
     */
    public GnutellaEngine(int minConnections, int maxConnections, int port) 
	throws IOException
    {
	this.connectionGroup = new ConnectionGroup(minConnections, maxConnections);
	this.port = port;
	this.router = new Router(10, 2000, this.connectionGroup);
	this.messageListeners = new ArrayList();
	this.connectionListeners = new ArrayList();
    }

    public void start() throws IOException {
	if (connectionsWatch == null) {
	    connectionsWatch = new ConnectionsWatch(this, connectionGroup);
	    addMessageListener(connectionsWatch);
	}

	if (serverSocketManager == null) {
	    serverSocketManager = new ServerSocketManager(port, this);
	}
    }

    public void stop() {
	if (connectionsWatch != null) {
	    connectionsWatch.shutdown();
	    connectionsWatch = null;
	}
	if (serverSocketManager != null) {
	    serverSocketManager.shutdown();
	    serverSocketManager = null;
	}
    }

    public void addMessageListener(MessageListener messageListener) {
	messageListeners.add(messageListener);
    }
    
    public void addConnectionListener(ConnectionListener connectionListener) {
	connectionListeners.add(connectionListener);
    }
    
    public void send(Message message) {
	router.route(message);
    }

    public void addHost(String host, int port) {
	if (connectionsWatch != null) {
	    connectionsWatch.addHost(host, port);
	}
    }

    void registerReceivedMessage(Message message) {

	router.registerReceivedMessage(message);
	Iterator iter = messageListeners.iterator();
	while (iter.hasNext()) {
	    MessageListener listener = (MessageListener) iter.next();
	    
	    if (message instanceof PingMessage) {
		listener.receivedPing((PingMessage) message);
	    } else if (message instanceof PongMessage) {
		listener.receivedPong((PongMessage) message);
	    } else if (message instanceof PushMessage) {
		listener.receivedPush((PushMessage) message);
	    } else if (message instanceof QueryMessage) {
		listener.receivedQuery((QueryMessage) message);
	    } else if (message instanceof QueryResponseMessage) {
		listener.receivedQueryResponse((QueryResponseMessage) message);
	    }
	}

	router.route(message);
    }

    ConnectionGroup getConnectionGroup() {
	return connectionGroup;
    }

    void connecting(ConnectionInfo info) {
	Iterator iter = connectionListeners.iterator();
	while (iter.hasNext()) {
	    ConnectionListener listener = (ConnectionListener) iter.next();
	    listener.connecting(info);
	}
    }
    
    void connected(ConnectionInfo info) {
	Iterator iter = connectionListeners.iterator();
	while (iter.hasNext()) {
	    ConnectionListener listener = (ConnectionListener) iter.next();
	    listener.connected(info);
	}
    }
    
    void connectFailed(ConnectionInfo info) {
	Iterator iter = connectionListeners.iterator();
	while (iter.hasNext()) {
	    ConnectionListener listener = (ConnectionListener) iter.next();
	    listener.connectFailed(info);
	}
    }
    
    void hostIgnored(ConnectionInfo info) {
	Iterator iter = connectionListeners.iterator();
	while (iter.hasNext()) {
	    ConnectionListener listener = (ConnectionListener) iter.next();
	    listener.hostIgnored(info);
	}
    }
    
    void disconnected(ConnectionInfo info) {
	Iterator iter = connectionListeners.iterator();
	while (iter.hasNext()) {
	    ConnectionListener listener = (ConnectionListener) iter.next();
	    listener.disconnected(info);
	}
    }
    
    void statusChange(ConnectionInfo info) {
	Iterator iter = connectionListeners.iterator();
	while (iter.hasNext()) {
	    ConnectionListener listener = (ConnectionListener) iter.next();
	    listener.statusChange(info);
	}
    }
}

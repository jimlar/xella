
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
    private HostCatcher hostCatcher;
    private Router router;
    private Collection messageListeners;
    private Collection connectionListeners;
    private ConnectionsPump connectionsPump;
    private int listenPort;

    /**
     * @param numberOfConnections the number of connections to keep up
     * @param listenPort the port for incoming connections
     */
    public GnutellaEngine(int minConnections, int maxConnections, int listenPort) 
	throws IOException
    {
	this.messageListeners = new ArrayList();
	this.connectionListeners = new ArrayList();
	this.listenPort = listenPort;
	this.connectionGroup = new ConnectionGroup(minConnections, maxConnections);
	this.router = new Router(this.connectionGroup);
	this.hostCatcher = new HostCatcher(this);
    }

    public void start() throws IOException {
	if (connectionsPump == null) {
	    connectionsPump = new ConnectionsPump(this);
	}
    }

    public void stop() {
	if (connectionsPump != null) {
	    connectionsPump.shutdown();
	    connectionsPump = null;
	}
    }

    public void addMessageListener(MessageListener messageListener) {
	messageListeners.add(messageListener);
    }
    
    public void addConnectionListener(ConnectionListener connectionListener) {
	connectionListeners.add(connectionListener);
    }
    
    public void send(Message message) {
	router.routeNewMessage(message);
    }

    /**
     * Add prioritized host for connecting 
     */
    public void addHost(String host, int port) {
	hostCatcher.addHost(host, port);
    }

    public int getListenPort() {
	return this.listenPort;
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

	router.routeReceivedMessage(message);
    }

    ConnectionGroup getConnectionGroup() {
	return connectionGroup;
    }

    HostCatcher getHostCatcher() {
	return hostCatcher;
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

    void hostIgnored(Host host) {
	Iterator iter = connectionListeners.iterator();
	while (iter.hasNext()) {
	    ConnectionListener listener = (ConnectionListener) iter.next();
	    listener.hostIgnored(host);
	}
    }
    
    void hostDiscovered(Host host) {
	Iterator iter = connectionListeners.iterator();
	while (iter.hasNext()) {
	    ConnectionListener listener = (ConnectionListener) iter.next();
	    listener.hostDiscovered(host);
	}
    }    
}

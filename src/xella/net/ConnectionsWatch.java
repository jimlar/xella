
package xella.net;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This is a watch thread that connects a new client connection if the
 * connection groups has become too small
 *
 */

class ConnectionsWatch extends Thread implements MessageListener {
	
    private GnutellaEngine engine;
    private List hosts;
    private List pongList;
    private ConnectionGroup connectionGroup;
    private boolean stopping;
    
    public ConnectionsWatch(GnutellaEngine engine,
			    ConnectionGroup connectionGroup) {
	this.engine = engine;
	this.connectionGroup = connectionGroup;
	this.stopping = false;
	this.pongList = Collections.synchronizedList(new ArrayList());
	this.hosts = Collections.synchronizedList(new ArrayList());
	this.start();
    }
    
    public void addHost(String host, int port) {
	try {
	    synchronized (hosts) {
		hosts.add(new URL("http://" + host + ":" + port));
	    }
	} catch (MalformedURLException e) {
	    throw new RuntimeException("bad url?" + e);
	}
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
		    break;
		}
	    }

	    //try {
		//Iterator iter = connectionGroup.getReadyConnections().iterator();
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
    public GnutellaConnection getNewConnection() {
	
	synchronized (hosts) {
	    if (hosts.size() > 0) {
		URL url = (URL) hosts.remove(0);
		return new GnutellaConnection(engine, url.getHost(), url.getPort());
	    }
	}

	synchronized (pongList) {
	    if (pongList.size() > 0) {
		PongMessage message = (PongMessage) pongList.remove(0);
		return new GnutellaConnection(engine, message.getHost(), message.getPort());
	    }
	}

	return null;
    }
    
    public void receivedPong(PongMessage message) {
	/* catch the host */
	synchronized (pongList) {
	    if (pongList.size() < 200) {
		String host = message.getHost();
		if (host.startsWith("192.168") || host.startsWith("10.")) {
		    this.engine.hostIgnored(new ConnectionInfo(host, message.getPort(), true, "Host ignored", 0, 0));
		} else {
		    pongList.add(message);
		    this.engine.hostDiscovered(new ConnectionInfo(host, message.getPort(), true, "Host found", 0, 0));
		}
	    }
	}
    }
    
    public void receivedPing(PingMessage message) {}
    public void receivedPush(PushMessage message) {}
    public void receivedQuery(QueryMessage message) {}
    public void receivedQueryResponse(QueryResponseMessage message) {}
}

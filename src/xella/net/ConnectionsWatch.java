
package xella.net;

import java.io.*;
import java.net.*;
import java.util.*;

class ConnectionsWatch extends Thread implements MessageListener {
	
    private GnutellaEngine engine;
    private List hosts;
    private List pongList;
    private GnutellaConnection connections[];
    private boolean stopping;
    
    public ConnectionsWatch(GnutellaEngine engine,
			    GnutellaConnection connections[]) {
	this.engine = engine;
	this.connections = connections;
	this.stopping = false;
	this.pongList = new ArrayList();
	this.hosts = new ArrayList();
	this.start();
    }
    
    public void addHost(String host, int port) {
	try {
	    hosts.add(new URL("http://" + host + ":" + port));
	} catch (MalformedURLException e) {
	    throw new RuntimeException("bad url?" + e);
	}
    }

    public void run() {
	while (!stopping) {
	    for (int i = 0; i < connections.length; i++) {
		if (connections[i] == null || connections[i].isClosed()) {
		    try {
			connections[i] = getNewConnection();
		    } catch (IOException e) {
			System.out.println("Exception while opening new connection: " + e);
			e.printStackTrace();
		    }
		}
	    }
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {}
	}
    }
    
    public void shutdown() {
	this.stopping = true;
    }
    
    /**
     * Fetch a new connection from the list of catched hosts
     */
    public GnutellaConnection getNewConnection() throws IOException {
	
	if (hosts.size() > 0) {
	    System.out.println("opening new connection, using added host");
	    URL url = (URL) hosts.remove(0);
	    return new GnutellaConnection(engine, url.getHost(), url.getPort());
	}

	if (pongList.size() > 0) {
	    System.out.println("opening new connection, using pong host");
	    PongMessage message = (PongMessage) pongList.remove(0);
	    return new GnutellaConnection(engine, message.getHost(), message.getPort());
	}

	return null;
    }
    
    public void receivedPong(PongMessage message) {
	/* catch the host */
	if (pongList.size() < 200) {
	    pongList.add(message);
	}
    }
    
    public void receivedPing(PingMessage message) {}
    public void receivedPush(PushMessage message) {}
    public void receivedQuery(QueryMessage message) {}
    public void receivedQueryResponse(QueryResponseMessage message) {}
}

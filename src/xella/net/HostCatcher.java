
package xella.net;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This is a class that catches specific messages and makes host ips available
 * for us
 *
 */

class HostCatcher implements MessageListener {

    private static final int MAX_HOSTLIST_SIZE = 500;
    private static final int KEEP_SEEN_HOSTS = 5000;

    private GnutellaEngine engine;
    private LinkedList seenHosts;
    private Set addedHosts;
    private Set caughtHosts;
    
    public HostCatcher (GnutellaEngine engine) {
	this.engine = engine;
	this.caughtHosts = new LinkedHashSet();
	this.addedHosts = new LinkedHashSet();
	this.seenHosts = new LinkedList();
	engine.addMessageListener(this);
    }
    
    public synchronized void addHost(String host, int port) {
	addedHosts.add(new Host(host, port));
    }

    /**
     * The manually added hosts are prioritized
     *
     * The caught hosts are returned in reported speed order
     *
     * @return a host from the list of known hosts
     * @return null if no hosts are known
     */
    public synchronized Host getNextHost() {
	
	Host host = null;

	if (addedHosts.size() > 0) {
	    host = (Host) addedHosts.iterator().next();
	    addedHosts.remove(host);
	
	} else if (caughtHosts.size() > 0) {
	    host = (Host) caughtHosts.iterator().next();
	    caughtHosts.remove(host);
	}

	if (host != null) {
	    if (seenHosts.size() >= KEEP_SEEN_HOSTS) {
		seenHosts.removeFirst();
	    }
	    seenHosts.add(host);
	}

	return host;
    }
    
    public synchronized void receivedPong(PongMessage message) {
	/* catch the host */
	if (caughtHosts.size() < MAX_HOSTLIST_SIZE) {
	    Host host = new Host(message.getHost(), message.getPort());

	    if (!seenHosts.contains(host)) {
		if (host.isNonPublic()) {
		    this.engine.hostIgnored(host);
		} else {
		    caughtHosts.add(host);
		    this.engine.hostDiscovered(host);
		}
	    } 
	}
    }
    public void receivedPing(PingMessage message) {}
    public void receivedPush(PushMessage message) {}
    public void receivedQuery(QueryMessage message) {}
    public void receivedQueryResponse(QueryResponseMessage message) {}
}

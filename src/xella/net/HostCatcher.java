
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

    private static final int MAX_HOSTLIST_SIZE = 200;
	
    private GnutellaEngine engine;
    private List addedHosts;
    private List caughtHosts;
    
    public HostCatcher (GnutellaEngine engine) {
	this.engine = engine;
	this.caughtHosts = Collections.synchronizedList(new ArrayList());
	this.addedHosts = Collections.synchronizedList(new ArrayList());
	engine.addMessageListener(this);
    }
    
    public synchronized void addHost(String host, int port) {
	addedHosts.add(new Host(host, port));
    }

    /**
     * The manually added hosts are prioritized
     *
     * @return a host from the list of known hosts
     * @return null if no hosts are known
     */
    public synchronized Host getNextHost() {
	
	Host host = null;

	if (addedHosts.size() > 0) {
	    host = (Host) addedHosts.remove(0);
	
	} else if (caughtHosts.size() > 0) {
	    host = (Host) caughtHosts.remove(0);
	}

	return host;
    }
    
    public synchronized void receivedPong(PongMessage message) {
	/* catch the host */
	if (caughtHosts.size() < MAX_HOSTLIST_SIZE) {
	    Host host = new Host(message.getHost(), message.getPort());
	    if (host.isNonPublic()) {
		this.engine.hostIgnored(host);
	    } else {
		caughtHosts.add(host);
		this.engine.hostDiscovered(host);
	    }
	}
    }
    public void receivedPing(PingMessage message) {}
    public void receivedPush(PushMessage message) {}
    public void receivedQuery(QueryMessage message) {}
    public void receivedQueryResponse(QueryResponseMessage message) {}
}


package xella.net;

import java.util.*;

/**
 * Class that represents a group of connections
 */

class ConnectionGroup {

    private Set connections;
    private int wantedSize;

    public ConnectionGroup(int wantedSize) {
	this.wantedSize = wantedSize;
	this.connections = new HashSet();	
    }
    
    public synchronized void addConnection(GnutellaConnection connection) {
	connections.add(connection);
    }

    public synchronized void removeConnection(GnutellaConnection connection) {
	connections.remove(connection);
    }

    public synchronized Iterator iterator() {
	return new ArrayList(connections).iterator();
    }

    public synchronized int getWantedSize() {
	return wantedSize;
    }

    public synchronized int size() {
	return connections.size();
    }

    public boolean isSmallerThanWanted() {
	return size() < getWantedSize();
    }
}

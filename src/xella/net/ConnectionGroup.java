
package xella.net;

import java.util.*;

/**
 * Class that represents a group of connections
 */

class ConnectionGroup {

    private Set connections;
    private int minSize;
    private int maxSize;

    public ConnectionGroup(int minSize, int maxSize) {
	this.minSize = minSize;
	this.maxSize = maxSize;
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

    public synchronized boolean isSmallerThanMinSize() {
	return connections.size() < minSize;
    }

    public synchronized boolean hasReachedMaxSize() {
	return connections.size() >= maxSize;
    }
}

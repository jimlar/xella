
package xella.net;

/**
 * Represents a host with ip, port and speed
 */
public class Host {

    private String hostname;
    private int port;
    private int speed;
    
    Host(String hostname, int port) {
	this(hostname, port, -1);
    }

    Host(String hostname, int port, int speed) {
	this.hostname = hostname;
	this.port = port;
	this.speed = speed;
    }

    public String getHostname() {
	return hostname;
    }


    public int getPort() {
	return port;
    }

    public boolean isNonPublic() {
	return hostname.startsWith("10.")
	    || hostname.startsWith("192.168.");
    }

    public String toString() {
	return "[Host " + hostname + ":" + port + ", speed=" + speed + "]";
    }

    public boolean equals(Object o) {
	if (o == null || !o.getClass().equals(this.getClass())) {
	    return false;
	}
	
	Host other = (Host) o;
	return this.hostname.equals(other.hostname)
	    && this.port == other.port;
    }

    public int hashCode() {
	return hostname.hashCode() ^ port;
    }
}

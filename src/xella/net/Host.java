
package xella.net;


public class Host {

    private String hostname;
    private int port;
    
    Host(String hostname, int port) {
	this.hostname = hostname;
	this.port = port;
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
}

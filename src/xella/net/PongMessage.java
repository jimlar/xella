
package xella.net;


public class PongMessage extends Message {
    
    private String host;
    private int port;
    private int numShared;
    private int kilobytesShared;

    PongMessage(MessageHeader header, 
		String host, 
		int port, 
		int numShared, 
		int kilobyteShared) 
    {
	super(header);
	this.host = host;
	this.port = port;
	this.numShared = numShared;
	this.kilobytesShared = kilobytesShared;
    }
    
    public String toString() {
	return "PongMessage: host=" + host 
	    + ", port=" + port
	    + ", numShared=" + numShared
	    + ", kilobytesShared=" + kilobytesShared
	    + ", " + getHeader().toString();
    }
}

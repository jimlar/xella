
package xella.net;

import java.io.*;

public class PongMessage extends Message {
    
    private String host;
    private int port;
    private int numShared;
    private int kilobytesShared;

    PongMessage(GnutellaConnection receivedFrom,
		MessageHeader header, 
		String host, 
		int port, 
		int numShared, 
		int kilobytesShared) 
    {
	super(receivedFrom, header);
	this.host = host;
	this.port = port;
	this.numShared = numShared;
	this.kilobytesShared = kilobytesShared;
    }
    
    public void send(GnutellaOutputStream out) throws IOException {
	getHeader().send(out);
	out.write16Bit(port);
	out.writeIPNumber(host);
	out.write32Bit(numShared);
	out.write32Bit(kilobytesShared);
    }

    public String toString() {
	return "PongMessage: host=" + host 
	    + ", port=" + port
	    + ", numShared=" + numShared
	    + ", kilobytesShared=" + kilobytesShared;
    }
}

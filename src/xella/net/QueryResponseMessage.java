
package xella.net;

import java.io.*;
import java.util.*;

/**
 * This class holds info on a single query hit
 *
 */

public class QueryResponseMessage extends Message {

    private byte serventId[];
    private String hostIP;
    private int port;
    private int hostSpeed;
    private List queryHits;

    QueryResponseMessage(GnutellaConnection receivedFrom,
			 MessageHeader header, 
			 byte serventId[],
			 String hostIP, 
			 int port, 
			 int hostSpeed, 
			 List queryHits) 
    {
	super(receivedFrom, header);
	this.serventId = serventId;
	this.hostIP = hostIP;
	this.port = port;
	this.hostSpeed = hostSpeed;
	this.queryHits = queryHits;
    }
    
    
    public void send(GnutellaOutputStream out) throws IOException {
	getHeader().send(out);
	throw new IOException("not fully implemented yet!");
    }

    public String toString() {
	String toReturn = "QueryResponseMessage: host=" + hostIP
	    + ", port=" + port
	    + ", hostSpeed=" 
	    + ", hits:\n";

	Iterator iter = queryHits.iterator();
	while (iter.hasNext()) {
	    QueryHit hit = (QueryHit) iter.next();
	    toReturn += "  " + hit + "\n";
	}

	toReturn += "header=" + getHeader().toString();
	return toReturn;
    }
}
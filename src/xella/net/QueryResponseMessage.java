
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

    public String getHostIP() {
	return hostIP;
    }
    
    public int getPort() {
	return port;
    }

    public int getHostSpeed() {
	return hostSpeed;
    }

    public List getQueryHits() {
	return queryHits;
    }

    public boolean isResponseFor(QueryMessage message) {

	if (message == null) {
	    return false;
	}
        byte ourDescriptor[] = getDescriptorId();
        byte otherDescriptor[] = message.getDescriptorId();
        for (int i = 0; i < ourDescriptor.length; i++) {
            if (ourDescriptor[i] != otherDescriptor[i]) {
                return false;
            }
        }
        return true;
    }
    
    public ByteBuffer getByteBuffer() {
	ByteBuffer buffer = ByteBuffer.allocate(MessageHeader.SIZE + MessageHeader.getMessageBodySize());
	buffer.put(getHeader().getByteBuffer());
	throw new RuntimeException("not implemented yet!");
    }
    
    public static QueryResponseMessage receive(MessageHeader messageHeader, GnutellaConnection connection) 
	throws IOException
    {
	GnutellaInputStream in = connection.getInputStream();

	int numberOfHits = in.read8Bit();
	int port = in.read16Bit();
	String hostIP = in.readIPNumber();
	int hostSpeed = in.read32Bit();	
	List queryHits = new ArrayList();
	
	/*
	 * NOTE: this needs to take into account the strange bear-share extensions
	 *
	 */
	
	/* Read all hits */
	for (int i = 0; i < numberOfHits; i++) {
	    int fileIndex = in.read32Bit();
	    int fileSize = in.read32Bit();
	    String fileName = in.readAsciiString();
	    
	    /* throw away extra null terminator */
	    in.read8Bit();
	    
	    queryHits.add(new QueryHit(fileIndex, fileSize, fileName));
	}
	
	byte serventId[] = in.readServentIdentifier();
	
	return new QueryResponseMessage(connection,
					messageHeader, 
					serventId, 
					hostIP, 
					port, 
					hostSpeed, 
					queryHits); 
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

	return toReturn;
    }
}

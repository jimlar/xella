
package xella.net;

import java.io.*;
import java.nio.ByteBuffer;
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
    private byte extendedData[];

    QueryResponseMessage(GnutellaConnection receivedFrom,
			 MessageHeader header, 
			 byte serventId[],
			 String hostIP, 
			 int port, 
			 int hostSpeed, 
			 List queryHits,
			 byte extendedData[]) 
    {
	super(receivedFrom, header);
	this.serventId = serventId;
	this.hostIP = hostIP;
	this.port = port;
	this.hostSpeed = hostSpeed;
	this.queryHits = queryHits;
	this.extendedData = extendedData;
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
	
	return message.getMessageId().equals(getMessageId());
    }
    
    public void writeTo(ByteBuffer buffer) {
	getHeader().writeTo(buffer);

	buffer.put(ByteEncoder.encode8Bit(queryHits.size()));

	buffer.put(ByteEncoder.encode16Bit(port));
	buffer.put(ByteEncoder.encodeIPNumber(hostIP));
	buffer.put(ByteEncoder.encode32Bit(hostSpeed));

	/* Put query hits */
	Iterator iter = queryHits.iterator();
	while (iter.hasNext()) {
	    QueryHit hit = (QueryHit) iter.next();
	    hit.writeTo(buffer);
	}
	
	/* write the extended data if any */
	if (extendedData != null) {
	    buffer.put(extendedData);
	}

	buffer.put(serventId);
    }
    
    public static QueryResponseMessage readFrom(ByteBuffer buffer,
						MessageHeader messageHeader,
						GnutellaConnection connection) {

	int bufferStartPos = buffer.position();

	int numberOfHits = ByteDecoder.decode8Bit(buffer);
	int port = ByteDecoder.decode16Bit(buffer);
	String hostIP = ByteDecoder.decodeIPNumber(buffer);
	int hostSpeed = ByteDecoder.decode32Bit(buffer);
	List queryHits = new ArrayList();
	
	/* Read all hits */
	for (int i = 0; i < numberOfHits; i++) {
	    queryHits.add(QueryHit.readFrom(buffer));
	}
	
	/*
	 * Read extended queryhit data if present
	 */
	
	byte extendedData[] = null;
	int expectedSize = messageHeader.getMessageBodySize();
	int bytesRead = buffer.position() - bufferStartPos;
	int extendedDataSize = expectedSize - bytesRead - 16;

	if (extendedDataSize > 0) {
	    extendedData = new byte[extendedDataSize];
	    buffer.get(extendedData);
	}

	byte serventId[] = new byte[16];
	buffer.get(serventId);
	
	return new QueryResponseMessage(connection,
					messageHeader, 
					serventId, 
					hostIP, 
					port, 
					hostSpeed, 
					queryHits,
					extendedData); 
    }
    
    public String toString() {
	String toReturn = "QueryResponseMessage: host=" + hostIP
	    + ", port=" + port;

	if (extendedData == null) {
	    toReturn += ", no extended data";
	} else {
	    toReturn += ", extended data=";
	    for (int i = 0; i < extendedData.length; i++) {
		toReturn += Integer.toHexString(extendedData[i] < 0 ? extendedData[i] + 256 : extendedData[i]);
		if (i != (extendedData.length - 1)) {
		    toReturn += ",";
		}
	    }
	}

	toReturn += ", hostSpeed=" + hostSpeed
	    + ", hits:\n";

	Iterator iter = queryHits.iterator();
	while (iter.hasNext()) {
	    QueryHit hit = (QueryHit) iter.next();
	    toReturn += "  " + hit;
	    if (iter.hasNext()) {
		toReturn += "\n";
	    }
	}

	return toReturn;
    }
}

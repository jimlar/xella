
package xella.net;

import java.io.*;
import java.nio.ByteBuffer;

public class QueryMessage extends Message {
    
    private String searchString;
    private int minSpeed;

    QueryMessage(GnutellaConnection receivedFrom,
		 MessageHeader header, 
		 String searchString, 
		 int minSpeed) {
	super(receivedFrom, header);
	this.searchString = searchString;
	this.minSpeed = minSpeed;
    }

    public String getSearchString() {
	return searchString;
    }

    public int getMinSpeed() {
	return minSpeed;
    }
    
    public void writeTo(ByteBuffer buffer) {
	getHeader().writeTo(buffer);
	buffer.put(ByteEncoder.encode16Bit(minSpeed));
	buffer.put(ByteEncoder.encodeAsciiString(searchString));
	buffer.put(ByteEncoder.encode8Bit(0));
    }

    public static QueryMessage readFrom(ByteBuffer buffer,
					MessageHeader messageHeader, 
					GnutellaConnection connection) {

	int minSpeed = ByteDecoder.decode16Bit(buffer);
	int stringSize = messageHeader.getMessageBodySize() - 3;
	String searchString = ByteDecoder.decodeAsciiString(buffer, stringSize);

	/* discard the null terminator */	
	int dummy = ByteDecoder.decode8Bit(buffer);
	
	return new QueryMessage(connection, messageHeader, searchString, minSpeed);
    }

    public String toString() {
	return "QueryMessage: query=" + searchString + ", minSpeed=" + minSpeed;
    }

    public boolean equals(Object o) {
	if (o == null || !o.getClass().equals(this.getClass())) {
	    return false;
	}
	
	QueryMessage other = (QueryMessage) o;
	return this.getHeader().equals(other.getHeader())
	    && this.searchString.equals(other.searchString)
	    && this.minSpeed == other.minSpeed;
    }

    public int hashCode() {
	return getHeader().hashCode()
	    ^ searchString.hashCode()
	    ^ minSpeed;
    }
}

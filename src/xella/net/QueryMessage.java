
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
    
    public ByteBuffer getByteBuffer() {
	ByteBuffer buffer = ByteBuffer.allocate(MessageHeader.SIZE + getHeader().getMessageBodySize());
	buffer.put(getHeader().getByteBuffer());
	buffer.put(ByteEncoder.encode16Bit(minSpeed));
	buffer.put(ByteEncoder.encodeAsciiString(searchString));
	buffer.put(ByteEncoder.encode8Bit(0));
	return buffer;
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
}

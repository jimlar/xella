
package xella.net;

import java.io.*;

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
	ByteBuffer buffer = ByteBuffer.allocate(MessageHeader.SIZE + MessageHeader.getMessageBodySize());
	buffer.put(getHeader().getByteBuffer());
	buffer.put(ByteEncoder.encode16Bit(minSpeed));
	buffer.put(ByteEncoder.encodeAsciiString(searchString));
	buffer.put(ByteEncoder.encode8Bit(0));
	return buffer;
    }

    public static QueryMessage receive(MessageHeader messageHeader, GnutellaConnection connection) 
	throws IOException
    {
	GnutellaInputStream in = connection.getInputStream();

	int minSpeed = in.read16Bit();
	int stringSize = messageHeader.getMessageBodySize() - 3;
	String searchString = in.readAsciiString(stringSize);
	/* discard the null terminator */	
	in.read8Bit();
	
	return new QueryMessage(connection, messageHeader, searchString, minSpeed);
    }

    public String toString() {
	return "QueryMessage: query=" + searchString + ", minSpeed=" + minSpeed;
    }
}

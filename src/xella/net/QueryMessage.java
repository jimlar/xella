
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
    
    public void send(GnutellaOutputStream out) throws IOException {
	getHeader().send(out);
	out.write16Bit(minSpeed);
	out.writeAsciiString(searchString);
	
	/* null terminate search string */
	out.write8Bit(0);
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

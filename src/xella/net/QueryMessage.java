
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

    public String toString() {
	return "QueryMessage: query=" + searchString + ", minSpeed=" + minSpeed;
    }
}

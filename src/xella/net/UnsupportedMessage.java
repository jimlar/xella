
package xella.net;

import java.io.*;
import java.nio.ByteBuffer;

public class UnsupportedMessage extends Message {

    UnsupportedMessage(GnutellaConnection receivedFrom, MessageHeader header) {
	super(receivedFrom, header);
    }
    
    public void writeTo(ByteBuffer buffer) {
	throw new RuntimeException("cant send unsupported messages!");
    }

    public static UnsupportedMessage readFrom(ByteBuffer buffer,
					      MessageHeader messageHeader, 
					      GnutellaConnection connection) {

	return new UnsupportedMessage(connection, messageHeader);
    }

    public String toString() {
	return "UnsupportedMessage: " + getHeader().toString();
    }

    public boolean equals(Object o) {
	if (o == null || !o.getClass().equals(this.getClass())) {
	    return false;
	}
	
	UnsupportedMessage other = (UnsupportedMessage) o;
	return this.getHeader().equals(other.getHeader());
    }

    public int hashCode() {
	return getHeader().hashCode();
    }
}

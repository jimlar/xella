
package xella.net;

import java.io.*;
import java.nio.ByteBuffer;

public class PingMessage extends Message {

    PingMessage(GnutellaConnection receivedFrom, MessageHeader header) {
	super(receivedFrom, header);
    }
    
    public void writeTo(ByteBuffer buffer) {
	getHeader().writeTo(buffer);
    }

    public static PingMessage readFrom(ByteBuffer buffer, 
				       MessageHeader header, 
				       GnutellaConnection connection) {
	return new PingMessage(connection, header);
    }
    
    public String toString() {
	return "PingMessage";
    }

    public boolean equals(Object o) {
	if (o == null || !o.getClass().equals(this.getClass())) {
	    return false;
	}
	
	PingMessage other = (PingMessage) o;
	return this.getHeader().equals(other.getHeader());
    }

    public int hashCode() {
	return getHeader().hashCode();
    }
}

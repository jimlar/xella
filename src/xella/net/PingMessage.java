
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
}

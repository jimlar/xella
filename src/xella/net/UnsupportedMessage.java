
package xella.net;

import java.io.*;
import java.nio.ByteBuffer;

public class UnsupportedMessage extends Message {

    UnsupportedMessage(GnutellaConnection receivedFrom, MessageHeader header) {
	super(receivedFrom, header);
    }
    
    public ByteBuffer getByteBuffer() {
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
}

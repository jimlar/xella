
package xella.net;

import java.io.*;

public class UnsupportedMessage extends Message {

    UnsupportedMessage(GnutellaConnection receivedFrom, MessageHeader header) {
	super(receivedFrom, header);
    }
    
    public void send(GnutellaOutputStream out) throws IOException {
	throw new IOException("cant send unsupported messages!");
    }

    public String toString() {
	return "UnsupportedMessage: " + getHeader().toString();
    }
}


package xella.net;

import java.io.*;

public class PingMessage extends Message {

    PingMessage(GnutellaConnection receivedFrom, MessageHeader header) {
	super(receivedFrom, header);
    }
    
    public void send(GnutellaOutputStream out) throws IOException {
	getHeader().send(out);
    }

    public String toString() {
	return "PingMessage: " + getHeader().toString();
    }
}

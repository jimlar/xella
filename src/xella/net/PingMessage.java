
package xella.net;

import java.io.*;

public class PingMessage extends Message {

    PingMessage(GnutellaConnection receivedFrom, MessageHeader header) {
	super(receivedFrom, header);
    }
    
    public void send(GnutellaOutputStream out) throws IOException {
	getHeader().send(out);
    }

    public static PingMessage receive(MessageHeader header, GnutellaConnection connection) 
	throws IOException
    {
	return new PingMessage(connection, header);
    }

    public String toString() {
	return "PingMessage";
    }
}

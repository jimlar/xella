
package xella.net;

import java.io.*;

public class UnsupportedMessage extends Message {

    UnsupportedMessage(GnutellaConnection receivedFrom, MessageHeader header) {
	super(receivedFrom, header);
    }
    
    public void send(GnutellaOutputStream out) throws IOException {
	throw new IOException("cant send unsupported messages!");
    }

    public static UnsupportedMessage receive(MessageHeader messageHeader, GnutellaConnection connection) 
	throws IOException
    {
	GnutellaInputStream in = connection.getInputStream();

	/* unsupported message type, just read past the body */
	for (int i = 0; i < messageHeader.getMessageBodySize(); i++) {
	    in.read();
	}

	return new UnsupportedMessage(connection, messageHeader);
    }

    public String toString() {
	return "UnsupportedMessage: " + getHeader().toString();
    }
}

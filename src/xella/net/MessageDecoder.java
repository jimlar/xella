
package xella.net;

import java.io.*;

/**
 * This class can decode Gnutella messages
 *
 */

class MessageDecoder {

    private GnutellaConnection connection;

    public MessageDecoder(GnutellaConnection connection) {
	this.connection = connection;
    }

    public MessageHeader decodeNextMessageHeader() throws IOException {
	return MessageHeader.receive(connection);
    }

    public Message decodeNextMessage(MessageHeader messageHeader) throws IOException {
	
	switch (messageHeader.getMessageType()) {

	case GnutellaConstants.PAYLOAD_PING:
	    return PingMessage.receive(messageHeader, connection);

 	case GnutellaConstants.PAYLOAD_PONG:
	    return PongMessage.receive(messageHeader, connection);

 	case GnutellaConstants.PAYLOAD_PUSH:
	    return PushMessage.receive(messageHeader, connection);

 	case GnutellaConstants.PAYLOAD_QUERY:
	    return QueryMessage.receive(messageHeader, connection);

 	case GnutellaConstants.PAYLOAD_QUERY_HIT:
	    return QueryResponseMessage.receive(messageHeader, connection);

	default:
	    return UnsupportedMessage.receive(messageHeader, connection);
	}
    }
}

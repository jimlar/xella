
package xella.net;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * This class can decode Gnutella messages
 *
 */

class MessageDecoder {

    private GnutellaConnection connection;

    public MessageDecoder(GnutellaConnection connection) {
	this.connection = connection;
    }

    public Message decodeMessage(MessageHeader messageHeader, ByteBuffer buffer) {
	
	switch (messageHeader.getMessageType()) {

	case Message.PAYLOAD_PING:
	    return PingMessage.readFrom(buffer, messageHeader, connection);

 	case Message.PAYLOAD_PONG:
	    return PongMessage.readFrom(buffer, messageHeader, connection);

 	case Message.PAYLOAD_PUSH:
	    return PushMessage.readFrom(buffer, messageHeader, connection);

 	case Message.PAYLOAD_QUERY:
	    return QueryMessage.readFrom(buffer, messageHeader, connection);

 	case Message.PAYLOAD_QUERY_HIT:
	    return QueryResponseMessage.readFrom(buffer, messageHeader, connection);

	default:
	    return UnsupportedMessage.readFrom(buffer, messageHeader, connection);
	}
    }
}

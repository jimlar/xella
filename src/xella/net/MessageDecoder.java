
package xella.net;

import java.io.*;
import java.util.*;

/**
 * This class can decode Gnutella messages
 *
 */

class MessageDecoder {

    private GnutellaInputStream in;

    public MessageDecoder(GnutellaInputStream in) {
	this.in = in;
    }

    public Message decodeNextMessage() throws IOException {
	MessageHeader messageHeader = decodeHeader();
	
	/* Header decoded, decode body (thrash unsupported messages) */
	switch (messageHeader.getMessageType()) {

	case GnutellaConstants.PAYLOAD_PING:
	    return new PingMessage(null, messageHeader);

 	case GnutellaConstants.PAYLOAD_PONG:
 	    return decodePongMessage(messageHeader);

 	case GnutellaConstants.PAYLOAD_PUSH:
	    return decodePushMessage(messageHeader);

 	case GnutellaConstants.PAYLOAD_QUERY:
	    return decodeQueryMessage(messageHeader);

 	case GnutellaConstants.PAYLOAD_QUERY_HIT:
	    return decodeQueryResponseMessage(messageHeader);

	default:
	    /* unknown message type, just read past the body */
	    for (int i = 0; i < messageHeader.getMessageBodySize(); i++) {
		in.read();
	    }
	    return new UnsupportedMessage(null, messageHeader);
	}
    }

    private MessageHeader decodeHeader() 
	throws IOException
    {	
	byte descriptorId[] = new byte[16];
	int bytesRead = in.read(descriptorId);
 	if (bytesRead != descriptorId.length) {
 	    throw new IOException("EOF before descriptor id read (read " 
 				  + bytesRead + " bytes)");
 	}
	
	int payloadDescriptor = in.read8Bit();
	int ttl = in.read8Bit();
	int hops = in.read8Bit();
	int payloadLength = in.read32Bit();
	    
	return new MessageHeader(descriptorId, payloadDescriptor, ttl, hops, payloadLength);
    }

    private PongMessage decodePongMessage(MessageHeader messageHeader)
	throws IOException
    {
	int port = in.read16Bit();
	String host = in.readIPNumber();
	int numShared = in.read32Bit();
	int kilobytesShared = in.read32Bit();
	return new PongMessage(null, messageHeader, host, port, numShared, kilobytesShared);
    }

    private PushMessage decodePushMessage(MessageHeader messageHeader)
	throws IOException
    {
	byte serventId[] = in.readServentIdentifier();
	int fileIndex = in.read32Bit();
	String host = in.readIPNumber();
	int port = in.read16Bit();

	return new PushMessage(null, messageHeader, serventId, host, port, fileIndex);
    }

    private QueryMessage decodeQueryMessage(MessageHeader messageHeader)
	throws IOException
    {
	int minSpeed = in.read16Bit();

	int stringSize = messageHeader.getMessageBodySize() - 3;
	String searchString = in.readAsciiString(stringSize);

	/* discard the null terminator */	
	in.read8Bit();
	
	return new QueryMessage(null, messageHeader, searchString, minSpeed);
    }

    private QueryResponseMessage decodeQueryResponseMessage(MessageHeader messageHeader)
	throws IOException
    {
	int numberOfHits = in.read8Bit();
	int port = in.read16Bit();
	String hostIP = in.readIPNumber();
	int hostSpeed = in.read32Bit();	
	List queryHits = new ArrayList();

	/*
	 * NOTE: this needs to take into account the strange bear-share extensions
	 *
	 */

	/* Read all hits */
	for (int i = 0; i < numberOfHits; i++) {
	    int fileIndex = in.read32Bit();
	    int fileSize = in.read32Bit();
	    String fileName = in.readAsciiString();

	    /* throw away extra null terminator */
	    in.read8Bit();
	    
	    queryHits.add(new QueryHit(fileIndex, fileSize, fileName));
	}

	byte serventId[] = in.readServentIdentifier();

	return new QueryResponseMessage(null,
					messageHeader, 
					serventId, 
					hostIP, 
					port, 
					hostSpeed, 
					queryHits); 
    }
}

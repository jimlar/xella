

package xella.net;

import java.io.*;
import java.util.*;

/**
 * This class can generate Gnutella packets
 *
 */

public class MessageFactory {
    
    private static MessageFactory instance;

    public static MessageFactory getInstance() {
	if (instance == null) {
	    instance = new MessageFactory();
	}
	return instance;
    }

    /* prevent construction */
    private MessageFactory() {}

    public PingMessage createPingMessage() {
	
	MessageHeader header = new MessageHeader(new MessageId(),
						 GnutellaConstants.PAYLOAD_PING, 
						 GnutellaConstants.TTL, 
						 0, 
						 0);
	return new PingMessage(null, header);
    }

    public PongMessage createPongMessage(PingMessage  replyTo,
					 String hostIP, 
					 int port, 
					 int numShared, 
					 int kilobytesShared) {

	MessageHeader header = new MessageHeader(replyTo.getMessageId(), 
						 GnutellaConstants.PAYLOAD_PONG, 
						 GnutellaConstants.TTL, 
						 0, 
						 GnutellaConstants.PONG_BODY_LENGTH);
	return new PongMessage(null,
			       header,
			       hostIP,
			       port,
			       numShared,
			       kilobytesShared);
    }

    public QueryMessage createQueryMessage(int minSpeed, String searchString) {

	/* minspeed is 16 bit and the string is null terminated */
	int size = 2 + searchString.length() + 1;

	MessageHeader header = new MessageHeader(new MessageId(),
						 GnutellaConstants.PAYLOAD_QUERY, 
						 GnutellaConstants.TTL, 
						 0,
						 size);
	
	return new QueryMessage(null, header, searchString, minSpeed);
    }

    public QueryResponseMessage createQueryResponseMessage(QueryMessage replyTo,
							   byte serventId[],
							   String hostIP,
							   int port,
							   int hostSpeed,
							   List queryHits) 
    {	
	/* two sizes before and after the resultset are fixed */
	int bodySize = 11 + 16;
	
	/* Add resultset size */
	Iterator iter = queryHits.iterator();
	while (iter.hasNext()) {
	    QueryHit hit = (QueryHit) iter.next();
	    bodySize += hit.size();
	}

	MessageHeader header = new MessageHeader(replyTo.getMessageId(),
						 GnutellaConstants.PAYLOAD_QUERY_HIT, 
						 GnutellaConstants.TTL, 
						 0,
						 bodySize);
	
	return new QueryResponseMessage(null,
					header, 
					serventId,
					hostIP, 
					port, 
					hostSpeed, 
					queryHits,
					null);
    }

    public PushMessage createPushMessage(QueryResponseMessage replyTo,
					 byte serventId[], 
					 int fileIndex, 
					 String hostIP, 
					 int port) {

	MessageHeader header = new MessageHeader(replyTo.getMessageId(),
						 GnutellaConstants.PAYLOAD_PUSH,
						 GnutellaConstants.TTL, 
						 0, 
						 26);

	return new PushMessage(null, header, serventId, hostIP, port, fileIndex);
    }
}



package xella.net;

import java.io.*;
import java.util.*;

/**
 * This class can generate Gnutella packets
 *
 */

public class MessageFactory {
    
    private static MessageFactory instance;
    private long nextMessageId = 1;

    public static MessageFactory getInstance() {
	if (instance == null) {
	    instance = new MessageFactory();
	}
	return instance;
    }

    /* prevent construction */
    private MessageFactory() {}

    public PingMessage createPingMessage() {
	
	MessageHeader header = new MessageHeader(getNewDescriptorId(),
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

	MessageHeader header = new MessageHeader(replyTo.getDescriptorId(), 
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

	MessageHeader header = new MessageHeader(getNewDescriptorId(),
						 GnutellaConstants.PAYLOAD_QUERY, 
						 GnutellaConstants.TTL, 
						 0,
						 size);
	
	return new QueryMessage(null, header, searchString, minSpeed);
    }

    public QueryResponseMessage createQueryResponseMessage() {
	throw new RuntimeException("not implemented");
    }

    public PushMessage createPushMessage(QueryResponseMessage replyTo,
					 byte serventId[], 
					 int fileIndex, 
					 String hostIP, 
					 int port) {

	MessageHeader header = new MessageHeader(replyTo.getDescriptorId(),
						 GnutellaConstants.PAYLOAD_PUSH,
						 GnutellaConstants.TTL, 
						 0, 
						 26);

	return new PushMessage(null, header, serventId, hostIP, port, fileIndex);
    }

    /**
     * As far as I understand the java API docs this is not valid if you are
     * on a machine with a non-public IP number (ie. 192.168.*.* or 10.*.*.*)
     *
     * But it is the method that Phex of Furi uses so i'll stick with it for now
     *
     */

    private byte[] getNewDescriptorId() {
	
	long messageId = nextMessageId++;
	byte toReturn[] = new byte[16];
	for (int i = 0; i < 8; i++) {
	    toReturn[i] = (byte) ((messageId >> (i * 8)) & 0xff);
	}

	return toReturn;
    }

//    private byte[] getNewDescriptorId() {
// 	String vmId = (new java.rmi.dgc.VMID()).toString();
// 	byte toReturn[] = new byte[16];
	
// 	for (int i = 0, j = 0; i < vmId.length(); i++) {
// 	    if (i < 16) {
// 		toReturn[j] = (byte) vmId.charAt(i);
// 	    } else {
// 		toReturn[j] ^= (byte) vmId.charAt(i);
// 	    }
// 	    j++;
// 	    if (j >= 16) {
// 		j = 0;
// 	    }
// 	}

// 	return toReturn;
//     }
}

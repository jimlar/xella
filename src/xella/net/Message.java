
package xella.net;

import java.io.*;
import java.nio.ByteBuffer;

public abstract class Message {

    public static final int PAYLOAD_PING = 0x00;
    public static final int PAYLOAD_PONG = 0x01;
    public static final int PAYLOAD_PUSH = 0x40;
    public static final int PAYLOAD_QUERY = 0x80;
    public static final int PAYLOAD_QUERY_HIT = 0x81;

    private GnutellaConnection receivedFrom;
    private MessageHeader header;
    private boolean isDropped;

    /**
     * Use null for receivedFrom for messages that has no originator
     *( i.e. not received from a connection, created new)
     */
    Message(GnutellaConnection receivedFrom,
	    MessageHeader      header) {
	this.receivedFrom = receivedFrom;
	this.header = header;
    }

    public MessageId getMessageId() {
	return header.getMessageId();
    }

    public int getHops() {
	return header.getHops();
    }

    public int getTTL() {
	return header.getTTL();
    }

    public boolean receivedFrom(GnutellaConnection connection) {
	if (receivedFrom == null) {
	    return connection == null;
	} else {
	    return receivedFrom.equals(connection);
	}
    }

    public abstract void writeTo(ByteBuffer buffer);


    public int size() {
	return MessageHeader.SIZE + getHeader().getMessageBodySize();
    }
    /**
     * Increase hops and decrease TTL of message
     */
    public void age() {
	getHeader().age();
    }
    
    /**
     * Mark message as dropped
     */
    public void drop() {
	this.isDropped = true;
	if (receivedFrom != null) {
	    receivedFrom.increaseDroppedMessages();
	}
    }
    
    protected MessageHeader getHeader() {
	return this.header;
    }

    public String toString() {
	return "Message: " + header.toString();
    }
}

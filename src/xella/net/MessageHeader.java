
package xella.net;

import java.io.*;
import java.nio.ByteBuffer;

public class MessageHeader {

    public static final int SIZE = 23;

    private MessageId messageId;
    private int payloadDescriptor;
    private int ttl;
    private int hops;
    private int payloadLength;

    MessageHeader(MessageId messageId,
		  int payloadDescriptor, 
		  int ttl, 
		  int hops, 
		  int payloadLength) {

	this.messageId = messageId;
	this.payloadDescriptor = payloadDescriptor;
	this.ttl = ttl;
	this.hops = hops;
	this.payloadLength = payloadLength;
    }

    public MessageId getMessageId() {
	return messageId;
    }

    public int getMessageBodySize() {
	return payloadLength;
    }

    public int getMessageType() {
	return payloadDescriptor;
    }

    public int getHops() {
	return hops;
    }

    public int getTTL() {
	return ttl;
    }

    /**
     * Increase hops and decrease TTL of message
     */
    public void age() {
	hops++;
	ttl--;
    }
    
    public void writeTo(ByteBuffer buffer) {	
	messageId.writeTo(buffer);
	buffer.put(ByteEncoder.encode8Bit(payloadDescriptor));
	buffer.put(ByteEncoder.encode8Bit(ttl));
	buffer.put(ByteEncoder.encode8Bit(hops));
	buffer.put(ByteEncoder.encode32Bit(payloadLength));
    }
    
    public static MessageHeader readFrom(ByteBuffer buffer) 
	throws IOException
    {
	MessageId messageId = MessageId.readFrom(buffer);
	int payloadDescriptor = ByteDecoder.decode8Bit(buffer);
	int ttl = ByteDecoder.decode8Bit(buffer);
	int hops = ByteDecoder.decode8Bit(buffer);
	int payloadLength = ByteDecoder.decode32Bit(buffer);
	    
	return new MessageHeader(messageId, payloadDescriptor, ttl, hops, payloadLength);
    }

    public String toString() {

	String toReturn = "MessageHeader: ";

	boolean showId = false;

	if (showId) {
	    toReturn += messageId + ", ";
	}

	toReturn += "payload=0x" + byteToHex(payloadDescriptor)
	    + ", ttl=" + ttl
	    + ", hops=" + hops
	    + ", length=" + payloadLength;

	return toReturn;
    }

    private String byteToHex(int b) {
	String ret = Integer.toHexString(b & 0xff);
	if (ret.length() < 2) {
	    return "0" + ret; 
	} else {
	    return ret;
	}
    }

    public boolean equals(Object o) {

	if (o == null || !o.getClass().equals(this.getClass())) {
	    return false;
	}
	
	MessageHeader mh = (MessageHeader) o;
	return mh.messageId.equals(this.messageId)
	    && mh.payloadDescriptor == this.payloadDescriptor
	    && mh.ttl == this.ttl
	    && mh.hops == this.hops
	    && mh.payloadLength == this.payloadLength;
    }

    public int hashCode() {
	return messageId.hashCode()
	    ^ payloadDescriptor
	    ^ ttl
	    ^ hops
	    ^ payloadLength;
    }
}

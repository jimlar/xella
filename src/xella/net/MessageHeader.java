
package xella.net;

import java.io.*;
import java.nio.ByteBuffer;

public class MessageHeader {

    public static final int SIZE = 23;

    private byte descriptorId[];
    private int payloadDescriptor;
    private int ttl;
    private int hops;
    private int payloadLength;

    MessageHeader(byte descriptorId[] , 
		  int payloadDescriptor, 
		  int ttl, 
		  int hops, 
		  int payloadLength) {

	this.descriptorId = descriptorId;
	this.payloadDescriptor = payloadDescriptor;
	this.ttl = ttl;
	this.hops = hops;
	this.payloadLength = payloadLength;
    }

    public byte[] getDescriptorId() {
	return descriptorId;
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
    
    public ByteBuffer getByteBuffer() {	
	ByteBuffer buf = ByteBuffer.allocate(SIZE);
	buf.put(descriptorId);
	buf.put(ByteEncoder.encode8Bit(payloadDescriptor));
	buf.put(ByteEncoder.encode8Bit(ttl));
	buf.put(ByteEncoder.encode8Bit(hops));
	buf.put(ByteEncoder.encode32Bit(payloadLength));
	return buf;
    }
    
    public static MessageHeader readFrom(ByteBuffer buffer) 
	throws IOException
    {
	byte descriptorId[] = new byte[16];
	buffer.get(descriptorId);
	
	int payloadDescriptor = ByteDecoder.decode8Bit(buffer);
	int ttl = ByteDecoder.decode8Bit(buffer);
	int hops = ByteDecoder.decode8Bit(buffer);
	int payloadLength = ByteDecoder.decode32Bit(buffer);
	    
	return new MessageHeader(descriptorId, payloadDescriptor, ttl, hops, payloadLength);
    }

    public String toString() {

	String toReturn = "MessageHeader: ";

	boolean showId = false;

	if (showId) {
	    toReturn += "id=[";
	    
	    for (int i = 0; i < descriptorId.length; i++) {
		if (i == descriptorId.length - 1) {
		    toReturn += byteToHex(descriptorId[i]) + "]";
		} else {
		    toReturn += byteToHex(descriptorId[i]) + ",";
		}
	    }
	    
	    toReturn += ", ";
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
}

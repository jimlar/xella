
package xella.protocol;


public class MessageHeader {

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

	toReturn += "payload=" + byteToHex(payloadDescriptor)
	    + ", ttl=" + ttl
	    + ", hops=" + hops
	    + ", length=" + payloadLength;

	return toReturn;
    }

    private String byteToHex(int b) {
	String ret = Integer.toHexString(((int)((char) b)) & 0xff);
	if (ret.length() < 2) {
	    return "0" + ret; 
	} else {
	    return ret;
	}
    }
}

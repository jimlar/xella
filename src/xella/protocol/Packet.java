
package xella.protocol;


public class Packet {

    private byte descriptorId[];
    private int payloadDescriptor;
    private int ttl;
    private int hops;
    private int payloadLength;

    Packet(byte descriptorId[] , int payloadDescriptor, int ttl, int hops, int payloadLength) {
	this.descriptorId = descriptorId;
	this.payloadDescriptor = payloadDescriptor;
	this.ttl = ttl;
	this.hops = hops;
	this.payloadLength = payloadLength;
    }

    public int getSize() {
	return payloadLength;
    }

    public String toString() {

	String toReturn = "Packet: id=[";
	
	for (int i = 0; i < descriptorId.length; i++) {
	    if (i == descriptorId.length - 1) {
		toReturn += byteToHex(descriptorId[i]) + "]";
	    } else {
		toReturn += byteToHex(descriptorId[i]) + ",";
	    }
	}

	toReturn += ", payload=" + byteToHex(payloadDescriptor)
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

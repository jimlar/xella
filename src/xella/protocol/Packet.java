
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

    public String toString() {
	return "Packet: payload=" + payloadDescriptor
	    + ", ttl=" + ttl
	    + ", hops=" + hops
	    + ", length=" + payloadLength;
    }
}

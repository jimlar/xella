

package xella.protocol;

import java.io.*;

/**
 * This class can decode Gnutella packets
 *
 */

public class PacketDecoder {

    public static Packet decodePacket(InputStream in) throws IOException {
	Packet packet = decodeDescriptorHeader(in);
	System.out.println("Packet decoded: " + packet);
	return packet;
    }

    private static Packet decodeDescriptorHeader(InputStream in) 
	throws IOException
    {	
	System.out.println("Reading descriptor header...");
	byte buffer[] = new byte[GnutellaConstants.DESCRIPTOR_HEADER_LENGTH];
	int bytesRead = in.read(buffer);
	if (bytesRead != buffer.length) {
	    throw new IOException("EOF before descriptor header read (read " 
				  + bytesRead + " bytes)");
	}

	System.out.println("done! Decoding header...");	

	/* 
	 * 0 - 15  = descriptor id 
	 * 16      = payload descriptor (type)
	 * 17      = ttl
	 * 18      = hops
	 * 19 - 22 = payload length
	 */

	byte descriptorId[] = new byte[16];
	System.arraycopy(buffer, 0, descriptorId, 0, descriptorId.length);
	
	int payloadDescriptor = (int) buffer[16];
	int ttl = (int) buffer[17];
	int hops = (int) buffer[18];
	
	int payloadLength = (((int) buffer[19]) << 6)
	    + (((int) buffer[20]) << 4)
	    + (((int) buffer[21]) << 2)
	    + (int) buffer[22];
	    
	System.out.println("done!");	

	return new Packet(descriptorId, payloadDescriptor, ttl, hops, payloadLength);
    }
}

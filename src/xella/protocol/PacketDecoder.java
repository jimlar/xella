
package xella.protocol;

import java.io.*;

/**
 * This class can decode Gnutella packets
 *
 */

class PacketDecoder {

    public static Packet decodePacket(InputStream in) throws IOException {
	Packet packet = decodeDescriptorHeader(in);
	
	/* Header decoded, decode body (thrash it for now... ;) */
	for (int i = 0; i < packet.getSize(); i++) {
	    in.read();
	}

	return packet;
    }

    private static Packet decodeDescriptorHeader(InputStream in) 
	throws IOException
    {	
	byte buffer[] = new byte[GnutellaConstants.DESCRIPTOR_HEADER_LENGTH];
	int bytesRead = in.read(buffer);
 	if (bytesRead != buffer.length) {
 	    throw new IOException("EOF before descriptor header read (read " 
 				  + bytesRead + " bytes)");
 	}

	FileOutputStream fileOut = new FileOutputStream("packet.out");
	fileOut.write(buffer, 0, bytesRead);

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
	
	/* Little endian */
	int payloadLength = (buffer[19] & 0xff) 
	    | (buffer[20] & 0xff) << 8 
	    | (buffer[21] & 0xff) << 16 
	    | (buffer[22] & 0xff) << 24;
	    
	return new Packet(descriptorId, payloadDescriptor, ttl, hops, payloadLength);
    }
}



package xella.protocol;

import java.io.*;

/**
 * This class can generate Gnutella packets
 *
 */

public class PacketGenerator {

    public static void sendPing(OutputStream out) throws IOException {
	sendDescriptorHeader(out, GnutellaConstants.PAYLOAD_PING, 7, 0, 0);
    }

    private static void sendDescriptorHeader(OutputStream out,
					     int          payloadDescriptor,
					     int          ttl,
					     int          hops,
					     int          payloadLength) 
	throws IOException
    {	
	byte buffer[] = new byte[GnutellaConstants.DESCRIPTOR_HEADER_LENGTH];
	
	/* 
	 * 0 - 15  = descriptor id 
	 * 16      = payload descriptor (type)
	 * 17      = ttl
	 * 18      = hops
	 * 19 - 22 = payload length
	 */

	buffer[16] = (byte) payloadDescriptor;
	buffer[17] = (byte) ttl;
	buffer[18] = (byte) hops;
	
	/* Little endian */
	buffer[19] = (byte) (payloadLength); 
	buffer[20] = (byte) (payloadLength >> 8);
	buffer[21] = (byte) (payloadLength >> 16);
	buffer[22] = (byte) (payloadLength >> 24);

	out.write(buffer);
    }
}

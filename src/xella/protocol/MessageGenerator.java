

package xella.protocol;

import java.io.*;

/**
 * This class can generate Gnutella packets
 *
 */

class MessageGenerator {

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

	System.arraycopy(getDescriptorId(), 0, buffer, 0, 16);

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

    /**
     * As far as I understand the java API docs this is not valid if you are
     * on a machine with a non-public IP number (ie. 192.168.*.* or 10.*.*.*)
     *
     * But it is the method that Phex of Furi uses so i'll stick with it for now
     *
     */

    private static byte[] getDescriptorId() {
	String vmId = (new java.rmi.dgc.VMID()).toString();
	byte toReturn[] = new byte[16];
	
	for (int i = 0, j = 0; i < vmId.length(); i++) {
	    if (i < 16) {
		toReturn[j] = (byte) vmId.charAt(i);
	    } else {
		toReturn[j] ^= (byte) vmId.charAt(i);
	    }
	    j++;
	    if (j >= 16) {
		j = 0;
	    }
	}

	return toReturn;
    }
}

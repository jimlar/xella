

package xella.protocol;

import java.io.*;
import java.util.*;

/**
 * This class can generate Gnutella packets
 *
 */

class MessageGenerator {
    
    private OutputStream out;

    public MessageGenerator(OutputStream out) {
	this.out = out;
    }

    public void sendPing() throws IOException {
	sendDescriptorHeader(GnutellaConstants.PAYLOAD_PING, 
			     GnutellaConstants.TTL, 
			     0, 
			     0);
    }

    public void sendPong(String hostIP, 
			 int port, 
			 int numShared, 
			 int kilobytesShared) 
	throws IOException 
    {
	sendDescriptorHeader(GnutellaConstants.PAYLOAD_PONG, 
			     GnutellaConstants.TTL, 
			     0, 
			     GnutellaConstants.PONG_BODY_LENGTH);	
    
	write16Bit(port);
	writeIPNumber(hostIP);
	write32Bit(numShared);
	write32Bit(kilobytesShared);
    }

    public void sendQuery(int minSpeed, String searchString) 
	throws IOException 
    {
	/* minspeed is 16 bit and the string is null terminated */
	int size = 2 + searchString.length() + 1;

	sendDescriptorHeader(GnutellaConstants.PAYLOAD_QUERY, 
			     GnutellaConstants.TTL, 
			     0, 
			     size);	
	write16Bit(minSpeed);
	writeAsciiString(searchString);

	/* null terminate search string */
	write8Bit(0);
    }

    public void sendQueryHit() throws IOException {
    }

    public void sendPush() throws IOException {
    }

    private void sendDescriptorHeader(int payloadDescriptor,
				      int ttl,
				      int hops,
				      int payloadLength) 
	throws IOException
    {	
	out.write(getDescriptorId());

	write8Bit(payloadDescriptor);
	write8Bit(ttl);
	write8Bit(hops);
	write32Bit(payloadLength);
    }

    /**
     * As far as I understand the java API docs this is not valid if you are
     * on a machine with a non-public IP number (ie. 192.168.*.* or 10.*.*.*)
     *
     * But it is the method that Phex of Furi uses so i'll stick with it for now
     *
     */

    private byte[] getDescriptorId() {
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


    private void write8Bit(int value) throws IOException {
	out.write((byte) value);
    }

    private void write16Bit(int value) throws IOException {
	value = (int)(short) value;

	/* Little endian */
	out.write((byte) (value)); 
	out.write((byte) (value >> 8));
    }

    private void write32Bit(int value) throws IOException {
	/* Little endian */
	out.write((byte) (value)); 
	out.write((byte) (value >> 8));
	out.write((byte) (value >> 16));
	out.write((byte) (value >> 24));
    }

    private void writeIPNumber(String ipNumber) throws IOException {
	StringTokenizer st = new StringTokenizer(ipNumber, ".");
	if (st.countTokens() != 4) {
	    throw new IOException("ip number must be on dotted decimal form (got " 
				  + ipNumber + ")");
	}
	
	while (st.hasMoreTokens()) {
	    String b = st.nextToken();
	    try {
		write8Bit(Integer.parseInt(b));
	    } catch (NumberFormatException e) {
		throw new IOException("ip number must be on dotted decimal form (got " 
				      + ipNumber + ")");
	    }
	}
    }

    private void writeAsciiString(String str) throws IOException {
	byte bytes[] = str.getBytes("ascii");
	out.write(bytes);
    }
}

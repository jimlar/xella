
package xella.net;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Handles byte to java type decodings with the endianess required by gnutella
 * protocol.
 *
 */

public class ByteDecoder {

    public static int decode8Bit(ByteBuffer buffer) {
	int b = buffer.get();
	return b < 0 ? b + 256 : b;
    }

    public static int decode16Bit(ByteBuffer buffer) {
	int loByte = decode8Bit(buffer);
	int hiByte = decode8Bit(buffer);
	return (hiByte & 0xff) << 8 | (loByte & 0xff); 
    }

    public static int decode32Bit(ByteBuffer buffer) {
	byte b[] = new byte[4];
	buffer.get(b);

	return (b[0] & 0xff) 
	    | (b[1] & 0xff) << 8 
	    | (b[2] & 0xff) << 16 
	    | (b[3] & 0xff) << 24;	
    }

    public static String decodeIPNumber(ByteBuffer buffer) {
	byte b[] = new byte[4];
	buffer.get(b);
	
	return (b[0] < 0 ? b[0] + 256 : b[0]) 
	    + "." + (b[1] < 0 ? b[1] + 256 : b[1]) 
	    + "." + (b[2] < 0 ? b[2] + 256 : b[2]) 
	    + "." + (b[3] < 0 ? b[3] + 256 : b[3]); 
    }

    public static String decodeAsciiString(byte b[]) {
	try {
	    return new String(b, "ascii");
	} catch (UnsupportedEncodingException e) {
	    //Should not really happen...
	    return null;
	}
    }

    public static String decodeAsciiString(ByteBuffer buffer, int length) {
	byte b[] = new byte[length];
	buffer.get(b);	
	return decodeAsciiString(b);
    }

    /**
     * Read until null termination is found
     */
    public static String decodeAsciiString(ByteBuffer buffer) {
	StringBuffer stringBuffer = new StringBuffer();
	byte b = buffer.get();

	while (b != 0) {

	    stringBuffer.append((char) b);
	    b = buffer.get();	
	}
	return stringBuffer.toString();
    }
}

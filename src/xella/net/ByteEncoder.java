
package xella.net;

import java.io.*;
import java.util.*;

/**
 * Byte encoder that supports encodings needed by gnutella
 *
 */

public class ByteEncoder {

    public static byte encode8Bit(int value) {
	return (byte) value;
    }
    
    public static byte[] encode16Bit(int value) {
	value = (int)(short) value;
	
	byte b[] = new byte[2];
	/* Little endian */
	b[0] = (byte) value; 
	b[1] = (byte) (value >> 8);
	return b;
    }
    
    public static byte[] encode32Bit(int value) {

	byte b[] = new byte[4];

	/* Little endian */
	b[0] = (byte) value; 
	b[1] = (byte) (value >> 8);
	b[2] = (byte) (value >> 16);
	b[3] = (byte) (value >> 24);
	return b;
    }
    
    public static byte[] encodeIPNumber(String ipNumber) {

	StringTokenizer st = new StringTokenizer(ipNumber, ".");
	if (st.countTokens() != 4) {
	    throw new IllegalArgumentException("ip number must be on dotted decimal form (got " 
					       + ipNumber + ")");
	}
	
	byte bytes[] = new byte[4];
	int i = 0;
	while (st.hasMoreTokens()) {
	    String b = st.nextToken();
	    try {
		bytes[i++] = encode8Bit(Integer.parseInt(b));
	    } catch (NumberFormatException e) {
		throw new IllegalArgumentException("ip number must be on dotted decimal form (got " 
				      + ipNumber + ")");
	    }
	}
	return bytes;
    }

    public static byte[] encodeAsciiString(String str) {
	try {
	    byte bytes[] = str.getBytes("ascii");
	    return bytes;
	} catch (UnsupportedEncodingException e) {
	    //This cant really happen...
	    return null;
	}
    }
}

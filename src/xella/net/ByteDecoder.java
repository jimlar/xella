
package xella.net;

import java.io.*;
import java.util.*;

/**
 * Handles byte to java type decodings with the endianess required by gnutella
 * protocol.
 *
 */

public class ByteDecoder {

    public static int decode8Bit(byte b) {
	return (int)(char)b;
    }

    public static int decode16Bit(byte b[]) {
	int loByte = decode8Bit(b[0]);
	int hiByte = decode8Bit(b[1]);
	return (hiByte & 0xff) << 8 | (loByte & 0xff); 
    }

    public static int decode32Bit(byte b[]) {	
	return (b[0] & 0xff) 
	    | (b[1] & 0xff) << 8 
	    | (b[2] & 0xff) << 16 
	    | (b[3] & 0xff) << 24;	
    }

    public static String decodeIPNumber(byte b[]) {
	return (b[0] < 0 ? b[0] + 256 : b[0]) 
	    + "." + (b[1] < 0 ? b[1] + 256 : b[1]) 
	    + "." + (b[2] < 0 ? b[2] + 256 : b[2]) 
	    + "." + (b[3] < 0 ? b[3] + 256 : b[3]); 
    }

    public static String decodeAsciiString(byte b[]) {
	return new String(b, "ascii");
    }
}

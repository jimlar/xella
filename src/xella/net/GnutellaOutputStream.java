
package xella.net;

import java.io.*;
import java.util.*;

/**
 * OutputStream that wraps another stream and adds utilities useful for gnutella
 * (Little endian codings and other stuff)
 *
 */

public class GnutellaOutputStream extends OutputStream {

    private OutputStream out;

    GnutellaOutputStream(OutputStream out) {
	this.out = out;
    }

    public void write8Bit(int value) throws IOException {
	out.write((byte) value);
    }
    
    public void write16Bit(int value) throws IOException {
	value = (int)(short) value;
	
	/* Little endian */
	out.write((byte) (value)); 
	out.write((byte) (value >> 8));
    }
    
    public void write32Bit(int value) throws IOException {
	/* Little endian */
	out.write((byte) (value)); 
	out.write((byte) (value >> 8));
	out.write((byte) (value >> 16));
	out.write((byte) (value >> 24));
    }
    
    public void writeIPNumber(String ipNumber) throws IOException {
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

    public void writeAsciiString(String str) throws IOException {
	byte bytes[] = str.getBytes("ascii");
	out.write(bytes);
    }

    public void writeServentIdentifier(byte serventId[]) throws IOException {
	out.write(serventId);
    }

    public void close() throws IOException {
	out.close();
    }

    public void flush() throws IOException {
	out.flush();
    } 

    public void write(byte[] b) throws IOException {
	out.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
	out.write(b, off, len);
    }

    public void write(int b) throws IOException {
	out.write(b);
    } 
}

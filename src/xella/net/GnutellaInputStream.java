
package xella.net;

import java.io.*;
import java.util.*;

/**
 * InputStream that wraps another stream and adds utilities useful for gnutella
 * (Little endian codings and other stuff)
 *
 */

public class GnutellaInputStream extends InputStream {

    private InputStream in;

    GnutellaInputStream(InputStream in) {
	this.in = in;
    }

    public int read8Bit() 
	throws IOException
    {
	int byteRead = in.read();
	if (byteRead == -1) {
	    throw new IOException("EOF before 8-bit value read");
	}
	
	return byteRead;	
    }

    public int read16Bit() 
	throws IOException 
    {
	int loByte = in.read();
	int hiByte = in.read();
	if (loByte == -1 || hiByte == -1) {
	    throw new IOException("EOF before 16-bit value read");
	}
	return (hiByte & 0xff) << 8 | (loByte & 0xff); 
    }

    public int read32Bit() 
	throws IOException 
    {
	byte buffer[] = new byte[4];
	int bytesRead = in.read(buffer);
	if (bytesRead != buffer.length) {
	    throw new IOException("EOF before 32-bit int read (read " 
				  + bytesRead + " expected " + buffer.length + ")");
	}
	
	return (buffer[0] & 0xff) 
	    | (buffer[1] & 0xff) << 8 
	    | (buffer[2] & 0xff) << 16 
	    | (buffer[3] & 0xff) << 24;	
    }

    public byte[] readServentIdentifier() 
	throws IOException
    {
	byte serventId[] = new byte[16];
	if (in.read(serventId) != 16) {
	    throw new IOException("EOF while reading servent id");
	}
	
	return serventId;
    }

    public String readIPNumber() 
	throws IOException
    {
	byte buffer[] = new byte[4];
	int bytesRead = in.read(buffer);
	if (bytesRead != buffer.length) {
	    throw new IOException("EOF before whole ipnumber read (read " 
				  + bytesRead + " expected " + buffer.length + ")");
	}
	
	return (buffer[0] < 0 ? buffer[0] + 256 : buffer[0]) 
	    + "." + (buffer[1] < 0 ? buffer[1] + 256 : buffer[1]) 
	    + "." + (buffer[2] < 0 ? buffer[2] + 256 : buffer[2]) 
	    + "." + (buffer[3] < 0 ? buffer[3] + 256 : buffer[3]); 
    }

    /**
     * Read fixed size ascii string 
     */
    public String readAsciiString(int size) throws IOException {
	
	if (size == -1) {
	    return null;
	}
	byte stringBytes[] = new byte[size];
	int bytesRead = in.read(stringBytes);
	if (bytesRead != stringBytes.length) {
	    throw new IOException("EOF before whole string read (read " 
				  + bytesRead + " expected " + stringBytes.length + ")");
	}

	return new String(stringBytes, "ascii");
    }

    /**
     * Read ascii string up until nul termnation
     * (the null terminator is removed from the stream)
     *
     */
    public String readAsciiString() throws IOException {

	StringBuffer buffer = new StringBuffer(256);
	int readChar = in.read();
	while (readChar != 0) {
	    if (readChar ==  -1) {
		throw new IOException("EOF before whole string read (read " 
				      + buffer.length() + " bytes)");
	    }
	    buffer.append((char) readChar);
	    readChar = in.read();
	}

	return buffer.toString();
    }

    /* -- Inputstream methods -- */

    public int available() throws IOException {
	return in.available();
    }

    public void close() throws IOException {
	in.close();
    }

    public void mark(int readlimit) {
	in.mark(readlimit);
    }

    public boolean markSupported() {
	return in.markSupported();
    }

    public int read() throws IOException {
	return in.read();
    }

    public int read(byte[] b) throws IOException {
	return in.read(b);
    }

    public int read(byte[] b, int off, int len) throws IOException {
	return in.read(b, off, len);
    }

    public void reset() throws IOException {
	in.reset();
    }

    public long skip(long n) throws IOException {
	return in.skip(n);
    }
}

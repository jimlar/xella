
package xella.net;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MessageId {

    private static long startTime = System.currentTimeMillis();
    private static long nextMessageId = 1;
    private byte idBytes[];

    /**
     * These do only have to be unique to our host so this will do
     * I think
     */

    private static synchronized byte[] getNewMessageId() {
	
	long messageId = nextMessageId++;
	byte toReturn[] = new byte[16];

	for (int i = 0; i < 8; i++) {
	    toReturn[i] = (byte) ((startTime >> (i * 8)) & 0xff);
	}

	for (int i = 8; i < 16; i++) {
	    toReturn[i] = (byte) ((messageId >> ((i - 8) * 8)) & 0xff);
	}

	return toReturn;
    }

    MessageId() {
	this(getNewMessageId());
    }

    private MessageId(byte idBytes[]) {
	if (idBytes == null || idBytes.length != 16) {
	    throw new IllegalArgumentException("need byte array with lenght 16");
	}
	this.idBytes = idBytes;
    }

    public void writeTo(ByteBuffer buffer) {	
	buffer.put(idBytes);
    }
    
    public static MessageId readFrom(ByteBuffer buffer) 
	throws IOException
    {
	byte idBytes[] = new byte[16];
	buffer.get(idBytes);
	return new MessageId(idBytes);
    }

    public String toString() {

	String toReturn = "MessageId[";
	
	for (int i = 0; i < idBytes.length; i++) {
	    if (i == idBytes.length - 1) {
		toReturn += byteToHex(idBytes[i]) + "]";
	    } else {
		toReturn += byteToHex(idBytes[i]) + ",";
	    }
	}
	return toReturn;
    }

    private String byteToHex(int b) {
	String ret = Integer.toHexString(b & 0xff);
	if (ret.length() < 2) {
	    return "0" + ret; 
	} else {
	    return ret;
	}
    }

    public boolean equals(Object other) {
	if (other == null) {
	    return false;
	}
	if (!other.getClass().equals(this.getClass())) {
	    return false;
	}		
        byte otherIdBytes[] = ((MessageId) other).idBytes;
	return Arrays.equals(this.idBytes, otherIdBytes);
    }

    public int hashCode() {	
	int toReturn = 0;
	for (int i = 0; i < idBytes.length; i += 4) {
	    int value = idBytes[i] 
		+ idBytes[i + 1] << 8
		+ idBytes[i + 2] << 16
		+ idBytes[i + 3] << 24;
	    
	    toReturn ^= value;
	}
	return toReturn;
    }
}


package xella.net;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class PushMessage extends Message {

    private byte serventId[];
    private String hostIP;
    private int port;
    private int fileIndex;

    PushMessage(GnutellaConnection receivedFrom,
		MessageHeader messageHeader, 
		byte[] serventId, 
		String hostIP, 
		int port, 
		int fileIndex) {
	super(receivedFrom, messageHeader);
	this.serventId = serventId;
	this.hostIP = hostIP;
	this.port = port;
	this.fileIndex = fileIndex;
    }
    
    public void writeTo(ByteBuffer buffer) {
	getHeader().writeTo(buffer);
	buffer.put(serventId);
	buffer.put(ByteEncoder.encode32Bit(fileIndex));
	buffer.put(ByteEncoder.encodeIPNumber(hostIP));
	buffer.put(ByteEncoder.encode16Bit(port));
    }

    public static PushMessage readFrom(ByteBuffer buffer,
				       MessageHeader messageHeader, 
				       GnutellaConnection connection) {

	byte serventId[] = new byte[16];
	buffer.get(serventId);
	int fileIndex = ByteDecoder.decode32Bit(buffer);
	String host = ByteDecoder.decodeIPNumber(buffer);
	int port = ByteDecoder.decode32Bit(buffer);
	
	return new PushMessage(connection, messageHeader, serventId, host, port, fileIndex);
    }

    public String toString() {
	return "PushMessage: host=" + hostIP
	    + ", port=" + port
	    + ", fileIndex=" + fileIndex;
    }

    public boolean equals(Object o) {
	if (o == null || !o.getClass().equals(this.getClass())) {
	    return false;
	}
	
	PushMessage other = (PushMessage) o;
	return this.getHeader().equals(other.getHeader())
	    && this.hostIP.equals(other.hostIP)
	    && this.port == other.port
	    && this.fileIndex == other.fileIndex
	    && Arrays.equals(this.serventId, other.serventId);
    }

    public int hashCode() {

	int serventHashCode = 0;
	for (int i = 0; i < serventId.length; i += 4) {
	    int value = serventId[i] 
		+ serventId[i + 1] << 8
		+ serventId[i + 2] << 16
		+ serventId[i + 3] << 24;
	    
	    serventHashCode ^= value;
	}

	return getHeader().hashCode()
	    ^ hostIP.hashCode()
	    ^ port
	    ^ fileIndex
	    ^ serventHashCode;
    }
}

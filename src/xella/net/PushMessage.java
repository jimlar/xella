
package xella.net;

import java.io.*;
import java.nio.ByteBuffer;

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
    
    public ByteBuffer getByteBuffer() {
	ByteBuffer buffer = ByteBuffer.allocate(MessageHeader.SIZE + getHeader().getMessageBodySize());
	buffer.put(getHeader().getByteBuffer());

	buffer.put(serventId);
	buffer.put(ByteEncoder.encode32Bit(fileIndex));
	buffer.put(ByteEncoder.encodeIPNumber(hostIP));
	buffer.put(ByteEncoder.encode16Bit(port));
	return buffer;
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
}

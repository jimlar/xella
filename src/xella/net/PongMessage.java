
package xella.net;

import java.io.*;

public class PongMessage extends Message {
    
    private String host;
    private int port;
    private int numShared;
    private int kilobytesShared;

    PongMessage(GnutellaConnection receivedFrom,
		MessageHeader header, 
		String host, 
		int port, 
		int numShared, 
		int kilobytesShared) 
    {
	super(receivedFrom, header);
	this.host = host;
	this.port = port;
	this.numShared = numShared;
	this.kilobytesShared = kilobytesShared;
    }

    public String getHost() {
	return host;
    }
    
    public int getPort() {
	return port;
    }

    public int getFilesShared() {
	return numShared;
    }

    public int getKilobytesShared() {
	return kilobytesShared;
    }

    public ByteBuffer getByteBuffer() {
	ByteBuffer buffer = ByteBuffer.allocate(MessageHeader.SIZE + MessageHeader.getMessageBodySize());
	buffer.put(getHeader().getByteBuffer());
	buffer.put(ByteEncoder.encode16Bit(port));
	buffer.put(ByteEncoder.encodeIPNumber(host));
	buffer.put(ByteEncoder.encode32Bit(numShared));
	buffer.put(ByteEncoder.encode32Bit(kilobytesShared));
	return buffer;
    }

    public static PongMessage receive(MessageHeader messageHeader, GnutellaConnection connection) 
	throws IOException
    {
	GnutellaInputStream in = connection.getInputStream();
	int port = in.read16Bit();
	String host = in.readIPNumber();
	int numShared = in.read32Bit();
	int kilobytesShared = in.read32Bit();
	return new PongMessage(connection, messageHeader, host, port, numShared, kilobytesShared);
    }

    public String toString() {
	return "PongMessage: host=" + host 
	    + ", port=" + port
	    + ", numShared=" + numShared
	    + ", kilobytesShared=" + kilobytesShared;
    }
}

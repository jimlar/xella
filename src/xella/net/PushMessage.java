
package xella.net;

import java.io.*;

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
    
    public void send(GnutellaOutputStream out) throws IOException {
	getHeader().send(out);
	out.writeServentIdentifier(serventId);
	out.write32Bit(fileIndex);
	out.writeIPNumber(hostIP);
	out.write16Bit(port);
    }

    public static PushMessage receive(MessageHeader messageHeader, GnutellaConnection connection) 
	throws IOException
    {
	GnutellaInputStream in = connection.getInputStream();
	byte serventId[] = in.readServentIdentifier();
	int fileIndex = in.read32Bit();
	String host = in.readIPNumber();
	int port = in.read16Bit();
	
	return new PushMessage(connection, messageHeader, serventId, host, port, fileIndex);
    }

    public String toString() {
	return "PushMessage: host=" + hostIP
	    + ", port=" + port
	    + ", fileIndex=" + fileIndex;
    }
}


package xella.protocol;


public class PushMessage extends Message {

    private byte serventId[];
    private String hostIP;
    private int port;
    private int fileIndex;

    PushMessage(MessageHeader messageHeader, 
		byte[] serventId, 
		String hostIP, 
		int port, 
		int fileIndex) {
	super(messageHeader);
	this.serventId = serventId;
	this.hostIP = hostIP;
	this.port = port;
	this.fileIndex = fileIndex;
    }
    
    public String toString() {
	return "PushMessage: host=" + hostIP
	    + ", port=" + port
	    + ", fileIndex=" + fileIndex
	    + ", header=" + getHeader().toString();
    }
}

package xella.net;


/**
 * This is a class that holds information about a connection
 */

public class ConnectionInfo {

    private String host;
    private int port;
    private boolean isOutgoing;    
    private String statusMessage;
    private int numMessagesSent;
    private int numMessagesReceived;


    ConnectionInfo(String host, 
		   int port, 
		   boolean isOutgoing, 
		   String statusMessage, 
		   int numMessagesReceived, 
		   int numMessagesSent) {
	this.host = host;
	this.port = port;
	this.isOutgoing = isOutgoing;
	this.statusMessage = statusMessage;	
	this.numMessagesReceived = numMessagesReceived;
	this.numMessagesSent = numMessagesSent;
    }

    public String getHost() {
	return this.host;
    }

    public int getPort() {
	return this.port;
    }

    public boolean isOutgoing() {
	return this.isOutgoing;
    }
    
    public String getStatusMessage() {
	return this.statusMessage;
    }

    public int getNumMessagesSent() {
	return this.numMessagesSent;
    }

    public int getNumMessagesReceived() {
	return this.numMessagesReceived;
    }

    void setStatusMessage(String statusMessage) {
	this.statusMessage = statusMessage;
    }
}

package xella.net;


/**
 * This is a class that holds information about a connection
 */

public class ConnectionInfo {

    private int connectionId;
    private String host;
    private int port;
    private boolean isOutgoing;    
    private String statusMessage;
    private int messagesSent;
    private int messagesReceived;
    private int messagesDropped;


    ConnectionInfo(int connectionId,
		   String host, 
		   int port, 
		   boolean isOutgoing, 
		   String statusMessage, 
		   int messagesReceived, 
		   int messagesSent,
		   int messagesDropped) {
	this.connectionId = connectionId;
	this.host = host;
	this.port = port;
	this.isOutgoing = isOutgoing;
	this.statusMessage = statusMessage;	
	this.messagesReceived = messagesReceived;
	this.messagesSent = messagesSent;
	this.messagesDropped = messagesDropped;
    }

    public int getConnectionId() {
	return connectionId;
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

    public int getMessagesSent() {
	return this.messagesSent;
    }

    public int getMessagesReceived() {
	return this.messagesReceived;
    }

    public int getMessagesDropped() {
	return this.messagesDropped;
    }

    void setStatus(String statusMessage, 
		   int messagesSent, 
		   int messagesReceived, 
		   int messagesDropped) {
	this.statusMessage = statusMessage;
	this.messagesSent = messagesSent;
	this.messagesReceived = messagesReceived;
	this.messagesDropped = messagesDropped;	
    }

    public String toString() {
	return "[ConnectionInfo: id=" + connectionId
	    + ", host=" + host + ":" + port
	    + (isOutgoing ? " (outgoing)" : " (incoming)")
	    + ", sent=" + messagesSent
	    + ", recv=" + messagesReceived
	    + ", dropped=" + messagesDropped
	    + ", status=" + statusMessage + "]";
    }
}

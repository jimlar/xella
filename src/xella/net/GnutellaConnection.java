
package xella.net;

import java.io.*;
import java.net.*;
import java.util.*;

class GnutellaConnection {

    private GnutellaEngine engine;

    private String host;
    private int port;
    private Socket socket;
    private boolean isClient;
    private GnutellaOutputStream out;
    private GnutellaInputStream in;

    private MessageDecoder messageDecoder;
    private MessageReader reader;

    private Exception disconnectReason;
    private boolean isClosed;

    private List sendBuffer;
    private MessageSender messageSender;

    /**
     * Connect as a client
     */
    GnutellaConnection(GnutellaEngine engine, String host, int port) 
	throws IOException
    {
	this(engine, host, port, null, true);
    }

    /**
     * Act as a server with the supplied socket
     * (probably retrieved from a ServerSocket.accept())
     */
    
    GnutellaConnection(GnutellaEngine engine, Socket socket) 
	throws IOException
    {
	this(engine, null, -1, socket, false);
    }

    /**
     * If socket param is null then host and port is used to open a client socket
     */

    private GnutellaConnection(GnutellaEngine engine, String host, int port, Socket socket, boolean isClient) 
	throws IOException
    {
	if (isClient == false && socket == null) {
	    throw new IllegalArgumentException("need socket to operate in non client mode");
	}

	this.engine = engine;
	this.host = host;
	this.port = port;
	this.socket = socket;
	this.isClient = isClient;
	this.sendBuffer = Collections.synchronizedList(new ArrayList());
	reader = new MessageReader();
	reader.start();
    }

    /**
     * Queue message for sending 
     */
    void send(Message message) {
	sendBuffer.add(message);
	synchronized (sendBuffer) {
	    sendBuffer.notifyAll();
	}
    }

    boolean isClosed() {
	return isClosed;
    }
    
    private void connect() throws IOException {

	if (this.socket == null) {
	    this.socket = new Socket(host, port);
	}
	this.out = new GnutellaOutputStream(this.socket.getOutputStream());
	this.in = new GnutellaInputStream(this.socket.getInputStream());
	
	if (isClient) {
	    doClientHandshake();
	} else {
	    doServerHandshake();
	}

	this.messageDecoder = new MessageDecoder(this, this.in);

	/* Always start out with a ping */
	send(MessageFactory.getInstance().createPingMessage());
	this.isClosed = false;
	this.messageSender = new MessageSender();
	this.messageSender.start();
    }

    void disconnect() {
	disconnect(null);
    }

    /**
     * close connection with a message
     * (this method ignores exceptions while trying to close)
     */
    void disconnect(Exception disconnectReason) {
	if (!isClosed) {
	    this.disconnectReason = disconnectReason;
	    try {
		this.socket.close();
	    } catch (Exception e) {}
	    engine.getConnectionGroup().removeConnection(this);
	    this.isClosed = true;
	}
    }

    private void receiveNextMessage() throws IOException {
	Message message = messageDecoder.decodeNextMessage();
	engine.registerReceivedMessage(message);
    }

    private void sendNextMessage() throws IOException {
	if (sendBuffer.size() > 0) {
	    ((Message) sendBuffer.remove(0)).send(this.out);
	} else {
	    try {
		synchronized(sendBuffer) {
		    sendBuffer.wait();
		}
	    } catch (InterruptedException e) {}
	}
    }

    private void doClientHandshake() 
	throws IOException
    {
	/* Send connect string */
	this.out.write((GnutellaConstants.CONNECT_MSG + "\n\n").getBytes("ascii"));
	
	/* Read the connect reply string */
	String reply = readAsciiLine();
	readAsciiLine();
	
	if (!reply.equalsIgnoreCase(GnutellaConstants.CONNECT_OK_REPLY)) {
	    throw new IOException("hanshaking error (reply was '" + reply + "')");
	}	
    }

    private void doServerHandshake() 
	throws IOException
    {
	/* read client connect string */
	String connectString = readAsciiLine();
	readAsciiLine();

	if (!connectString.equals(GnutellaConstants.CONNECT_MSG)) {
	    throw new IOException("invalid connect string " + connectString);
	}

	/* Here controls should be made for access and stuff like that */

	/* Send connect reply string */
	this.out.write((GnutellaConstants.CONNECT_OK_REPLY + "\n\n").getBytes("ascii"));
    }

    private String readAsciiLine() 
	throws IOException
    {
	StringBuffer buffer = new StringBuffer();
	int readChar = in.read();
	
	while (readChar != -1 && readChar != '\n') {
	    buffer.append((char) readChar);
	    readChar = in.read();
	}

	return buffer.toString();
    }

    private class MessageReader extends Thread {	
	public void run() {
	    try {
		connect();
	    } catch (IOException e) {
		System.out.println("Error connecting, closing connection");
		disconnect(e);
	    }

	    try {
		while (!isClosed()) {
		    receiveNextMessage();
		}
	    } catch (IOException e) {
		System.out.println("Error reading next message, closing connection");
		disconnect(e);
	    }
	}
    }

    private class MessageSender extends Thread {	
	public void run() {
	    try {
		while (!isClosed()) {
		    sendNextMessage();
		}
	    } catch (IOException e) {
		System.out.println("Error sending, closing connection");
		disconnect(e);
	    }
	}
    }
}

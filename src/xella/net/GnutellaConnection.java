
package xella.net;

import java.io.*;
import java.net.*;
import java.util.*;

class GnutellaConnection {

    private static final int CONNECT_TIMEOUT = 1000 * 30;
    private static final int SENDBUFFER_CLOSE_LEVEL = 200;
    private static final int MESSAGESIZE_CLOSE_LEVEL = 65535;

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

    private int numMessagesReceived = 0;
    private int numMessagesSent = 0;

    /**
     * Connect as a client
     */
    GnutellaConnection(GnutellaEngine engine, String host, int port) {
	this(engine, host, port, null, true);
    }

    /**
     * Act as a server with the supplied socket
     * (probably retrieved from a ServerSocket.accept())
     */
    
    GnutellaConnection(GnutellaEngine engine, Socket socket) {
	this(engine, socket.getInetAddress().getHostAddress(), socket.getPort(), socket, false);
    }

    /**
     * If socket param is null then host and port is used to open a client socket
     */

    private GnutellaConnection(GnutellaEngine engine, String host, int port, Socket socket, boolean isClient) {
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

	synchronized (sendBuffer) {
	    if (sendBuffer.size() >= SENDBUFFER_CLOSE_LEVEL) {
		disconnect(new IOException("send buffer overflow"));
		engine.disconnected(new ConnectionInfo(host,
						       port,
						       isClient,
						       "Send buffer overflow", 
						       numMessagesReceived,
						       numMessagesSent));
		return;
	    }
	    sendBuffer.add(message);
	    sendBuffer.notifyAll();
	}
    }

    boolean isClosed() {
	return isClosed;
    }
    
    GnutellaInputStream getInputStream() {
	return this.in;
    }

    private void connect() throws IOException {

	engine.connecting(new ConnectionInfo(host, 
					     port, 
					     isClient,
					     "Connecting",
					     numMessagesReceived,
					     numMessagesSent));	

	if (this.socket == null) {
	    this.socket = new Socket();
	    this.socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT);
	}
	this.out = new GnutellaOutputStream(this.socket.getOutputStream());
	this.in = new GnutellaInputStream(this.socket.getInputStream());
	
	if (isClient) {
	    doClientHandshake();
	} else {
	    doServerHandshake();
	}

	this.messageDecoder = new MessageDecoder(this);
	this.isClosed = false;
	this.messageSender = new MessageSender();
	this.messageSender.start();

	/* Start out with a ping if we are client */
	if (isClient) {
	    send(MessageFactory.getInstance().createPingMessage());
	}
    }

    /**
     * close connection with a message
     * (this method ignores exceptions while trying to close)
     */
    private synchronized void disconnect(Exception disconnectReason) {
	if (!isClosed) {
	    this.isClosed = true;
	    this.disconnectReason = disconnectReason;
	    try {
		this.socket.close();
	    } catch (Exception e) {}
	    engine.getConnectionGroup().removeConnection(this);
	    this.isClosed = true;
	}
    }

    private void receiveNextMessage() throws IOException {
	MessageHeader header = messageDecoder.decodeNextMessageHeader();

	if (header.getMessageBodySize() > MESSAGESIZE_CLOSE_LEVEL) {
	    throw new IOException("message too big (size = " + header.getMessageBodySize());
	}
	Message message = messageDecoder.decodeNextMessage(header);
	numMessagesReceived++;
	engine.registerReceivedMessage(message);
    }

    private void sendNextMessage() throws IOException {
	Message message = null;

	synchronized (sendBuffer) {
	    if (sendBuffer.size() > 0) {
		message = (Message) sendBuffer.remove(0);
	    } 
	}

	if (message != null) {
	    message.send(this.out);
	    numMessagesSent++;
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
		engine.connected(new ConnectionInfo(host, 
						    port, 
						    isClient,
						    "Connected",
						    numMessagesReceived,
						    numMessagesSent));
	    } catch (IOException e) {
		disconnect(e);
		engine.connectFailed(new ConnectionInfo(host, 
							port,
							isClient,
							e.getMessage(),
							numMessagesReceived,
							numMessagesSent));
	    }

	    try {
		while (!isClosed()) {
		    receiveNextMessage();
		}
	    } catch (IOException e) {
		if (!isClosed()) {
		    disconnect(e);
		    engine.disconnected(new ConnectionInfo(host, 
							   port, 
							   isClient,
							   e.getMessage(),
							   numMessagesReceived,
							   numMessagesSent));
		}
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
		if (!isClosed()) {
		    disconnect(e);
		    engine.disconnected(new ConnectionInfo(host, 
							   port,
							   isClient,
							   e.getMessage(),
							   numMessagesReceived,
							   numMessagesSent));
		}
	    }
	}
    }
}


package xella.net;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;

class GnutellaConnection {

    private static final int CONNECT_TIMEOUT = 1000 * 30;
    private static final int SENDBUFFER_CLOSE_LEVEL = 200;
    private static final int MESSAGESIZE_CLOSE_LEVEL = 65535;

    private static final int STATE_NEW = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_HANDSHAKE_STEP1 = 2;
    private static final int STATE_HANDSHAKE_STEP2 = 3;
    private static final int STATE_CONNECTED_RECEIVING_HEADER = 4;
    private static final int STATE_CONNECTED_RECEIVING_BODY = 5;
    private static final int STATE_CLOSED = 6;

    private GnutellaEngine engine;

    private String host;
    private int port;
    private SocketChannel socketChannel;
    private boolean isClient;

    private MessageDecoder messageDecoder;
    private MessageReader reader;

    private Exception disconnectReason;
    private boolean isClosed;

    private List sendBuffer;
    private MessageSender messageSender;

    private int numMessagesReceived = 0;
    private int numMessagesSent = 0;

    private int connectionState = STATE_NEW;

    private ByteBuffer currentInputBuffer;

    /**
     * Connect as a client
     */
    GnutellaConnection(GnutellaEngine engine, String host, int port) {
	this(engine, host, port, null, true);
    }

    /**
     * Act as a server with the supplied socketChannel
     * (probably retrieved from a ServerSocketChannel.accept())
     */
    
    GnutellaConnection(GnutellaEngine engine, SocketChannel socketChannel) {
	this(engine, 
	     socketChannel.socket().getInetAddress().getHostAddress(), 
	     socketChannel.socket().getPort(), 
	     socket, 
	     false);
    }

    /**
     * If socket param is null then host and port is used to open a client socket
     */

    private GnutellaConnection(GnutellaEngine engine, String host, int port, SocketChannel socketChannel, boolean isClient) {
	if (isClient == false && socket == null) {
	    throw new IllegalArgumentException("need socketChannel to operate in non client mode");
	}

	this.engine = engine;
	this.host = host;
	this.port = port;
	this.socketChannel = socketChannel;
	this.isClient = isClient;
	this.sendBuffer = Collections.synchronizedList(new ArrayList());

	//reader = new MessageReader();
	//reader.start();
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
	    
	    sendBuffer.add(message.getByteBuffer());
	}
    }

    boolean isClosed() {
	return isClosed;
    }

    boolean isConnected() {
	return connectionState == STATE_CONNECTED_RECEIVING_HEADER;
    }

    /**
     * Run the connection 
     * has to be called periodically to initiate the connections
     * asychronous tasks
     */
    void pumpConnection() throws IOException {
	
	switch (connectionState) {

	case STATE_NEW:
	    startConnect();
	    break;

	case STATE_CONNECTING:
	    finishConnect();
	    break;

	case STATE_HANDSHAKE_STEP1:
	    startHandshake();
	    break;

	case STATE_HANDSHAKE_STEP2:
	    finishHandshake();
	    break;

	case STATE_CONNECTED:
	    /* check for sent/received messages */
	    /* Initiate delivery of next */
	    break;

	case STATE_CLOSED:
	    break;
	}
    }

    
    private void startConnect() throws IOException {

	engine.connecting(new ConnectionInfo(host, 
					     port, 
					     isClient,
					     "Connecting",
					     numMessagesReceived,
					     numMessagesSent));	

	if (this.socketChannel == null) {
	    this.socketChannel = SocketChannel.open();
	    this.socketChannel.socket().setSoTimeout(CONNECT_TIMEOUT);
	    this.socketChannel.connect(new InetSocketAddress(host, port));
	}

	connectionState = STATE_CONNECTING;
    }

    private void finishConnect() throws IOException {
	boolean done = this.socketChannel.finishConnect();
	if (done) {
	    connectionState = STATE_HANSHAKE1;
	    currentByteBuffer = null;
	}
    }

    private void startHandshake() throws IOException {
	
	boolean result;
	if (isClient) {
	    result = startClientHandshake();
	} else {
	    result = startServerHandshake();
	}
	
	if (result) {
	    connectionState = STATE_HANDSHAKE_STEP2;
	}
    }
    
    private void finishHandshake() throws IOException {

	boolean result;
	if (isClient) {
	    result = finishClientHandshake();
	} else {
	    result = finishServerHandshake();
	}

	if (result) {
	    this.messageDecoder = new MessageDecoder(this);
	    this.isClosed = false;
	    this.messageSender = new MessageSender();
	    this.messageSender.start();
	    
	    /* Start out with a ping if we are client */
	    if (isClient) {
		send(MessageFactory.getInstance().createPingMessage());
	    }

	    connectionState = STATE_CONNECTED_RECEIVING_HEADER;
	}
    }

    private boolean startClientHandshake() 
	throws IOException
    {
	/* Send connect string and initiate readback */
	ByteBuffer buffer = ByteBuffer.allocate(GnutellaConstants.CONNECT_MSG.length() + 2);
	buffer.put(ByteEncoder.encodeAsciiString(GnutellaConstants.CONNECT_MSG + "\n\n"));
	socketChannel.write(buffer);
	currentInputBuffer = ByteBuffer.allocate(GnutellaConstants.CONNECT_OK_REPLY + 2);
	socketChannel.read(currentInputBuffer);
	return true;
    }
    
    private boolean finishClientHandshake() 
	throws IOException
    {
	/* Has the whole reply arrived? */
	if (!currentInputBuffer.hasRemaining()) {
	    
	    byte strBytes[] = new byte[currentInputBuffer.capacity()];
	    String reply = ByteDecoder.decodeAsciiString(strBytes);
	    
	    if (!reply.equalsIgnoreCase(GnutellaConstants.CONNECT_OK_REPLY)) {
		throw new IOException("hanshaking error (reply was '" + reply + "')");
	    }
	    return true;
	}
	return false;
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


    /**
     * close connection with a message
     * (this method ignores exceptions while trying to close)
     */
    private synchronized void disconnect(Exception disconnectReason) {
	if (!isClosed) {
	    this.isClosed = true;
	    this.disconnectReason = disconnectReason;
	    try {
		this.socketChannel.close();
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



//     private class MessageReader extends Thread {	
// 	public void run() {
// 	    try {
// 		connect();
// 		engine.connected(new ConnectionInfo(host, 
// 						    port, 
// 						    isClient,
// 						    "Connected",
// 						    numMessagesReceived,
// 						    numMessagesSent));
// 	    } catch (IOException e) {
// 		disconnect(e);
// 		engine.connectFailed(new ConnectionInfo(host, 
// 							port,
// 							isClient,
// 							e.getMessage(),
// 							numMessagesReceived,
// 							numMessagesSent));
// 	    }

// 	    try {
// 		while (!isClosed()) {
// 		    receiveNextMessage();
// 		}
// 	    } catch (IOException e) {
// 		if (!isClosed()) {
// 		    disconnect(e);
// 		    engine.disconnected(new ConnectionInfo(host, 
// 							   port, 
// 							   isClient,
// 							   e.getMessage(),
// 							   numMessagesReceived,
// 							   numMessagesSent));
// 		}
// 	    }
// 	}
//     }

//     private class MessageSender extends Thread {	
// 	public void run() {
// 	    try {
// 		while (!isClosed()) {
// 		    sendNextMessage();
// 		}
// 	    } catch (IOException e) {
// 		if (!isClosed()) {
// 		    disconnect(e);
// 		    engine.disconnected(new ConnectionInfo(host, 
// 							   port,
// 							   isClient,
// 							   e.getMessage(),
// 							   numMessagesReceived,
// 							   numMessagesSent));
// 		}
// 	    }
// 	}
//     }
}

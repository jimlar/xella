
package xella.net;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;

class GnutellaConnection {

    public static final String CONNECT_MSG = "GNUTELLA CONNECT/0.4";
    public static final String CONNECT_OK_REPLY = "GNUTELLA OK";
    
    private static final int TIMEOUT_MS = 1000 * 30;
    private static final int SENDBUFFER_CLOSE_LEVEL = 200;
    private static final int MAX_MESSAGESIZE = 65535;

    private static final int STATE_NEW = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_HANDSHAKE_STEP1 = 2;
    private static final int STATE_HANDSHAKE_STEP2 = 3;
    private static final int STATE_CONNECTED_RECEIVING_HEADER = 4;
    private static final int STATE_CONNECTED_RECEIVING_BODY = 5;
    private static final int STATE_CLOSED = 6;

    private static int nextConnectionNumber = 1;

    private GnutellaEngine engine;

    private String host;
    private int port;
    private SocketChannel socketChannel;
    private boolean isClient;
    private int connectionNumber;

    private MessageDecoder messageDecoder;

    private Exception disconnectReason;

    private List sendQueue;

    private int numMessagesReceived = 0;
    private int numMessagesSent = 0;
    private int numMessagesDropped = 0;

    private int connectionState = STATE_NEW;

    private ByteBuffer inputBuffer;
    private ByteBuffer outputBuffer;
    private MessageHeader currentMessageHeader;

    private ConnectionInfo connectionInfo;


    private synchronized static int getNextConnectionNumber() {
	return nextConnectionNumber++;
    }

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
	     socketChannel, 
	     false);
    }

    /**
     * If socket param is null then host and port is used to open a client socket
     */

    private GnutellaConnection(GnutellaEngine engine, 
			       String host, 
			       int port, 
			       SocketChannel socketChannel, 
			       boolean isClient) {
	this.connectionNumber = getNextConnectionNumber();
	this.engine = engine;
	this.host = host;
	this.port = port;
	this.socketChannel = socketChannel;
	this.isClient = isClient;
	this.sendQueue = Collections.synchronizedList(new ArrayList());
	this.outputBuffer = ByteBuffer.allocateDirect(MAX_MESSAGESIZE);
	this.outputBuffer.limit(0);
	this.inputBuffer = ByteBuffer.allocateDirect(MAX_MESSAGESIZE);
	this.inputBuffer.limit(0);

	this.connectionInfo = new ConnectionInfo(connectionNumber,
						 host, 
						 port, 
						 isClient,
						 "New",
						 numMessagesReceived,
						 numMessagesSent,
						 numMessagesDropped);	
	
	pumpConnection();
    }

    /**
     * Queue message for sending 
     */
    synchronized void send(Message message) {

	synchronized (sendQueue) {
	    if (sendQueue.size() >= SENDBUFFER_CLOSE_LEVEL) {
		disconnect(new IOException("send buffer overflow"));
		connectionInfo.setStatus("Send buffer overflow", 
					 numMessagesReceived,
					 numMessagesSent,
					 numMessagesDropped);

		engine.disconnected(connectionInfo);
		return;
	    }
	    
	    sendQueue.add(message);
	}
    }

    boolean isClosed() {
	return connectionState == STATE_CLOSED;
    }

    boolean isConnected() {
	return connectionState == STATE_CONNECTED_RECEIVING_HEADER
	    || connectionState == STATE_CONNECTED_RECEIVING_BODY;
    }

    SocketChannel getChannel() {
	return socketChannel;
    }

    synchronized void increaseDroppedMessages() {
	numMessagesDropped++;
	sendStatusChange();
    }

    /**
     * Run the connection 
     * has to be called periodically to initiate the connections
     * asychronous tasks
     */
    synchronized void pumpConnection() {
	
	log("start pump, state=" + connectionState);

	try {
	    doReadWrite();

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
		if (isConnected()) {
		    connectionInfo.setStatus("Connected",
					     numMessagesReceived,
					     numMessagesSent,
					     numMessagesDropped);
		    engine.connected(connectionInfo);
		}
		break;
		
	    case STATE_CONNECTED_RECEIVING_HEADER:
		receiveNextHeader();
		sendNextMessage();
		break;
		
	    case STATE_CONNECTED_RECEIVING_BODY:
		receiveNextMessage();
		sendNextMessage();
		break;
		
	    case STATE_CLOSED:
		log("closed");
		break;
	    }

	    doReadWrite();

	} catch (Exception e) {
	    int state = connectionState;
	    disconnect(e);

	    switch (state) {

	    case STATE_NEW:
	    case STATE_CONNECTING:
	    case STATE_HANDSHAKE_STEP1:
	    case STATE_HANDSHAKE_STEP2:
		connectionInfo.setStatus(e.getMessage(),
					 numMessagesReceived,
					 numMessagesSent,
					 numMessagesDropped);
		engine.connectFailed(connectionInfo);
		break;

	    default:
		connectionInfo.setStatus(e.getMessage(),
					 numMessagesReceived,
					 numMessagesSent,
					 numMessagesDropped);
		engine.disconnected(connectionInfo);
		break;
	    }
	}

	log("end pump!");
    }

    private void doReadWrite() throws IOException {
	
	/* Write if there are bytes to write */
	if (outputBuffer.hasRemaining()) {
	    socketChannel.write(outputBuffer);
	} 
	
	/* Read if there are bytes to read */
	if (inputBuffer.hasRemaining()) {
	    socketChannel.read(inputBuffer);
	} 
    }

    private void startConnect() throws IOException {

	/* Connect us if we are a client connection */
	if (isClient) {
	    connectionInfo.setStatus("Connecting",
				     numMessagesReceived,
				     numMessagesSent,
				     numMessagesDropped);	
	    engine.connecting(connectionInfo);	    
	    this.socketChannel = SocketChannel.open();
	    this.socketChannel.configureBlocking(false);
	    this.socketChannel.connect(new InetSocketAddress(host, port));
	
	} else {
	    this.socketChannel.configureBlocking(false);
	}
	
	this.socketChannel.socket().setSoTimeout(TIMEOUT_MS);
	connectionState = STATE_CONNECTING;
    }

    private void finishConnect() throws IOException {
	this.socketChannel.finishConnect();

	if (this.socketChannel.isConnected()) {
	    connectionState = STATE_HANDSHAKE_STEP1;
	    sendStatusChange("Handshaking...");
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
	    
	    /* Start out with a ping if we are client */
	    if (isClient) {
		send(MessageFactory.getInstance().createPingMessage());
	    }

	    /* Setup buffer for reading next header */
	    inputBuffer.limit(MessageHeader.SIZE);
	    inputBuffer.rewind();
	    connectionState = STATE_CONNECTED_RECEIVING_HEADER;
	}
    }

    private boolean startClientHandshake() 
	throws IOException
    {
	/* Send connect string and initiate readback */
	outputBuffer.limit(CONNECT_MSG.length() + 2);
	outputBuffer.rewind();
	outputBuffer.put(ByteEncoder.encodeAsciiString(CONNECT_MSG + "\n\n"));
	outputBuffer.rewind();
	socketChannel.write(outputBuffer);

	inputBuffer.limit(CONNECT_OK_REPLY.length() + 2);
	inputBuffer.rewind();
	socketChannel.read(inputBuffer);

	return true;
    }
    
    private boolean finishClientHandshake() 
	throws IOException
    {
	/* Has the whole reply arrived? */
	if (!inputBuffer.hasRemaining()) {
	    
	    inputBuffer.rewind();
	    String reply = ByteDecoder.decodeAsciiString(inputBuffer, 
							 inputBuffer.limit());
	    
	    if (!reply.equalsIgnoreCase(CONNECT_OK_REPLY + "\n\n")) {
		throw new IOException("hanshaking error (reply was '" + reply + "')");
	    }
	    inputBuffer.limit(0);
	    return true;
	}
	return false;
    }
    
    private boolean startServerHandshake() 
	throws IOException
    {
	String expectedString = CONNECT_MSG + "\n\n";

	/* initiate read request */
	inputBuffer.limit(expectedString.length());
	inputBuffer.rewind();
	socketChannel.read(inputBuffer);

	return true;
    }

    private boolean finishServerHandshake() 
	throws IOException
    {
	/* Has the whole reply arrived? */
	if (!inputBuffer.hasRemaining()) {
	    
	    inputBuffer.rewind();
	    byte strBytes[] = new byte[inputBuffer.limit()];
	    inputBuffer.get(strBytes);

	    String message = ByteDecoder.decodeAsciiString(strBytes);
	    
	    String expectedString = CONNECT_MSG + "\n\n";
	    if (!message.equalsIgnoreCase(expectedString)) {
		throw new IOException("hanshaking error (connect message was '" + message + "')");
	    }

	    inputBuffer.limit(0);
	    String toSend = CONNECT_OK_REPLY + "\n\n";
	
	    /* Send connect reply string */
	    outputBuffer.limit(toSend.length());
	    outputBuffer.rewind();
	    outputBuffer.put(ByteEncoder.encodeAsciiString(toSend));
	    outputBuffer.rewind();
	    socketChannel.write(outputBuffer);
	    return true;
	}
	return false;
    }

    /**
     * close connection with a message
     * (this method ignores exceptions while trying to close)
     */
    synchronized void disconnect(Exception disconnectReason) {
	if (!isClosed()) {
	    inputBuffer.limit(0);
	    outputBuffer.limit(0);
	    this.disconnectReason = disconnectReason;
	    try {
		this.socketChannel.close();
	    } catch (Exception e) {}
	    engine.getConnectionGroup().removeConnection(this);
	    this.connectionState = STATE_CLOSED;
	    log("disconnected: " + disconnectReason);
	}
    }


    private void receiveNextHeader() throws IOException {

	if (!inputBuffer.hasRemaining()) {
	    inputBuffer.rewind();
	    currentMessageHeader = MessageHeader.readFrom(inputBuffer);
	    
	    /* Setup buffer for reading body */
	    if (currentMessageHeader.getMessageBodySize() > MAX_MESSAGESIZE) {
		throw new IOException("message too big (size = " 
				      + currentMessageHeader.getMessageBodySize());
	    }
	    
	    inputBuffer.limit(currentMessageHeader.getMessageBodySize());
	    inputBuffer.rewind();
	    
	    connectionState = STATE_CONNECTED_RECEIVING_BODY;
	}
    }

    private void receiveNextMessage() throws IOException {

	if (!inputBuffer.hasRemaining()) {
	    inputBuffer.rewind();
	    
	    Message message = messageDecoder.decodeMessage(currentMessageHeader, inputBuffer);
	    numMessagesReceived++;
	    engine.registerReceivedMessage(message);
	    log("Got " + message);
	    
	    /* Setup buffer for reading next header */
	    inputBuffer.limit(MessageHeader.SIZE);
	    inputBuffer.rewind();
	    connectionState = STATE_CONNECTED_RECEIVING_HEADER;		
	    sendStatusChange();
	}
    }

    /**
     * Fill outputBuffer with the next message if it's time
     *
     */
    private synchronized void sendNextMessage() throws IOException {

	/* Have everything been sent? */
	if (!outputBuffer.hasRemaining()) {
	    if (sendQueue.size() > 0) {
		Message message = (Message) sendQueue.remove(0);
		if (message.size() > MAX_MESSAGESIZE) {
		    throw new IOException("tried to send to large message");
		}
		log("sending message: " + message);
		
		outputBuffer.limit(message.size());
		outputBuffer.rewind();
		message.writeTo(outputBuffer);
		outputBuffer.rewind();
		numMessagesSent++;
		
		/* We might aswell do a write right now */
		socketChannel.write(outputBuffer);

		sendStatusChange();
	    }
	} 
    }

    private void sendStatusChange(String status) {
	connectionInfo.setStatus(status,
				 numMessagesReceived,
				 numMessagesSent,
				 numMessagesDropped);	
	engine.statusChange(connectionInfo);
    }

    private void sendStatusChange() {
	sendStatusChange(connectionInfo.getStatusMessage());
    }

    private void log(String message) {
	System.out.println("[" + connectionNumber + "]: " + message);
    }
}

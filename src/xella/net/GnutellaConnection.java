
package xella.net;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;

class GnutellaConnection {

    private static final int TIMEOUT = 1000 * 30;
    private static final int SENDBUFFER_CLOSE_LEVEL = 200;
    private static final int MESSAGESIZE_CLOSE_LEVEL = 65535;

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

    private List sendBuffer;

    private int numMessagesReceived = 0;
    private int numMessagesSent = 0;

    private int connectionState = STATE_NEW;

    private ByteBuffer currentInputBuffer;
    private ByteBuffer currentOutputBuffer;
    private MessageHeader currentMessageHeader;


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
	this.sendBuffer = Collections.synchronizedList(new ArrayList());

	pumpConnection();
    }

    /**
     * Queue message for sending 
     */
    synchronized void send(Message message) {

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

    /**
     * Run the connection 
     * has to be called periodically to initiate the connections
     * asychronous tasks
     */
    synchronized void pumpConnection() {
	
	try {
	    switch (connectionState) {
		
	    case STATE_NEW:
		try {
		    startConnect();
		} catch (IOException e) {
		    disconnect(e);
		    engine.connectFailed(new ConnectionInfo(host, 
							    port,
							    isClient,
							    e.getMessage(),
							    numMessagesReceived,
							    numMessagesSent));
		}
		break;
		
	    case STATE_CONNECTING:
		try {
		    finishConnect();
		} catch (IOException e) {
		    disconnect(e);
		    engine.connectFailed(new ConnectionInfo(host, 
							    port,
							    isClient,
							    e.getMessage(),
							    numMessagesReceived,
							    numMessagesSent));
		}
		break;
		
	    case STATE_HANDSHAKE_STEP1:
		try {
		    startHandshake();
		} catch (IOException e) {
		    disconnect(e);
		    engine.connectFailed(new ConnectionInfo(host, 
							    port,
							    isClient,
							    e.getMessage(),
							    numMessagesReceived,
							    numMessagesSent));
		}
		break;
		
	    case STATE_HANDSHAKE_STEP2:
		try {
		    finishHandshake();
		    if (isConnected()) {
			engine.connected(new ConnectionInfo(host, 
							    port, 
							    isClient,
							    "Connected",
							    numMessagesReceived,
							    numMessagesSent));
		    }
		} catch (IOException e) {
		    disconnect(e);
		    engine.connectFailed(new ConnectionInfo(host, 
							    port,
							    isClient,
							    e.getMessage(),
							    numMessagesReceived,
							    numMessagesSent));
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
	} catch (Exception e) {
	    e.printStackTrace();
	    disconnect(e);
	    engine.disconnected(new ConnectionInfo(host, 
						   port, 
						   isClient,
						   e.getMessage(),
						   numMessagesReceived,
						   numMessagesSent));
	}
    }

    
    private void startConnect() throws IOException {

	/* Connect us if we are a client connection */
	if (isClient) {

	    log("connecting to " + host + ":" + port);
	    engine.connecting(new ConnectionInfo(host, 
						 port, 
						 isClient,
						 "Connecting",
						 numMessagesReceived,
						 numMessagesSent));	
	    
	    this.socketChannel = SocketChannel.open();
	    this.socketChannel.connect(new InetSocketAddress(host, port));
	}

	this.socketChannel.configureBlocking(false);
	this.socketChannel.socket().setSoTimeout(TIMEOUT);
	connectionState = STATE_CONNECTING;
    }

    private void finishConnect() throws IOException {
	this.socketChannel.finishConnect();

	if (this.socketChannel.isConnected()) {
	    connectionState = STATE_HANDSHAKE_STEP1;
	    currentInputBuffer = null;
	    currentOutputBuffer = null;
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

	    connectionState = STATE_CONNECTED_RECEIVING_HEADER;
	}
    }

    private boolean startClientHandshake() 
	throws IOException
    {
	/* Send connect string and initiate readback */
	ByteBuffer buffer = ByteBuffer.allocate(GnutellaConstants.CONNECT_MSG.length() + 2);
	buffer.put(ByteEncoder.encodeAsciiString(GnutellaConstants.CONNECT_MSG + "\n\n")).rewind();
	socketChannel.write(buffer);

	log("handshake sent, reading response...");

	currentInputBuffer = ByteBuffer.allocate(GnutellaConstants.CONNECT_OK_REPLY.length() + 2);
	socketChannel.read(currentInputBuffer);
	log("response read request sent");
	return true;
    }
    
    private boolean finishClientHandshake() 
	throws IOException
    {
	/* Has the whole reply arrived? */
	if (currentInputBuffer.hasRemaining()) {
	    socketChannel.read(currentInputBuffer);
	} else {
	    
	    currentInputBuffer.rewind();
	    String reply = ByteDecoder.decodeAsciiString(currentInputBuffer, 
							 currentInputBuffer.capacity());
	    
	    this.currentInputBuffer = null;
	    if (!reply.equalsIgnoreCase(GnutellaConstants.CONNECT_OK_REPLY + "\n\n")) {
		throw new IOException("hanshaking error (reply was '" + reply + "')");
	    }
	    log("handshake ok!");
	    return true;
	}
	return false;
    }
    
    /**
     * currentInputBuffer has to be null before calling this the first time
     */

    private boolean startServerHandshake() 
	throws IOException
    {
	String expectedString = GnutellaConstants.CONNECT_MSG + "\n\n";

	/* initiate read request */
	if (currentInputBuffer == null) {
	    currentInputBuffer = ByteBuffer.allocate(expectedString.length());
	}

	/* Has the whole reply arrived? */
	if (currentInputBuffer.hasRemaining()) {
	    socketChannel.read(currentInputBuffer);

	} else {
	    
	    currentInputBuffer.rewind();
	    byte strBytes[] = new byte[currentInputBuffer.capacity()];
	    String message = ByteDecoder.decodeAsciiString(strBytes);
	    this.currentInputBuffer = null;
	    
	    if (!message.equalsIgnoreCase(expectedString)) {
		throw new IOException("hanshaking error (connect message was '" + message + "')");
	    }
	    return true;
	}
	return false;
    }

    private boolean finishServerHandshake() 
	throws IOException
    {
	String toSend = GnutellaConstants.CONNECT_OK_REPLY + "\n\n";
	
	/* Send connect reply string */
	ByteBuffer buffer = ByteBuffer.allocate(toSend.length());
	buffer.put(ByteEncoder.encodeAsciiString(toSend)).rewind();
	socketChannel.write(buffer);
	return true;
    }

    /**
     * close connection with a message
     * (this method ignores exceptions while trying to close)
     */
    synchronized void disconnect(Exception disconnectReason) {
	if (!isClosed()) {
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

	if (currentInputBuffer == null) {
	    currentInputBuffer = ByteBuffer.allocate(MessageHeader.SIZE);
	    log("initiated header read");
	} else {

	    if (currentInputBuffer.hasRemaining()) {
		socketChannel.read(currentInputBuffer);
	    } else {
		currentInputBuffer.rewind();
		log("decoding header " + currentInputBuffer.remaining() + " bytes");
		currentMessageHeader = MessageHeader.readFrom(currentInputBuffer);
		currentInputBuffer = null;
		connectionState = STATE_CONNECTED_RECEIVING_BODY;		
	    }
	}
    }

    private void receiveNextMessage() throws IOException {

	if (currentInputBuffer == null) {
	    if (currentMessageHeader.getMessageBodySize() > MESSAGESIZE_CLOSE_LEVEL) {
		throw new IOException("message too big (size = " 
				      + currentMessageHeader.getMessageBodySize());
	    }
	    currentInputBuffer = ByteBuffer.allocate(currentMessageHeader.getMessageBodySize());
	    log("initiated message body read");

	} else {

	    if (currentInputBuffer.hasRemaining()) {
		socketChannel.read(currentInputBuffer);
	    } else {
		currentInputBuffer.rewind();

		log("decoding message body " + currentInputBuffer.remaining() + " bytes");
		Message message = messageDecoder.decodeMessage(currentMessageHeader, currentInputBuffer);
		numMessagesReceived++;
		currentInputBuffer = null;
		connectionState = STATE_CONNECTED_RECEIVING_HEADER;		
		engine.registerReceivedMessage(message);
		log("message body done");
	    }
	}
    }

    private synchronized void sendNextMessage() throws IOException {

	if (currentOutputBuffer != null && currentOutputBuffer.hasRemaining()) {
	    socketChannel.write(currentOutputBuffer);	    
	    
	} else {
	    
	    if (sendBuffer.size() > 0) {
		Message message = (Message) sendBuffer.remove(0);
		currentOutputBuffer = message.getByteBuffer();
		currentOutputBuffer.rewind();
		socketChannel.write(currentOutputBuffer);
		numMessagesSent++;
		log("sent message (size=" + message.size() + ")");
	    }
	} 
    }

    private void log(String message) {
	System.out.println("[" + connectionNumber + "]: " + message);
    }
}

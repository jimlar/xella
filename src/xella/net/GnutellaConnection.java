
package xella.net;

import java.io.*;
import java.net.*;

public class GnutellaConnection {

    private GnutellaEngine engine;
    private Socket socket;
    private GnutellaOutputStream out;
    private GnutellaInputStream in;

    private MessageDecoder messageDecoder;
    private MessageReader reader;

    /**
     * Connect as a client
     */
    public GnutellaConnection(GnutellaEngine engine, String host, int port) 
	throws IOException
    {
	this(engine, new Socket(host, port), true);
    }

    /**
     * @param isClient if true we are assumed to be the client side 
     *                 (ie. sending the connect string, otherwise we are receiveing the 
     *                      connect string)
     *
     */
    
    public GnutellaConnection(GnutellaEngine engine, Socket socket, boolean isClient) 
	throws IOException 
    {
	this.engine = engine;
	this.socket = socket;
	this.out = new GnutellaOutputStream(socket.getOutputStream());
	this.in = new GnutellaInputStream(socket.getInputStream());
	
	if (isClient) {
	    doClientHandshake();
	} else {
	    doServerHandshake();
	}

	this.messageDecoder = new MessageDecoder(this);
	engine.getRouter().addConnection(this);

	reader = new MessageReader();
	reader.start();

	/* Always start out with a ping */
	send(MessageFactory.getInstance().createPingMessage());
    }

    void send(Message message) throws IOException {
	message.send(out);
    }

    GnutellaInputStream getInputStream() {
	return this.in;
    }

    private void receiveNextMessage() throws IOException {
	Message message = messageDecoder.decodeNextMessage();
	engine.registerReceivedMessage(message);
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
		while (true) {
		    receiveNextMessage();
		}
	    } catch (IOException e) {
		System.out.println("Error reading next message, should close connection");
		e.printStackTrace();
	    }
	}
    }
}

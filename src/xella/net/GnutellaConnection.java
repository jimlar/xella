
package xella.net;

import java.io.*;
import java.net.*;

public class GnutellaConnection {

    private Socket socket;
    private OutputStream out;
    private InputStream in;

    private MessageGenerator messageGenerator;
    private MessageDecoder messageDecoder;

    public GnutellaConnection(String host, int port) 
	throws IOException
    {
	this(new Socket(host, port), true);
    }

    /**
     * @param isClient if true we are assumed to be the client side 
     *                 (ie. sending the connect string, otherwise we are receiveing the 
     *                      connect string)
     *
     */
    
    public GnutellaConnection(Socket socket, boolean isClient) 
	throws IOException 
    {
	this.socket = socket;
	this.out = socket.getOutputStream();
	this.in = socket.getInputStream();
	
	if (isClient) {
	    doClientHandshake();
	} else {
	    doServerHandshake();
	}

	this.messageGenerator = new MessageGenerator(this.out);
	this.messageDecoder = new MessageDecoder(this.in);
    }

    public void sendPing() throws IOException {
	messageGenerator.sendPing();
    }

    public void sendPong(PingMessage replyTo,
			 String hostIP, 
			 int port, 
			 int numShared, 
			 int kilobytesShared) 
	throws IOException 
    {
	messageGenerator.sendPong(replyTo,
				  hostIP, 
				  port, 
				  numShared, 
				  kilobytesShared);
    }
    
    public void sendQuery(int minSpeed, String searchString)
	throws IOException 
    {
	messageGenerator.sendQuery(minSpeed, searchString);	
    }
    
    public Message getNextMessage() throws IOException {
	return messageDecoder.decodeNextMessage();
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
}

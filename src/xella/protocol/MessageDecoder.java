
package xella.protocol;

import java.io.*;
import java.util.*;

/**
 * This class can decode Gnutella messages
 *
 */

class MessageDecoder {

    private InputStream in;

    public MessageDecoder(InputStream in) {
	this.in = in;
    }

    public Message decodeNextMessage() throws IOException {
	MessageHeader messageHeader = decodeHeader();
	
	/* Header decoded, decode body (thrash unsupported messages) */
	switch (messageHeader.getMessageType()) {

	case GnutellaConstants.PAYLOAD_PING:
	    return new PingMessage(messageHeader);

 	case GnutellaConstants.PAYLOAD_PONG:
 	    return decodePongMessage(messageHeader);

 	case GnutellaConstants.PAYLOAD_PUSH:
	    return decodePushMessage(messageHeader);

 	case GnutellaConstants.PAYLOAD_QUERY:
	    return decodeQueryMessage(messageHeader);

 	case GnutellaConstants.PAYLOAD_QUERY_HIT:
	    return decodeQueryResponseMessage(messageHeader);

	default:
	    /* unknown message type, just read past the body */
	    for (int i = 0; i < messageHeader.getMessageBodySize(); i++) {
		in.read();
	    }
	    return new Message(messageHeader);
	}
    }

    private MessageHeader decodeHeader() 
	throws IOException
    {	
	byte descriptorId[] = new byte[16];
	int bytesRead = in.read(descriptorId);
 	if (bytesRead != descriptorId.length) {
 	    throw new IOException("EOF before descriptor id read (read " 
 				  + bytesRead + " bytes)");
 	}
	
	int payloadDescriptor = read8Bit();
	int ttl = read8Bit();
	int hops = read8Bit();
	int payloadLength = read32Bit();
	    
	return new MessageHeader(descriptorId, payloadDescriptor, ttl, hops, payloadLength);
    }

    private PongMessage decodePongMessage(MessageHeader messageHeader)
	throws IOException
    {
	int port = read16Bit();
	String host = readIPNumber();
	int numShared = read32Bit();
	int kilobytesShared = read32Bit();
	return new PongMessage(messageHeader, host, port, numShared, kilobytesShared);
    }

    private PushMessage decodePushMessage(MessageHeader messageHeader)
	throws IOException
    {
	byte serventId[] = readServentIdentifier();
	int fileIndex = read32Bit();
	String host = readIPNumber();
	int port = read16Bit();

	return new PushMessage(messageHeader, serventId, host, port, fileIndex);
    }

    private QueryMessage decodeQueryMessage(MessageHeader messageHeader)
	throws IOException
    {
	int minSpeed = read16Bit();

	int stringSize = messageHeader.getMessageBodySize() - 3;
	String searchString = readAsciiString(stringSize);

	/* discard the null terminator */	
	read8Bit();
	
	return new QueryMessage(messageHeader, searchString, minSpeed);
    }

    private QueryResponseMessage decodeQueryResponseMessage(MessageHeader messageHeader)
	throws IOException
    {
	int numberOfHits = read8Bit();
	int port = read16Bit();
	String hostIP = readIPNumber();
	int hostSpeed = read32Bit();	
	List queryHits = new ArrayList();

	/*
	 * NOTE: this needs to take into account the strange bear-share extensions
	 *
	 */

	/* Read all hits */
	for (int i = 0; i < numberOfHits; i++) {
	    int fileIndex = read32Bit();
	    int fileSize = read32Bit();
	    String fileName = readAsciiString();

	    /* throw away extra null terminator */
	    read8Bit();
	    
	    queryHits.add(new QueryHit(fileIndex, fileSize, fileName));
	}

	byte serventId[] = readServentIdentifier();

	return new QueryResponseMessage(messageHeader, 
					serventId, 
					hostIP, 
					port, 
					hostSpeed, 
					queryHits); 
    }


    private int read8Bit() 
	throws IOException
    {
	int byteRead = in.read();
	if (byteRead == -1) {
	    throw new IOException("EOF before 8-bit value read");
	}
	
	return byteRead;	
    }

    private int read16Bit() 
	throws IOException 
    {
	int loByte = in.read();
	int hiByte = in.read();
	if (loByte == -1 || hiByte == -1) {
	    throw new IOException("EOF before 16-bit value read");
	}
	return (hiByte & 0xff) << 8 | (loByte & 0xff); 
    }

    private int read32Bit() 
	throws IOException 
    {
	byte buffer[] = new byte[4];
	int bytesRead = in.read(buffer);
	if (bytesRead != buffer.length) {
	    throw new IOException("EOF before 32-bit int read (read " 
				  + bytesRead + " expected " + buffer.length + ")");
	}
	
	return (buffer[0] & 0xff) 
	    | (buffer[1] & 0xff) << 8 
	    | (buffer[2] & 0xff) << 16 
	    | (buffer[3] & 0xff) << 24;	
    }

    private byte[] readServentIdentifier() 
	throws IOException
    {
	byte serventId[] = new byte[16];
	if (in.read(serventId) != 16) {
	    throw new IOException("EOF while reading servent id");
	}
	
	return serventId;
    }

    private String readIPNumber() 
	throws IOException
    {
	byte buffer[] = new byte[4];
	int bytesRead = in.read(buffer);
	if (bytesRead != buffer.length) {
	    throw new IOException("EOF before whole ipnumber read (read " 
				  + bytesRead + " expected " + buffer.length + ")");
	}
	
	return (buffer[0] < 0 ? buffer[0] + 256 : buffer[0]) 
	    + "." + (buffer[1] < 0 ? buffer[1] + 256 : buffer[1]) 
	    + "." + (buffer[2] < 0 ? buffer[2] + 256 : buffer[2]) 
	    + "." + (buffer[3] < 0 ? buffer[3] + 256 : buffer[3]); 
    }

    /**
     * Read fixed size ascii string 
     */
    private String readAsciiString(int size) throws IOException {
	
	byte stringBytes[] = new byte[size];
	int bytesRead = in.read(stringBytes);
	if (bytesRead != stringBytes.length) {
	    throw new IOException("EOF before whole string read (read " 
				  + bytesRead + " expected " + stringBytes.length + ")");
	}

	return new String(stringBytes, "ascii");
    }

    /**
     * Read ascii string up until nul termnation
     * (the null terminator is removed from the stream)
     *
     */
    private String readAsciiString() throws IOException {

	StringBuffer buffer = new StringBuffer(256);
	int readChar = in.read();
	while (readChar != 0) {
	    if (readChar ==  -1) {
		throw new IOException("EOF before whole string read (read " 
				      + buffer.length() + " bytes)");
	    }
	    buffer.append((char) readChar);
	    readChar = in.read();
	}

	return buffer.toString();
    }
}

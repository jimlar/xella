
package xella.protocol;

import java.io.*;

/**
 * This class can decode Gnutella messages
 *
 */

class MessageDecoder {

    public static Message decodeMessage(InputStream in) throws IOException {
	MessageHeader messageHeader = decodeHeader(in);
	
	/* Header decoded, decode body (thrash unsupported messages) */
	switch (messageHeader.getMessageType()) {

	case GnutellaConstants.PAYLOAD_PING:
	    return new PingMessage(messageHeader);

 	case GnutellaConstants.PAYLOAD_PONG:
 	    return decodePongMessage(messageHeader, in);

// 	case GnutellaConstants.PAYLOAD_PUSH:
// 	    break;

 	case GnutellaConstants.PAYLOAD_QUERY:
	    return decodeQueryMessage(messageHeader, in);

// 	case GnutellaConstants.PAYLOAD_QUERY_HIT:
// 	    break;

	default:
	    /* unknown message type, just ignore the body */
	    for (int i = 0; i < messageHeader.getMessageBodySize(); i++) {
		in.read();
	    }
	    return new Message(messageHeader);
	}
    }



    private static MessageHeader decodeHeader(InputStream in) 
	throws IOException
    {	
	byte buffer[] = new byte[GnutellaConstants.DESCRIPTOR_HEADER_LENGTH];
	int bytesRead = in.read(buffer);

	FileOutputStream fileOut = new FileOutputStream("message.out");
	fileOut.write(buffer, 0, bytesRead);

 	if (bytesRead != buffer.length) {
 	    throw new IOException("EOF before descriptor header read (read " 
 				  + bytesRead + " bytes)");
 	}

	/* 
	 * 0 - 15  = descriptor id 
	 * 16      = payload descriptor (type)
	 * 17      = ttl
	 * 18      = hops
	 * 19 - 22 = payload length
	 */

	byte descriptorId[] = new byte[16];
	System.arraycopy(buffer, 0, descriptorId, 0, descriptorId.length);
	
	int payloadDescriptor = (buffer[16] < 0 ? buffer[16] + 256 : buffer[16]);
	int ttl = (buffer[17] < 0 ? buffer[17] + 256 : buffer[17]);
	int hops = (buffer[18] < 0 ? buffer[18] + 256 : buffer[18]);
	
	/* Little endian */
	int payloadLength = (buffer[19] & 0xff) 
	    | (buffer[20] & 0xff) << 8 
	    | (buffer[21] & 0xff) << 16 
	    | (buffer[22] & 0xff) << 24;
	    
	return new MessageHeader(descriptorId, payloadDescriptor, ttl, hops, payloadLength);
    }

    private static PongMessage decodePongMessage(MessageHeader messageHeader, InputStream in)
	throws IOException
    {
	int port = read16Bit(in);
	String host = readIPNumber(in);
	int numShared = read32Bit(in);
	int kilobytesShared = read32Bit(in);
	return new PongMessage(messageHeader, host, port, numShared, kilobytesShared);
    }

    private static QueryMessage decodeQueryMessage(MessageHeader messageHeader, InputStream in)
	throws IOException
    {
	int minSpeed = read16Bit(in);

	/* The last byte is a zero byte, we just discard it */
	byte searchString[] = new byte[messageHeader.getMessageBodySize() - 3];
	int bytesRead = in.read(searchString);
	if (bytesRead != searchString.length) {
	    throw new IOException("EOF before search string read (read " 
				  + bytesRead + " expected " + searchString.length + ")");
	}
	in.read();
	
	return new QueryMessage(messageHeader, new String(searchString, "ascii"), minSpeed);
    }

    private static int read16Bit(InputStream in) 
	throws IOException 
    {
	int loByte = in.read();
	int hiByte = in.read();
	return (hiByte & 0xff) << 8 | (loByte & 0xff); 
    }

    private static int read32Bit(InputStream in) 
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

    private static String readIPNumber(InputStream in) 
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
}

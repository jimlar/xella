
package xella.net;

import java.nio.ByteBuffer;

public class QueryHit {
    
    private int fileIndex;
    private int fileSize;
    private String fileName;

    /* This is used by gnotella, possibly others too. Not really well defined */
    private byte extraData[];

    public QueryHit(int fileIndex, int fileSize, String fileName) {
	this(fileIndex, fileSize, fileName, null);
    }

    private QueryHit(int fileIndex, int fileSize, String fileName, byte extraData[]) {
	this.fileIndex = fileIndex;
	this.fileSize = fileSize;
	this.fileName = fileName;
	this.extraData = extraData;
    }

    public int getFileIndex() {
	return fileIndex;
    }

    public int getFileSize() {
	return fileSize;
    }

    public String getFileName() {
	return fileName;
    }

    public void writeTo(ByteBuffer buffer) {
	buffer.put(ByteEncoder.encode32Bit(getFileIndex()));
	buffer.put(ByteEncoder.encode32Bit(getFileSize()));
	buffer.put(ByteEncoder.encodeAsciiString(getFileName()));
	
	/* Double null terminated filename */
	buffer.put((byte) 0);
	
	if (extraData != null) {
	    buffer.put(extraData);
	}

	buffer.put((byte) 0);
    }

    public static QueryHit readFrom(ByteBuffer buffer) {

	int fileIndex = ByteDecoder.decode32Bit(buffer);
	int fileSize = ByteDecoder.decode32Bit(buffer);

	/* Read nullterminated string */
	String fileName = ByteDecoder.decodeAsciiString(buffer);
	
	/* read gnotella defined extra data if present (until the next null byte) */
	byte extraData[] = null;
	int b = ByteDecoder.decode8Bit(buffer);
	if (b != 0) {
	    byte tmpBuf[] = new byte[1024];
	    int i = 0;
	    while(b != 0) {
		tmpBuf[i++] = (byte) b;
		b = ByteDecoder.decode8Bit(buffer);
	    }
	   
	    extraData = new byte[i];
	    System.arraycopy(tmpBuf, 0, extraData, 0, extraData.length);
	}
	
	return new QueryHit(fileIndex, fileSize, fileName, extraData);
    }    

    /* Return the byte size of this hit object (for reading or writing) */
    public int size() {
	return 10 + fileName.length();
    }

    public String toString() {
	String toReturn = "QueryHit: index=" + fileIndex 
	    + ", size=" + fileSize 
	    + ", name=" + fileName;
	
	if (extraData == null) {
	    toReturn += ", no extra data";
	} else {
	    toReturn += ", extra data=";
	    for (int i = 0; i < extraData.length; i++) {
		toReturn += Integer.toHexString(extraData[i] < 0 ? extraData[i] + 256 : extraData[i]);
		if (i != (extraData.length - 1)) {
		    toReturn += ",";
		}
	    }
	}
	
	return toReturn;
    }
}


package xella.net;

import java.nio.ByteBuffer;

public class QueryHit {
    
    private int fileIndex;
    private int fileSize;
    private String fileName;

    public QueryHit(int fileIndex, int fileSize, String fileName) {
	this.fileIndex = fileIndex;
	this.fileSize = fileSize;
	this.fileName = fileName;
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
	buffer.put((byte) 0);
    }

    public static QueryHit readFrom(ByteBuffer buffer) {

	int fileIndex = ByteDecoder.decode32Bit(buffer);
	int fileSize = ByteDecoder.decode32Bit(buffer);

	/* Read nullterminated string */
	String fileName = ByteDecoder.decodeAsciiString(buffer);
	
	/* throw away extra null terminator */
	int nullTerminator = ByteDecoder.decode8Bit(buffer);
	
	return new QueryHit(fileIndex, fileSize, fileName);
    }    

    /* Return the byte size of this hit object (for reading or writing) */
    public int size() {
	return 10 + fileName.length();
    }

    public String toString() {
	return "QueryHit: index=" + fileIndex + ", size=" + fileSize + ", name=" + fileName;
    }
}


package xella.net;


public class QueryHit {
    
    private int fileIndex;
    private int fileSize;
    private String fileName;

    QueryHit(int fileIndex, int fileSize, String fileName) {
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

    public String toString() {
	return "QueryHit: index=" + fileIndex + ", size=" + fileSize + ", name=" + fileName;
    }
}

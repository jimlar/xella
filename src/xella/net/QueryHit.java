
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

    public String toString() {
	return "QueryHit: index=" + fileIndex + ", size=" + fileSize + ", name=" + fileName;
    }
}


package xella.protocol;


public class QueryMessage extends Message {
    
    private String searchString;
    private int minSpeed;

    QueryMessage(MessageHeader header, String searchString, int minSpeed) {
	super(header);
	this.searchString = searchString;
	this.minSpeed = minSpeed;
    }
    
    public String toString() {
	return "QueryMessage: query=" + searchString + ", minSpeed=" + minSpeed + ", " + getHeader().toString();
    }
}


package xella.net;


public class PingMessage extends Message {

    PingMessage(MessageHeader header) {
	super(header);
    }
    
    public String toString() {
	return "PingMessage: " + getHeader().toString();
    }
}


package xella.protocol;


public class Message {

    private MessageHeader header;

    Message(MessageHeader header) {
	this.header = header;
    }

    public MessageHeader getHeader() {
	return this.header;
    }

    public String toString() {
	return "Message: " + header.toString();
    }
}

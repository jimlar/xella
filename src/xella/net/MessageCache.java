
package xella.net;

import java.util.*;

import xella.net.*;

/**
 * Policy definitions used by the router
 * Extends this class to provde your own
 *
 */

class MessageCache {

    private int maxMessages;
    private List messageFIFO;
    private Map messagesById;

    public MessageCache(int maxMessages) {
	this.maxMessages = maxMessages;
	this.messageFIFO = new ArrayList();
	this.messagesById = new HashMap();
    }

    public synchronized void add(Message message) {
	Object o = messagesById.put(message.getMessageId(), message);

	/* protect against duplicate entries in fifo */
	if (o == null) {
	    messageFIFO.add(message);
	    if (messageFIFO.size() > maxMessages) {
		Message removedMessage = (Message) messageFIFO.remove(0);
		messagesById.remove(removedMessage.getMessageId());
	    }
	}
    }

    public synchronized Message getByMessageId(MessageId messageId) {
	return (Message) messagesById.get(messageId);
    }
}

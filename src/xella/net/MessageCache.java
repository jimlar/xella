
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
    private Map messagesByDescriptorId;

    public MessageCache(int maxMessages) {
	this.maxMessages = maxMessages;
	this.messageFIFO = new ArrayList();
	this.messagesByDescriptorId = new HashMap();
    }

    public synchronized void add(Message message) {
	Object o = messagesByDescriptorId.put(message.getDescriptorId(), message);

	/* protect against duplicate entries in fifo */
	if (o == null) {
	    messageFIFO.add(message);
	    if (messageFIFO.size() > maxMessages) {
		Message removedMessage = (Message) messageFIFO.remove(0);
		messagesByDescriptorId.remove(removedMessage.getDescriptorId());
	    }
	}
    }

    public synchronized Message getByDescriptorId(byte descriptorId[]) {
	return (Message) messagesByDescriptorId.get(descriptorId);
    }
}


package xella.router;

import java.util.*;

import xella.net.*;

/**
 * Policy definitions used by the router
 * Extends this class to provde your own
 *
 */

public class MessageCache {

    private int maxMessages;
    private List messageFIFO;
    private Map messagesByDescriptorId;

    public MessageCache(int maxMessages) {
	this.maxMessages = maxMessages;
	this.messageFIFO = new ArrayList();
	this.messagesByDescriptorId = new HashMap();
    }

    public synchronized void add(Message message) {
	messagesByDescriptorId.put(message.getDescriptorId(), message);
	messageFIFO.add(message);
	if (messageFIFO.size() > maxMessages) {
	    Message removedMessage = (Message) messageFIFO.remove(0);
	    messagesByDescriptorId.remove(removedMessage.getDescriptorId());
	}
    }

    public synchronized Message getByDescriptorId(byte descriptorId[]) {
	return (Message) messagesByDescriptorId.get(descriptorId);
    }
}

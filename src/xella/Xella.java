
package xella;

import java.io.*;
import java.net.*;

import xella.net.*;

/**
 * The main class of xella
 *
 */

public class Xella implements MessageListener {

    private GnutellaEngine engine;

    public Xella() throws Exception {
	this.engine = new GnutellaEngine(8, 6346);
	engine.start();
	engine.addMessageListener(this);
	engine.addHost("127.0.0.1", 2944);
	engine.addHost("gnutellahosts.com", 6346);
	engine.addHost("router.limewire.com", 6346);
	engine.addHost("gnutella.hostscache.com", 6346);
    }

    public void receivedPing(PingMessage message) {

	try {
	    /* Pong the pings */
	    PongMessage pongMessage 
		= MessageFactory.getInstance().createPongMessage(message,
								 "192.168.1.31", 
								 6346, 
								 12, 
								 1234567890);
	    engine.send(pongMessage);	
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void receivedPong(PongMessage message) {
	//System.out.println("Got: " + message);
    }
    public void receivedPush(PushMessage message) {
	System.out.println("Got: " + message);
    }
    public void receivedQuery(QueryMessage message) {
	System.out.println("Got: " + message);
    }
    public void receivedQueryResponse(QueryResponseMessage message) {
	System.out.println("Got: " + message);
    }


    public static void main(String args[]) throws Exception {
	Xella xella = new Xella();
    }
}

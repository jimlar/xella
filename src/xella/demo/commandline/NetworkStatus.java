package xella.demo.commandline;

import java.util.*;

import xella.net.*;

/**
 *
 * @author  jimmy
 */
public class NetworkStatus 
    implements MessageListener, ConnectionListener 
{
    private GnutellaEngine engine;

    private int numPings = 0;
    private int numPongs = 0;
    private int numPushes = 0;
    private int numQueries = 0;
    private int numQueryResponses = 0;

    private int numHostsConnecting = 0;
    private int numHostsConnected = 0;
    private int numConnectFailed = 0;
    private int numHostsDisconnected = 0;
    private int numHostsIgnored = 0;
    private int numFoundHosts = 0;

    public void receivedPing(PingMessage message) {
	numPings++;	

	/* Pong the pings */
	PongMessage pongMessage 
	    = MessageFactory.getInstance().createPongMessage(message,
							     "192.168.1.31", 
							     6346, 
							     12, 
							     1234567890);
	engine.send(pongMessage);	
    }


    public void receivedPong(PongMessage message) {
	numPongs++; 
    }

    public void receivedPush(PushMessage message) {
	numPushes++;
    }

    public void receivedQuery(QueryMessage message) {
	numQueries++; 
	
	/* Test to reply to a query */
	if (message.getSearchString().equals("ant")) {
	    
	    List hits = new ArrayList();
	    hits.add(new QueryHit(0, 1024, "big fat ant.mp3"));
	    QueryResponseMessage responseMessage 
		= MessageFactory.getInstance().createQueryResponseMessage(message,
									  new byte[16],
									  "192.168.1.31", 
									  6346, 
									  1024, 
									  hits);
	    engine.send(responseMessage);
	}
    }

    public void receivedQueryResponse(QueryResponseMessage message) {
	numQueryResponses++; 
    }

    public void showStatus() {
	
	System.out.println("Messages: " + numPings + " pings\n" 
			   + "          " + numPongs + " pongs\n"
			   + "          " + numPushes + " pushes\n" 
			   + "          " + numQueries + " queries\n" 
			   + "          " + numQueryResponses + " query responses\n"
			   + "Connections: " + numHostsConnected + " connected\n" 
			   + "             " + numHostsDisconnected + " disconnected\n" 
			   + "             " + numConnectFailed + " connect failed\n" 
			   + "             " + numHostsConnecting + " connecting\n"
			   + "             " + numHostsIgnored + " hosts ignored\n"
			   + "             " + numFoundHosts + " hosts found");
    }

    public synchronized void connecting(ConnectionInfo info) {
	numHostsConnecting++;
    }
    
    public synchronized void connected(ConnectionInfo info) {
	numHostsConnected++;
	numHostsConnecting--;
    }
    
    public synchronized void connectFailed(ConnectionInfo info) {
	numConnectFailed++;
	numHostsConnecting--;
    }
    
    public synchronized void hostIgnored(Host host) {
	numHostsIgnored++;
    }
    
    public synchronized void hostDiscovered(Host host) {
	numFoundHosts++;
    }
    
    public synchronized void disconnected(ConnectionInfo info) {
	numHostsDisconnected++;
	numHostsConnected--;
    }
    public synchronized void statusChange(ConnectionInfo info) {}
}

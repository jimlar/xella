package xella.demo;

import java.io.IOException;
import java.util.*;

import xella.net.*;

/**
 *
 * @author  jimmy
 */
public class CommandLineDemo 
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

    /** Creates new form XellaDemo */
    public CommandLineDemo() throws IOException {
        this.engine = new GnutellaEngine(10, 12, 6346);

	engine.addMessageListener(this);
	engine.addConnectionListener(this);
	engine.start();

	engine.addHost("127.0.0.1", 5555);
	engine.addHost("gnutellahosts.com", 6346);
 	engine.addHost("router.limewire.com", 6346);
	engine.addHost("gnutella.hostscache.com", 6346);
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) throws Exception {
        new CommandLineDemo();
	while (true) {
	    Thread.sleep(1000);
	}
    }
    
    public void receivedPing(PingMessage message) {
	numPings++;
	showStatus();

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
	showStatus();
    }

    public void receivedPush(PushMessage message) {
	numPushes++;
	showStatus();
    }

    public void receivedQuery(QueryMessage message) {
	numQueries++; 
	showStatus();
	
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
	showStatus();
    }

    private void showStatus() {
	
	System.out.print("\rmsg: " + numPings + " pi, " 
			 + numPongs + " po, "
			 + numPushes + " pu, " 
			 + numQueries + " q, " 
			 + numQueryResponses + " qr."
			 + " con: " + numHostsConnected + " c, " 
			 + numHostsDisconnected + " dc, " 
			 + numConnectFailed + " cf, " 
			 + numHostsConnecting + " cing, "
			 + numHostsIgnored + " ih, "
			 + numFoundHosts + " fh");
    }

    public synchronized void connecting(ConnectionInfo info) {
	numHostsConnecting++;
	showStatus();
    }
    
    public synchronized void connected(ConnectionInfo info) {
	numHostsConnected++;
	numHostsConnecting--;
	showStatus();
    }
    
    public synchronized void connectFailed(ConnectionInfo info) {
	numConnectFailed++;
	numHostsConnecting--;
	showStatus();
    }
    
    public synchronized void hostIgnored(Host host) {
	numHostsIgnored++;
	showStatus();
    }
    
    public synchronized void hostDiscovered(Host host) {
	numFoundHosts++;
	showStatus();
    }
    
    public synchronized void disconnected(ConnectionInfo info) {
	numHostsDisconnected++;
	numHostsConnected--;
	showStatus();
    }
    public synchronized void statusChange(ConnectionInfo info) {}
}

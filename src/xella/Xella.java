
package xella;

import java.io.*;
import java.net.*;

import xella.net.*;

/**
 * The main class of xella
 *
 */

public class Xella {

    public static void main(String args[]) throws Exception {

	ServerSocket serv = new ServerSocket(6346);
	Socket socket = serv.accept();
	
 	//Socket socket = new Socket("192.168.1.1", 2662);
 	//Socket socket = new Socket("gnutellahosts.com", 6346);

	GnutellaConnection con = new GnutellaConnection(socket, false);
	Mongo mongo = new Mongo(con);

	while (true) {

	    //con.send(MessageFactory.getInstance().createQueryMessage(0, "cult"));
	    con.send(MessageFactory.getInstance().createPingMessage());
	    Thread.sleep(10000);
	}
    }

    private static class Mongo extends Thread {
	private GnutellaConnection con;

	public Mongo(GnutellaConnection con) {
	    this.con = con;
	    this.start();
	}

	public void run() {
	    try {
		while (true) {
		    Message m = con.receiveNextMessage();
		    System.out.println(m.toString());	    

		    if (m instanceof PingMessage) {
			System.out.println("Got ping, sending pong(s)...");
			PongMessage message 
			    = MessageFactory.getInstance().createPongMessage((PingMessage) m, 
									     "192.168.1.31", 
									     6346, 
									     12, 
									     1234567890);
			con.send(message);
		    }
		}	
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }
}

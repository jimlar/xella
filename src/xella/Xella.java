
package xella;

import java.io.*;
import java.net.*;

import xella.protocol.*;

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

	    //con.sendQuery(0, "cult");
	    con.sendPing();
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
		    Message m = con.getNextMessage();
		    System.out.println(m.toString());	    

		    if (m instanceof PingMessage) {
			System.out.println("Got ping, sending pong(s)...");
			con.sendPong((PingMessage) m, "192.168.1.31", 6346, 12, 1024 * 1024);
// 			con.sendPong("197.168.1.32", 6346, 12, 14);
// 			con.sendPong("197.168.1.33", 6346, 12, 14);
// 			con.sendPong("197.168.1.34", 6346, 12, 14);
// 			con.sendPong("197.168.1.35", 6346, 12, 14);
// 			con.sendPong("197.168.1.36", 6346, 12, 14);
// 			con.sendPong("197.168.1.37", 6346, 12, 14);
// 			con.sendPong("197.168.1.38", 6346, 12, 14);
// 			con.sendPong("197.168.1.39", 6346, 12, 14);
		    }
		}	
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }
}

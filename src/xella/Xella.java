
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
	    con.sendPing();
	    Thread.sleep(1000);
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
		}	
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }
}

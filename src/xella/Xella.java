
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

	Socket socket = new Socket("gnutellahosts.com", 6346);

	System.out.print("Sending ping...");
	OutputStream out = socket.getOutputStream();
	PacketGenerator.sendPing(out);
	out.flush();
	System.out.println("ok!");

	MongoThread m = new MongoThread(socket.getInputStream());
    }

    private static class MongoThread extends Thread {
	
	private InputStream in;

	public MongoThread(InputStream in) {
	    this.in = in;
	    this.start();
	}

	public void run() {
	    while (true) {
		try {
		    Packet p = PacketDecoder.decodePacket(in);
		    System.out.println("Got packet: " + p);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}
    }
}

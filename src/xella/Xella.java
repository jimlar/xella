
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
	Socket sock = serv.accept();
	
	GnutellaConnection con = new GnutellaConnection(sock, false);
	while (true) {
	    Packet p = con.getNextPacket();
	    System.out.println("Got packet: " + p);	    
	}

// 	Socket socket = new Socket("192.168.1.1", 2662);

// 	System.out.print("Sending ping...");
// 	OutputStream out = socket.getOutputStream();
// 	PacketGenerator.sendPing(out);
// 	out.flush();
// 	System.out.println("ok!");

// 	MongoThread m = new MongoThread(socket.getInputStream());
    }
}

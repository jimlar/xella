package xella.demo.commandline;

import java.io.*;
import java.util.*;

import xella.net.*;

/**
 *
 * @author  jimmy
 */
public class CommandLineDemo {

    private GnutellaEngine engine;
    private NetworkStatus status;

    public CommandLineDemo() throws IOException {
        this.engine = new GnutellaEngine(10, 12, 6346);
	this.status = new NetworkStatus();

	engine.addMessageListener(status);
	engine.addConnectionListener(status);
	engine.start();

	engine.addHost("gnutellahosts.com", 6346);
 	engine.addHost("router.limewire.com", 6346);
	engine.addHost("gnutella.hostscache.com", 6346);
    }
    
    public String readCommand(String prompt) throws IOException {
	System.out.print(prompt + " ");
	return readLine();
    }

    public String readLine() throws IOException {

	StringBuffer buffer = new StringBuffer();
	int chr = System.in.read();
	while (chr != -1 && chr != '\n' && chr != '\r') {
	    buffer.append((char) chr);
	    chr = System.in.read();
	}

	return buffer.toString();
    }

    public void executeCommand(String command) {
	try {
	    if (command.startsWith("s")) {
		status.showStatus();
	    } else if (command.startsWith("q")) {
		System.exit(0);
	    } else if (command.startsWith("add")) {
		
		String host = null;
		int port = 6346;

		int i = command.indexOf(" ");
		if (i == -1) {
		    throw new IllegalArgumentException("Unknown command " + command);
		}

		int j = command.indexOf(":", i);
		if (j == -1) {
		    host = command.substring(i + 1);
		} else {
		    host = command.substring(i + 1, j);
		    try {
			port = Integer.parseInt(command.substring(j + 1));
		    } catch (NumberFormatException e) {
			throw new IllegalArgumentException("Illegal port number " + command.substring(j + 1));
		    }
		}

		engine.addHost(host, port);
		System.out.println("Added " + host + ":" + port);

	    } else {
		throw new IllegalArgumentException("Unknown command " + command);
	    }
	} catch (IllegalArgumentException e) {
	    System.out.println(e.getMessage());
	}
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        CommandLineDemo demo = new CommandLineDemo();

	while (true) {
	    String command = demo.readCommand("Xella:");
	    demo.executeCommand(command);
	}
    }
}

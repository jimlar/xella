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
	if (command.equals("status")) {
	    status.showStatus();
	} else {
	    System.out.println("Unknown command " + command);
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

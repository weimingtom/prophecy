package com.ugame.prophecy.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @see 
 * @author Administrator
 *
 */
public class SimpleSocketServer {
    public static void main(String[] args) throws IOException {
	System.out.println("This is SimpleSocketServer running on port "
		+ Utility.PORT);
	ServerSocket serverSocket = null;
	boolean listening = true;
	try {
	    serverSocket = new ServerSocket(Utility.PORT);
	} catch (IOException e) {
	    System.err.println("Could not listen on port:" + Utility.PORT);
	    System.exit(-1);
	}
	while (listening) {
	    new ThreadedHandler(serverSocket.accept()).start();
	}
	serverSocket.close();
    }
}

class ThreadedHandler extends Thread {
    private Socket socket = null;
    
    public ThreadedHandler(Socket socket) {
	super("ThreadedHandler");
	this.socket = socket;
    }

    @Override
    public void run() {
	try {
	    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	    BufferedReader in = new BufferedReader(new InputStreamReader(socket
		    .getInputStream()));
	    in.readLine();
	    out.print(Utility.getPage("SimpleSocketServer", 512));
	    out.close();
	    in.close();
	    socket.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}

package com.ugame.prophecy.protocol.tcp.mmocore;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;

/**
 * 包头2字节, short数值(little endian, 非网络，低位小端在前)
 * 等于包体长度加2
 * @see test/testmmocorepack.py
 */
public class PacketServer {
    private final static int PORT = 9999;
    private final SelectorConfig sc = new SelectorConfig();    
    private final SelectorThread<PacketSession> _selectorThread;
    private final PacketHandler _packetHandler;
    private InetAddress bindAddress = null;
	
    public PacketServer() throws IOException {
	sc.MAX_READ_PER_PASS = 12;
	sc.MAX_SEND_PER_PASS = 12;
	sc.SLEEP_TIME = 20;
	sc.HELPER_BUFFER_COUNT = 20;
	_packetHandler = new PacketHandler();
	_selectorThread = new SelectorThread<PacketSession>(sc, 
		_packetHandler, _packetHandler, _packetHandler, 
		new IPv4Filter());
	try {
	    bindAddress = InetAddress.getByName("localhost");
	} catch (UnknownHostException e) {
	    e.printStackTrace();
	}
    }
    
    public void start() {
	try {
	    _selectorThread.openServerSocket(bindAddress, PORT);
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.out.println("listen on port " + PORT);
	_selectorThread.start();
    }
    
    public static void main(String[] args) throws IOException {
	new PacketServer().start(); 
   }
}

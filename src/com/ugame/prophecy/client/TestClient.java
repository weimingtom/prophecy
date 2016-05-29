package com.ugame.prophecy.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @see D:/ugame/src/java/TCP_IP_Sockets_in_Java
 * @author Administrator
 * 
 */
public class TestClient {
    public static final String ADDR = "127.0.0.1";
    public static final String PORT = "8899";
    public static final boolean TEST_SEND = true;
    public static final boolean TEST_RECEIVE = false;

    public static void main(String args[]) {
	String destAddr = ADDR;
	int destPort = Integer.parseInt(PORT);
	// 连接
	Socket sock = null;
	try {
	    sock = new Socket(destAddr, destPort);
	    OutputStream out = sock.getOutputStream();
	    ClientCoder coder = new ClientCoder(sock.getInputStream());
	    
	    byte[] encodedMsg = null;
	    ClientMsg msg = null;
	    if (TEST_SEND) {
		// 编码
		msg = new ClientMsg(
			ClientCoder.MODL_GAMECLIENT, 
			ClientCoder.NMSG_LOGIN);
		msg.username = "nanami";
		msg.password = "123456"; 
		encodedMsg = coder.toWire(msg);
		System.out.println("Sending Message (" + encodedMsg.length
			+ " bytes): ");
		System.out.println(msg);
		// 发送
		coder.frameMsg(encodedMsg, out);
	    }
	    if (TEST_RECEIVE) {
		// 解码
		msg = coder.fromWire(encodedMsg);
		System.out.println("Received Response (" + encodedMsg.length
			+ " bytes): ");
		System.out.println(msg);
		// 接收后解码
		msg = coder.fromWire(coder.nextMsg());
		System.out.println("Received Response (" + encodedMsg.length
			+ " bytes): ");
		System.out.println(msg);
	    }
	} catch (UnknownHostException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    try {
		if(sock != null) {
		    sock.close();
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }
}

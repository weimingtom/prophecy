package com.ugame.prophecy.protocol.tcp.javanio;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalConfig;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.tcp.IPackServer;


public class JavaNIOPackServer implements IPackServer {
    private static final Logger LOGGER = LoggerFactory
	    .getLogger(JavaNIOPackServer.class);

    private static JavaNIOPackServer instance;
    private JavaNIOPackThread thread = null;
    
    private JavaNIOPackServer() {
	
    }
    
    public static JavaNIOPackServer getInstance() {
	if(instance == null) {
	    instance = new JavaNIOPackServer();
	}
	return instance;
    }
    
    @Override
    public void start() {
	try {
	    Selector selector = Selector.open();
	    ServerSocketChannel server = ServerSocketChannel.open();
	    server.socket().bind(new java.net.InetSocketAddress(GlobalConfig.DEFAULT_PORT));
	    server.configureBlocking(false);
	    CommonSysLog.output(this.getClass().getName() + " : Listening on port " + GlobalConfig.DEFAULT_PORT);
	    thread = new JavaNIOPackThread(selector, server);
	    thread.start();
	} catch (IOException e) {
	    LOGGER.error("IOException:", e.getCause());
	}
    }

    @Override 
    public void stop() {
	//FIXME: no implement
	thread.interrupt();
    }
}

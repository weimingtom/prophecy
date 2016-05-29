package com.ugame.prophecy.protocol.tcp.grizzly;

import java.io.IOException;

import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalConfig;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.tcp.IPackServer;

/**
 * 
 * @author Administrator
 *
 */
public class GrizzlyPackServer implements IPackServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrizzlyPackServer.class);
    
    private static GrizzlyPackServer instance;
    private TCPNIOTransport transport;
    
    public void start() throws IOException {
	FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();
        filterChainBuilder.add(new TransportFilter());
        filterChainBuilder.add(new GrizzlyPackServerFilter());
        transport = TCPNIOTransportBuilder.newInstance().build();
        transport.setProcessor(filterChainBuilder.build());
        transport.bind("localhost", GlobalConfig.DEFAULT_PORT);
        transport.start();
	CommonSysLog.info(LOGGER, "Listening on port " + GlobalConfig.DEFAULT_PORT);
	CommonSysLog.output(this.getClass().getName() + " : Listening on port " + GlobalConfig.DEFAULT_PORT);
    }
    
    public void stop() throws IOException {
        transport.stop();
    }
    
    public static GrizzlyPackServer getInstance() {
	if(instance == null) {
	    instance = new GrizzlyPackServer();
	}
	return instance;
    }
    
    public static void main(String[] args) {
	GrizzlyPackServer server = getInstance();
	try {
	    server.start();
	    System.in.read();
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    try {
		server.stop();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }
    
    // ---------------------------------------------
    private GrizzlyPackServer() {

    }
}


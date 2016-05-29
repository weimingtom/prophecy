package com.ugame.prophecy.protocol.tcp.xsocket;

import org.xsocket.connection.IServer;
import org.xsocket.connection.Server;
import org.xsocket.connection.IConnection.FlushMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalConfig;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.tcp.IPackServer;

public class XSocketPackServer implements IPackServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(XSocketPackServer.class);
    
    private static XSocketPackServer instance;
    
    private transient IServer srv;
    
    @Override
    public void start() throws Exception {
	srv = new Server(GlobalConfig.DEFAULT_PORT, 
		new XSocketPackServerHandler());
	srv.setFlushmode(FlushMode.ASYNC);
	srv.start();
	CommonSysLog.info(LOGGER, "Listening on port " + GlobalConfig.DEFAULT_PORT);
	CommonSysLog.info(LOGGER, srv.getStartUpLogMessage());
	CommonSysLog.output(this.getClass().getName() + " : Listening on port " + GlobalConfig.DEFAULT_PORT);
    }

    @Override
    public void stop() throws Exception {
	// TODO Auto-generated method stub
	srv.close();
    }

    public static XSocketPackServer getInstance() {
	if(instance == null) {
	    instance = new XSocketPackServer();
	}
	return instance;
    }
/*
    public static void main(final String[] args) throws Exception {
	getInstance().start();
    }
*/
}

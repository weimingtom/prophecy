package com.ugame.prophecy.protocol.tcp.xnet;

import com.ugame.prophecy.global.GlobalConfig;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.tcp.IPackServer;

import xnet.core.server.Config;
import xnet.core.server.Server;

public class XnetPackServer implements IPackServer {
    private static XnetPackServer instance;
    private Server server; 
    
    @Override
    public void start() throws Exception {
	Config config = new Config();
	config.keepalive = true;
	config.session = XnetPackSession.class;
	config.threadNum = Runtime.getRuntime().availableProcessors() + 1;
	config.port = GlobalConfig.DEFAULT_PORT;
	config.keepalive = true;
	config.ip = "0.0.0.0";
	config.maxConnection = 1000;
	//config.rTimeout = 3000;
	//config.wTimeout = 3000;
	server = new Server(config);
	CommonSysLog.output(this.getClass().getName() + 
		" : Listening on port " + GlobalConfig.DEFAULT_PORT);
	Runnable runnable = new Runnable(){
	    public void run() {
		try {
		    server.run();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	};
	new Thread(runnable).start();
    }

    @Override
    public void stop() throws Exception {
	
    }
    
    
    private XnetPackServer() {
	
    }

    public static XnetPackServer getInstance() {
	if(instance == null) {
	    instance = new XnetPackServer();
	}
	return instance;
    }
    
    public static void main(String[] args) {
	
    }
}


package com.ugame.prophecy.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalConfig;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.tcp.IPackServer;
import com.ugame.prophecy.protocol.tcp.PackServerFactory;

public class ConsoleMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleMain.class);
    //服务器实例
    private transient IPackServer server;

    public ConsoleMain() {
	server = PackServerFactory.newServer(GlobalConfig.serverType);
    }
    
    public void init() {
	try {
    	    //启动服务器
	    server.start();
    	} catch (Exception ex){
    	    CommonSysLog.error(LOGGER, "server start error.", ex);
    	}
    }
    
    public static void main(final String[] args) {
	final ConsoleMain console = new ConsoleMain();
	console.init();
    }
}


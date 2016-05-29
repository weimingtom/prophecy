package com.ugame.prophecy.protocol.tcp.yanf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.yanf4j.config.Configuration;
import com.google.code.yanf4j.core.impl.AbstractController;
import com.google.code.yanf4j.core.impl.ByteBufferCodecFactory;
import com.google.code.yanf4j.nio.TCPController;
import com.ugame.prophecy.global.GlobalConfig;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.tcp.IPackServer;


/**
 * Yanf4j is a part of XMemcached
 * @see http://code.google.com/p/xmemcached/
 * @see http://www.blogjava.net/killme2008/archive/2008/10/11/233747.html
 * @author Administrator
 * 
 */
public class Yanf4jPackServer implements IPackServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Yanf4jPackServer.class);
    
    private static Yanf4jPackServer instance;
    
    @Override
    public void start() throws Exception {
	// TODO Auto-generated method stub
	Configuration configuration = new Configuration();
	configuration.setStatisticsServer(true);
	configuration.setSessionReadBufferSize(256 * 1024); // 设置读的缓冲区大小
	// controller.setReuseAddress(false); // 设置是否重用端口
	AbstractController controller = new TCPController(configuration,
		new ByteBufferCodecFactory());
	//TextLineCodecFactory
	controller.setLocalSocketAddress(new InetSocketAddress("localhost",
		GlobalConfig.DEFAULT_PORT));
	controller.setReadThreadCount(1); // 设置读线程数，通常为1
	controller.setHandler(new Yanf4jPackServerHandler()); // 设置handler
	controller.setHandleReadWriteConcurrently(true); // 设置是否允许读写并发处理
	// controller.addStateListener(new ServerStateListener());
	controller.start();
	CommonSysLog.info(LOGGER, "Listening on port " + GlobalConfig.DEFAULT_PORT);
	CommonSysLog.output(this.getClass().getName() + " : Listening on port " + GlobalConfig.DEFAULT_PORT);
    }

    @Override
    public void stop() throws Exception {
	// TODO Auto-generated method stub
	
    }
    
    private Yanf4jPackServer() {
	
    }
    
    public static Yanf4jPackServer getInstance() {
	if(instance == null) {
	    instance = new Yanf4jPackServer();
	}
	return instance;
    }
    
    public static void main(String[] args) throws IOException {

    }
}

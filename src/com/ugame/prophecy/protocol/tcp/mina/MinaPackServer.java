package com.ugame.prophecy.protocol.tcp.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.service.SimpleIoProcessorPool;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioProcessor;
import org.apache.mina.transport.socket.nio.NioSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalConfig;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.tcp.IPackServer;

/**
 * 服务器实例
 * @author Administrator
 *
 */
public final class MinaPackServer implements IPackServer{
    private static final Logger LOGGER = LoggerFactory.getLogger(MinaPackServer.class);
    
    private static MinaPackServer instance;
    
    private final transient ExecutorService executorService = 
	Executors.newCachedThreadPool();
    private final transient SimpleIoProcessorPool<NioSession> pool = 
	new SimpleIoProcessorPool<NioSession>(
	    NioProcessor.class, 
	    executorService, 
	    Runtime.getRuntime().availableProcessors() + 1);

    private final transient NioSocketAcceptor acceptor = new NioSocketAcceptor(pool);

    @Override
    public void start() throws IOException {
	if (GlobalConfig.IS_ADD_LOGGER) {
	    acceptor.getFilterChain().addLast("logger", new LoggingFilter());
	}
	//关闭纳格算法，减少延时
	acceptor.getSessionConfig().setTcpNoDelay(true);
	acceptor.setHandler(new MinaPackServerHandler());
	acceptor.setReuseAddress(true);
	acceptor.bind(new InetSocketAddress(GlobalConfig.DEFAULT_PORT));
	CommonSysLog.info(LOGGER, "Listening on port " + GlobalConfig.DEFAULT_PORT);
	CommonSysLog.output(this.getClass().getName() + " : Listening on port " + GlobalConfig.DEFAULT_PORT);
    }

    @Override
    public void stop() {
	if (acceptor != null) {
	    //关闭服务器路由总开关
	    acceptor.unbind();
	    acceptor.dispose();
	    //关闭线程池，否则无法退出
	    //executorService.shutdown();
	    shutdownAndAwaitTermination(executorService);
	}
    }

    /**
     * @see ExecutorService的注释
     * @param pool
     */
    private static void shutdownAndAwaitTermination(ExecutorService pool) {
	pool.shutdown(); // Disable new tasks from being submitted
	try {
	    // Wait a while for existing tasks to terminate
	    if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
		pool.shutdownNow(); // Cancel currently executing tasks
		// Wait a while for tasks to respond to being cancelled
		if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
		    System.err.println("Pool did not terminate");
		}
	    }
	} catch (InterruptedException ie) {
	    // (Re-)Cancel if current thread also interrupted
	    pool.shutdownNow();
	    // Preserve interrupt status
	    Thread.currentThread().interrupt();
	}
    }
    
    public static MinaPackServer getInstance() {
	if(instance == null) {
	    instance = new MinaPackServer();
	}
	return instance;
    }
    
    // ---------------------------------------------
    private MinaPackServer() {

    }
}

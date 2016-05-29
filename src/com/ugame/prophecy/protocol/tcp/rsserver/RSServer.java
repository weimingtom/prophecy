package com.ugame.prophecy.protocol.tcp.rsserver;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.log.CommonSysLog;

/**
 * 服务器主线程
 * @see testjavasocket
 * 
 * 私服SVN:
 * @see http://code.google.com/p/regesa/ 基于rsserver
 * @see http://rt5e.googlecode.com/svn/trunk 基于netty
 * @see http://holdenms.googlecode.com/svn/trunk 基于Mina
 * 
 * @author Administrator
 *
 */
public class RSServer implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RSServer.class);
    
    /**
     * 循环时间
     * 默认500
     */
    public static final int cycleTime = 10; 
    /**
     * 端口号
     */
    public static int serverlistenerPort = 8899;
    /**
     * 单实例
     */
    public static RSServer clientHandler = null;
    /**
     * Accept线程
     */
    public static RSClientHandler playerHandler = null;
    /**
     * Accept套接字
     */
    public static ServerSocket clientListener = null;
    /**
     * 关闭服务器主线程
     */
    public static boolean shutdownServer = false;
    /**
     * 关闭Accept线程
     */
    public static boolean shutdownClientHandler;

    
    /**
     * 初始化服务器
     */
    public RSServer() {
	
    }

    @Override
    public void run() {
	try {
	    shutdownClientHandler = false;
	    clientListener = new ServerSocket(serverlistenerPort, 1, null);
	    CommonSysLog.info(LOGGER, "Starting BlakeScape Server Mod on "
		    + clientListener.getInetAddress().getHostAddress() + ":"
		    + clientListener.getLocalPort());
	    while (true) {
		Socket s = clientListener.accept();
		s.setTcpNoDelay(true);
		String connectingHost = s.getInetAddress().getHostName();
		if (connectingHost.startsWith("localhost")
			|| connectingHost.equals("127.0.0.1")) {
		    CommonSysLog.info(LOGGER, "ClientHandler: Accepted from "
			    + connectingHost + ":" + s.getPort());
		    playerHandler.newPlayerClient(s, connectingHost);
		} else {
		    CommonSysLog.info(LOGGER, "ClientHandler: Rejected " + connectingHost
			    + ":" + s.getPort());
		    s.close();
		}
	    }
	} catch (IOException e) {
	    if (!shutdownClientHandler) {
		CommonSysLog.error(LOGGER, "Error: Unable to startup listener on "
			+ serverlistenerPort + " - port already in use?", e);
	    } else {
		CommonSysLog.info(LOGGER, "ClientHandler was shut down.");
	    }
	}
    }
    
    /**
     * 关闭服务器
     */
    public void killServer() {
	try {
	    shutdownClientHandler = true;
	    if (clientListener != null) {
		clientListener.close();
	    }
	    clientListener = null;
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    /**
     * 主入口
     * @param args
     */
    public static void main(String args[]) {
	clientHandler = new RSServer();
	(new Thread(clientHandler)).start();
	playerHandler = new RSClientHandler();
	int waitFails = 0;
	long lastTicks = System.currentTimeMillis();
	long totalTimeSpentProcessing = 0;
	int cycle = 0;
	while (!shutdownServer) {
	    playerHandler.process();
	    long timeSpent = System.currentTimeMillis() - lastTicks;
	    totalTimeSpentProcessing += timeSpent;
	    if (timeSpent >= cycleTime) {
		timeSpent = cycleTime;
		if (++waitFails > 100) {
		    shutdownServer = true;
		    CommonSysLog.info(LOGGER, "[KERNEL]: machine is too slow to run this server!");
		}
	    }
	    try {
		Thread.sleep(cycleTime - timeSpent);
	    } catch (Exception e) {
		
	    }
	    lastTicks = System.currentTimeMillis();
	    cycle++;
	    if (cycle % 100 == 0) {
		float time = ((float) totalTimeSpentProcessing) / cycle;
		CommonSysLog.info(LOGGER, "[KERNEL]: " + (time * 100 / cycleTime)
			+ "% processing time");
	    }
	}
	playerHandler.destruct();
	clientHandler.killServer();
	clientHandler = null;
    }
}

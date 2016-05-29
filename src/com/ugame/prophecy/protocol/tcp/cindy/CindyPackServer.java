package com.ugame.prophecy.protocol.tcp.cindy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalConfig;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.tcp.IPackServer;

import net.sf.cindy.Session;
import net.sf.cindy.SessionAcceptor;
import net.sf.cindy.SessionAcceptorHandlerAdapter;
import net.sf.cindy.SessionHandler;
import net.sf.cindy.SessionType;
import net.sf.cindy.decoder.ByteBufferDecoder;
import net.sf.cindy.session.SessionFactory;

/**
 * TODO:
 * @author Administrator
 *
 */
public class CindyPackServer implements IPackServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CindyPackServer.class);
    
    private static CindyPackServer instance;
    
    private transient SessionHandler handler;
    private transient SessionAcceptor acceptor;
    
    @Override
    public void start() {
	handler = new CindyPackServerHandler();
        acceptor = SessionFactory
                .createSessionAcceptor(SessionType.TCP);
        acceptor.setListenPort(GlobalConfig.DEFAULT_PORT);
        acceptor.setAcceptorHandler(new SessionAcceptorHandlerAdapter() {
            @Override
            public void sessionAccepted(final SessionAcceptor acceptor, final Session session) throws Exception {
        	//缺省的PacketDecoder在读取时可能有问题
        	session.setPacketDecoder(new ByteBufferDecoder());
        	//session.setPacketDecoder(new BufferDecoder());
                session.setSessionHandler(handler);
                session.start();
            }
            
	    @Override
	    public void exceptionCaught(SessionAcceptor arg0, Throwable arg1) {
		super.exceptionCaught(arg0, arg1);
	    }
        });
        acceptor.start();
        if (acceptor.isStarted()) {
            CommonSysLog.info(LOGGER, "Listening on " + acceptor.getListenAddress());
    	    CommonSysLog.output(this.getClass().getName() + " : Listening on port " + GlobalConfig.DEFAULT_PORT);
        }
    }
    
    @Override
    public void stop() {
	acceptor.close();
    }
    
    public static CindyPackServer getInstance() {
	if(instance == null) {
	    instance = new CindyPackServer();
	}
	return instance;
    }
    
/*
    public static void main(String[] args) throws IOException {
        getInstance().start();
    }
*/
}

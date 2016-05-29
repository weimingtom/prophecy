package com.ugame.prophecy.protocol.http;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Apache Mina 1
 * @see http://mina.apache.org/
 */
final public class TestHttpServer {
    private final static Logger LOGGER = LoggerFactory.getLogger(TestHttpServer.class);
    
    private final static int DEFAULT_PORT = 8080;
    public final static String VERSION_STRING = "$Revision: 555855 $ $Date: 2007-07-13 12:19:00 +0900 (Fri, 13 Jul 2007) $";

    private TestHttpServer() {
	
    }
    
    public static void main(final String[] args) throws IOException {
        int port = DEFAULT_PORT;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-port")) {
                port = Integer.parseInt(args[i + 1]);
            }
        }
        final SocketAcceptor acceptor = new NioSocketAcceptor();
        acceptor.setReuseAddress(true);
        acceptor.setHandler(new TestHttpServerHandler());
        acceptor.bind(new InetSocketAddress(port));
        LOGGER.info("Server now listening on port " + port);
    }
}

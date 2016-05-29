package com.ugame.prophecy.serializer.amf3;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;


public class GameServer {

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
	// TODO Auto-generated method stub
	// 创建一个非阻塞的Server端Socket,用NIO
	SocketAcceptor acceptor = new NioSocketAcceptor();
	// 创建接收数据的过滤器
	DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
	chain.addLast("amf3", new ProtocolCodecFilter(new AMF3CodecFactory()));
	chain.addLast("logger", new LoggingFilter());
	// 设定服务器端的消息处理器,
	acceptor.setHandler(new GameServerHandler());
	// 服务器端绑定的端口
	int bindPort = 110;
	// 绑定端口,启动服务器
	acceptor.bind(new InetSocketAddress(bindPort));
	System.out.println("Game Server is Listing on= " + bindPort);
    }

}

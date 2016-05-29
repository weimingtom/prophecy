package com.ugame.prophecy.protocol.tcp.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalConfig;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.tcp.IPackServer;

/**
 * 
 * @author Administrator
 * @see http://code.google.com/p/jmemcache-daemon/
 */
public class NettyPackServer implements IPackServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyPackServer.class);
    
    private static NettyPackServer instance;
    
    private ServerSocketChannelFactory channelFactory;
    private DefaultChannelGroup allChannels;
    
    /**
     * 创建频道，监听端口，然后加入频道组
     */
    @Override
    public void start() {
	channelFactory = new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());
        ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);
        allChannels = new DefaultChannelGroup("NettyPackServerChannelGroup");
        ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
	    @Override
	    public ChannelPipeline getPipeline() throws Exception {
	        ChannelPipeline p = Channels.pipeline();
	        p.addLast("handler", new NettyPackServerHandler());
	        return p;
	    }
	};
        bootstrap.setPipelineFactory(pipelineFactory);
        bootstrap.setOption("sendBufferSize", 65536);
        bootstrap.setOption("receiveBufferSize", 65536);
        InetSocketAddress addr = new InetSocketAddress(
        	GlobalConfig.DEFAULT_PORT);
        Channel serverChannel = bootstrap.bind(addr);
        allChannels.add(serverChannel);
	CommonSysLog.output(this.getClass().getName() + " : Listening on port " + GlobalConfig.DEFAULT_PORT);
    }
    
    @Override
    public void stop() throws NettyPackServerException {
	CommonSysLog.info(LOGGER, "terminating daemon; closing all channels");
        ChannelGroupFuture future = allChannels.close();
        future.awaitUninterruptibly();
        if (!future.isCompleteSuccess()) {
            throw new NettyPackServerException(
        	    "failure to complete closing all network channels");
        }
        CommonSysLog.info(LOGGER, "channels closed, freeing cache storage");
        channelFactory.releaseExternalResources();
        CommonSysLog.info(LOGGER, "successfully shut down");
    }
    
    private NettyPackServer() {
	
    }
    
    public static NettyPackServer getInstance() {
	if(instance == null) {
	    instance = new NettyPackServer();
	}
	return instance;
    }
    
    /*
    public static void main(String[] args) throws Exception {
	NettyPackServer.getInstance().start();
    }
    */
}

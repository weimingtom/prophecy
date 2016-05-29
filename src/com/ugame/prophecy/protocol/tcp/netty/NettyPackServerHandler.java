package com.ugame.prophecy.protocol.tcp.netty;

import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalData;
import com.ugame.prophecy.protocol.pack.CommonPack;
import com.ugame.prophecy.protocol.tcp.PackBufferException;
import com.ugame.prophecy.protocol.tcp.PackContext;

public class NettyPackServerHandler extends SimpleChannelUpstreamHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyPackServerHandler.class);
    
    private transient PackContext conn;
    // up to 256 ^ 4 / 2 - 1
    private final static int BUFFER_MAX = 256 * 256;
    
    /**
     * 频道事件总路由器（？）
     */
    @Override
    public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent event) throws Exception {
        if (event instanceof ChannelStateEvent) {
            LOGGER.info(event.toString());
        }
        super.handleUpstream(ctx, event);
    }

    @Override
    public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent event)
	    throws Exception {
	try {
	    super.channelConnected(ctx, event);
	    final int idLogin = GlobalData.addSession();
	    //TODO:貌似这个类是有状态的，但不一定
	    conn = new PackContext(idLogin);
	} catch (Exception ex) {
	    LOGGER.error("NettyPackServerHandler channelConnected Exception:", 
		    ex.getCause());
	}
    }

    @Override
    public void channelDisconnected(final ChannelHandlerContext ctx, final ChannelStateEvent event) throws Exception {
	try {
	    GlobalData.removeSession();
	    super.channelDisconnected(ctx, event);
	} catch (Exception ex) {
	    LOGGER.error("PackServerHandler sessionClosed Exception:", 
		    ex.getCause());
	}
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent event) {
	try {
	    messageProcess(ctx, (ChannelBuffer) event.getMessage());
	} catch (NettyPackServerException ex) {
	    LOGGER.error("NettyPackServerException:", ex.getCause());
	} catch (PackBufferException ex) {
	    LOGGER.error("PackBufferException:", ex.getCause());
	} catch (UnsupportedEncodingException ex) {
	    LOGGER.error("UnsupportedEncodingException:", ex.getCause());
	}
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent event) {
        LOGGER.error("Unexpected exception from downstream.",
        	event.getCause());
        event.getChannel().close();
    }
    
    public void messageProcess(final ChannelHandlerContext ctx, final ChannelBuffer message) 
    	throws PackBufferException, UnsupportedEncodingException, NettyPackServerException {
	conn.decoderBuffer.putMessage(message);
	while (true) {
	    if (conn.decoderState == 0) {
		assert conn.decoderBuffer.getRpos() == 0;
		if (conn.decoderBuffer.available() >= 6) {
		    readHead(conn);
		} else {
		    break;
		}
	    } else if (conn.decoderState == 1) {
		assert conn.decoderBuffer.getRpos() == 6;
		if (conn.decoderBuffer.available() >= conn.decoderLength) {
		    //TODO:这里byte[]的生成可能不利于GC
		    final byte[] data = conn.decoderBuffer.getBytesByLength(
			    (int)conn.decoderLength);
		    doSecondProtocalLogic(data, conn);
		    conn.decoderState = 0;
		} else {
		    break;
		}
	    }
	}
    }

    public void readHead(final PackContext conn) throws PackBufferException, NettyPackServerException {
	byte head1 = 0;
	byte head2 = 0;
	head1 = conn.decoderBuffer.getByte();
	head2 = conn.decoderBuffer.getByte();
	if (head1 != 'C' || head2 != 'T') {
	    throw new NettyPackServerException(
		    "package head error: " +
		    "head1==" + head1 + ", " + 
		    "head2==" + head2);
	}
	conn.decoderLength = conn.decoderBuffer.getInt(); // 4 bytes
	if (conn.decoderLength < 0 || conn.decoderLength >= BUFFER_MAX) {
	    throw new NettyPackServerException(
		    "package length error: " +
		    "head1==" + head1 + ", " + 
		    "head2==" + head2 + ", " +
		    "decoderLength == " + conn.decoderLength);
	}
	conn.decoderState = 1;
    }

    public void doSecondProtocalLogic(final byte[] data,
	    final PackContext conn)
	    throws UnsupportedEncodingException {
	CommonPack.unpack(data, conn);
    }
}

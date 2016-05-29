package com.ugame.prophecy.protocol.tcp.yanf4j;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.yanf4j.buffer.IoBuffer;
import com.google.code.yanf4j.core.Session;
import com.google.code.yanf4j.core.impl.HandlerAdapter;

import com.ugame.prophecy.global.GlobalData;
import com.ugame.prophecy.protocol.pack.CommonPack;
import com.ugame.prophecy.protocol.tcp.PackBufferException;
import com.ugame.prophecy.protocol.tcp.PackContext;

public class Yanf4jPackServerHandler extends HandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Yanf4jPackServerHandler.class);
    
    // up to 256 ^ 4 / 2 - 1
    private final static int BUFFER_MAX = 256 * 256;
    
    @Override
    public void onExceptionCaught(Session session, Throwable throwable) {
	//throwable.printStackTrace();
	LOGGER.info(throwable.getMessage());
    }

    @Override
    public void onMessageSent(Session session, Object message) {
	//System.out.println("sent to "
	//	+ session.getRemoteSocketAddress());
    }

    @Override
    public void onSessionStarted(Session session) {
	//System.out.println("session started");
	//FIXME:
	session.setUseBlockingRead(false); 
	session.setUseBlockingWrite(false);
    }

    public void onSessionCreated(Session session) {
	//System.out.println(session.getRemoteSocketAddress().toString()
	//	+ " connected");
	final int idLogin = GlobalData.addSession();
	session.setAttribute(PackContext.ATTR_CONNECTION,
		new PackContext(idLogin));
    }

    public void onSessionClosed(Session session) {
	//System.out.println(session.getRemoteSocketAddress().toString()
	//	+ " disconnected");
	GlobalData.removeSession();
	session.removeAttribute(PackContext.ATTR_CONNECTION);
    }

    @Override
    public void onMessageReceived(final Session session, final Object message) {
	try {
	    messageProcess(session, message);
	} catch (Yanf4jPackServerException ex) {
	    LOGGER.error("Yanf4jPackServerException:", ex.getCause());
	    //ex.printStackTrace();
	} catch (PackBufferException ex) {
	    LOGGER.error("PackBufferException:", ex.getCause());
	    //ex.printStackTrace();
	} catch (UnsupportedEncodingException ex) {
	    LOGGER.error("UnsupportedEncodingException:", ex.getCause());
	}
    }
    
    public void messageProcess(final Session session, final Object message) 
	throws PackBufferException, UnsupportedEncodingException, Yanf4jPackServerException {
	final PackContext conn = (PackContext) session.getAttribute(
		PackContext.ATTR_CONNECTION);
	conn.decoderBuffer.putYanf4jBuffer((IoBuffer) message);
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

    public void readHead(final PackContext conn) throws PackBufferException, Yanf4jPackServerException {
	byte head1 = 0;
	byte head2 = 0;
	head1 = conn.decoderBuffer.getByte();
	head2 = conn.decoderBuffer.getByte();
	if (head1 != 'C' || head2 != 'T') {
	    throw new Yanf4jPackServerException(
		    "package head error: " +
		    "head1==" + head1 + ", " + 
		    "head2==" + head2);
	}
	conn.decoderLength = conn.decoderBuffer.getInt(); // 4 bytes
	if (conn.decoderLength < 0 || conn.decoderLength >= BUFFER_MAX) {
	    throw new Yanf4jPackServerException(
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

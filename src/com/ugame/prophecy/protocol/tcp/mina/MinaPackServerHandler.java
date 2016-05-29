package com.ugame.prophecy.protocol.tcp.mina;

import java.io.UnsupportedEncodingException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalData;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.http.HttpRequestDecoder;
import com.ugame.prophecy.protocol.http.HttpRequestMessage;
import com.ugame.prophecy.protocol.http.HttpResponseEncoder;
import com.ugame.prophecy.protocol.http.HttpResponseMessage;
import com.ugame.prophecy.protocol.pack.CommonPack;
import com.ugame.prophecy.protocol.tcp.PackBufferException;
import com.ugame.prophecy.protocol.tcp.PackContext;

/**
 * 包结构
 * Package structure: 
 * | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | ...
 * | Magic |    Length     |  ID   | Body

 * 1-2: Magic
 * CT (Client to server) 
 * SR (Server to client)
 * 3-6: Length
 * 7  : ModualID (byte, 1-255) or MODL_SERIALIZER(0xff) (need serializer) 
 * 8  : NetMsgID (byte, 1-255) or serializer type
 * 
 * 连接处理句柄
 * 使用上下文状态机处理粘包和分裂包
 */
public class MinaPackServerHandler extends IoHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinaPackServerHandler.class);

    // up to 256 ^ 4 / 2 - 1
    private final static int BUFFER_MAX = 256 * 256;

    @Override
    public void sessionOpened(final IoSession session) {
	session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 60 * 30);
    }

    @Override
    public void sessionCreated(final IoSession session) {
	try {
	    super.sessionCreated(session);
	    final int idLogin = GlobalData.addSession();
	    session.setAttribute(PackContext.ATTR_CONNECTION,
		    new PackContext(idLogin));
	} catch (Exception e) {
	    CommonSysLog.error(LOGGER, "sessionCreated Exception:", e);
	}
    }

    @Override
    public void sessionClosed(final IoSession session) {
	try {
	    GlobalData.removeSession();
	    //FIXME: 未显式释放PackContext对象的内存
	    session.removeAttribute(PackContext.ATTR_CONNECTION);
	    super.sessionClosed(session);
	} catch (Exception e) {
	    CommonSysLog.error(LOGGER, "sessionClosed Exception:", e);
	}
    }

    @Override
    public void exceptionCaught(final IoSession session, final Throwable cause) {
	CommonSysLog.error(LOGGER, "exceptionCaught Exception:", cause);
	session.close(true);
    }

    /**
     * 异常在这里集中处理
     * 注意异常可能会破坏PackConnection这个状态机对象
     */
    @Override
    public void messageReceived(final IoSession session, final Object message) {
	try {
	    messageProcess(session, (IoBuffer) message);
	} catch (MinaServerException e) {
	    CommonSysLog.error(LOGGER, "MinaServerException:", e);
	} catch (PackBufferException e) {
	    CommonSysLog.error(LOGGER, "PackBufferException:", e);
	} catch (UnsupportedEncodingException e) {
	    CommonSysLog.error(LOGGER, "UnsupportedEncodingException:", e);
	}
    }

    public void messageProcess(final IoSession session, final IoBuffer message)
	    throws MinaServerException, PackBufferException, UnsupportedEncodingException {
	final PackContext conn = (PackContext) session.getAttribute(
		PackContext.ATTR_CONNECTION);
	conn.decoderBuffer.putBuffer(message);
	while (true) {
	    if(conn.protocol == PackContext.UNKNOWN) {
		//TODO:NO EXCEPTION READ
		int result = readProtocol(conn);
		if(result == PackContext.NOT_OK) {
		    break;
		}
		conn.protocol = result;
	    }
	    if(conn.protocol == PackContext.PACK) {
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
        		conn.protocol = PackContext.UNKNOWN;
    	    	    } else {
        		break;
    	    	    }
    	    	}
	    } else if (conn.protocol == PackContext.HTTP) {
		final HttpRequestDecoder decoder = conn.httpDecoder;
		//TODO:因为前面PackBuffer的干扰，所以不能直接用IoBuffer参数message
		final IoBuffer buffer = (IoBuffer) conn.decoderBuffer.getBuffer();
		if(decoder.messageComplete(buffer)) {
		    final HttpRequestMessage requestMessage = decoder.decodeBody(buffer);
		    if(requestMessage != null) {
			CommonSysLog.output("[HTTP]" + requestMessage.toString());
			final HttpResponseMessage response = new HttpResponseMessage();
			response.setContentType("text/plain");
			response.setResponseCode(HttpResponseMessage.SUCCESS);
			response.appendBody("CONNECTED");
			if (response != null) {
			    final HttpResponseEncoder encoder = conn.httpEncoder;
			    encoder.encode(session, response);
			    conn.protocol = PackContext.UNKNOWN;
			    //FIXME:服务器不断开会好些？
			    //session.close(true);
			}
		    }
		}
	    }
	}
    }
    
    public int readProtocol(final PackContext conn) throws PackBufferException {
	int ret = PackContext.PACK;
	/**
	 * TODO:NO EXCEPTION READ
	 */
	if(conn.decoderBuffer.available() < 2) {
	    ret = PackContext.NOT_OK;
	} else {
	    int rpos = conn.decoderBuffer.getRpos();
	    Byte b1 = conn.decoderBuffer.getByte();
	    Byte b2 = conn.decoderBuffer.getByte();
	    //TODO:rewind
	    conn.decoderBuffer.setRpos(rpos);
	    //TODO:get magic number
	    if((b1 == 'G' && b2 == 'E') ||
	       (b1 == 'P' && b2 == 'O') ) {
		ret = PackContext.HTTP;
	    }
	}
	return ret;
    }

    public void readHead(final PackContext conn) throws PackBufferException, MinaServerException {
	byte head1 = 0;
	byte head2 = 0;
	head1 = conn.decoderBuffer.getByte();
	head2 = conn.decoderBuffer.getByte();
	if (head1 != 'C' || head2 != 'T') {
	    throw new MinaServerException(
		    "package head error: " +
		    "head1==" + head1 + ", " + 
		    "head2==" + head2);
	}
	conn.decoderLength = conn.decoderBuffer.getInt(); // 4 bytes
	if (conn.decoderLength < 0 || conn.decoderLength >= BUFFER_MAX) {
	    throw new MinaServerException(
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

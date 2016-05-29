package com.ugame.prophecy.protocol.tcp.cindy;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalData;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.pack.CommonPack;
import com.ugame.prophecy.protocol.tcp.PackBufferException;
import com.ugame.prophecy.protocol.tcp.PackContext;

import net.sf.cindy.Session;
import net.sf.cindy.SessionHandlerAdapter;

/**
 * FIXME:出现空指针异常
 * @author Administrator
 *
 */
public class CindyPackServerHandler extends SessionHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CindyPackServerHandler.class);
    
    // up to 256 ^ 4 / 2 - 1
    private final static int BUFFER_MAX = 256 * 256;
    
    //TODO:是否使用会话属性（均可）
    private final static boolean USE_ATTR = true;
    
    //private PackContext conn;    
    //private final ThreadLocal contextCache = new ThreadLocal();
    private PackContext contextCatch2 = null;
    
    /*
    private void setContext(PackContext context) {
	contextCache.set(context);
    }
    */
    
    /**
     * TODO：在这里处理sessionStarted的逻辑
     * （虽然和sessionStarted不一定等效）
     * @return
     */
    /*
    private PackContext getContext() {
	PackContext context = (PackContext) contextCache.get();
	if(context == null) {
	    final int idLogin = GlobalData.addSession();
	    context = new PackContext(idLogin);
	    setContext(context);
	}
	return context;
    }
    */
    
    private void setContext2(PackContext context) {
	contextCatch2 = context;
    }
    
    /**
     * TODO：在这里处理sessionStarted的逻辑
     * （虽然和sessionStarted不一定等效）
     * @return
     */
    private PackContext getContext2() {
	PackContext context = contextCatch2;
	if(context == null) {
	    final int idLogin = GlobalData.addSession();
	    context = new PackContext(idLogin);
	    setContext2(context);
	}
	return context;
    }
    
    @Override
    public void exceptionCaught(final Session session, final Throwable exception) {
	CommonSysLog.error(LOGGER, "exceptionCaught ", exception);
    }
    
    /**
     * FIXME: 好像在出现大量短连接时，有空指针错误，
     * 导致这个回调不安全（可能不被调用），
     * 不要在这里放逻辑处理！
     * 见getContext
     */
    /*
    @Override
    public void sessionStarted(final Session session) throws Exception {
	CommonSysLog.info(LOGGER, "sessionStarted");
	try {
	    //super.sessionStarted(session);
	    final int idLogin = GlobalData.addSession();
	    //conn = new PackContext(idLogin);
	    //session.setAttribute(PackContext.ATTR_CONNECTION,
	    //	    new PackContext(idLogin));
	    setContext(new PackContext(idLogin));
	} catch (Exception e) {
	    CommonSysLog.error(LOGGER, "CindyPackServerHandler sessionCreated Exception:", e);
	}
    }
    */
    
    @Override
    public void sessionClosed(final Session session) throws Exception {
	CommonSysLog.info(LOGGER, "sessionClosed");
	try {
	    GlobalData.removeSession();
	    //（已过时）TODO:回收会话的状态对象内存
	    //session.setAttribute(PackContext.ATTR_CONNECTION, null);
	    //TODO:注意这里必须置空，
	    //否则getContext的单实例模式失效
	    if (USE_ATTR) {
		session.removeAttribute(PackContext.ATTR_CONNECTION);
	    } else {
		setContext2(null);
	    }
	    super.sessionClosed(session);
	} catch (Exception e) {
	    CommonSysLog.error(LOGGER, "CindyPackServerHandler sessionClosed Exception:", e);
	}
    }
    
    @Override
    public void objectReceived(final Session session, final Object buffer) throws Exception {
	//Buffer buffer = (Buffer) obj;
	//LOG.info("objectReceived " + 
	//	buffer.remaining() + " bytes.");
	try {
	    messageProcess(session, (ByteBuffer) buffer);
	} catch (CindyServerException e) {
	    CommonSysLog.error(LOGGER, "CindyServerException:", e);
	} catch (PackBufferException e) {
	    CommonSysLog.error(LOGGER, "PackBufferException:", e);
	} catch (UnsupportedEncodingException e) {
	    CommonSysLog.error(LOGGER, "UnsupportedEncodingException:", e);
	}
    }
    
    public void messageProcess(final Session session, final ByteBuffer message) throws CindyServerException, PackBufferException, UnsupportedEncodingException {
	PackContext conn; 
	if (USE_ATTR) {
	    conn = (PackContext) session.getAttribute(
		PackContext.ATTR_CONNECTION);
	    if (conn == null) {
		final int idLogin = GlobalData.addSession();
		conn = new PackContext(idLogin);
		session.setAttribute(PackContext.ATTR_CONNECTION, conn);
	    }
	} else {
	    conn = getContext2();
	}
	conn.decoderBuffer.putCindyBuffer(message);
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

    public void readHead(PackContext conn) 
    	throws PackBufferException, CindyServerException {
	byte head1 = 0;
	byte head2 = 0;
	head1 = conn.decoderBuffer.getByte();
	head2 = conn.decoderBuffer.getByte();
	if (head1 != 'C' || head2 != 'T') {
	    throw new CindyServerException(
		    "package head error: " +
		    "head1==" + head1 + ", " + 
		    "head2==" + head2);
	}
	conn.decoderLength = conn.decoderBuffer.getInt(); // 4 bytes
	if (conn.decoderLength < 0 || conn.decoderLength >= BUFFER_MAX) {
	    throw new CindyServerException(
		    "package length error: " +
		    "head1==" + head1 + ", " + 
		    "head2==" + head2 + ", " +
		    "decoderLength == " + conn.decoderLength);
	}
	conn.decoderState = 1;
    }

    public void doSecondProtocalLogic(final byte[] data,
	    final PackContext conn)throws UnsupportedEncodingException {
	CommonPack.unpack(data, conn);
    }
}
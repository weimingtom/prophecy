package com.ugame.prophecy.protocol.tcp.xnet;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalData;
import com.ugame.prophecy.protocol.pack.CommonPack;
import com.ugame.prophecy.protocol.tcp.PackBufferException;
import com.ugame.prophecy.protocol.tcp.PackContext;

import xnet.core.server.Session;
import xnet.core.util.IOBuffer;

/**
 * XNet接口代码
 * @see http://xnet.googlecode.com/svn/trunk
 * @see http://mybridge.googlecode.com/svn/trunk
 * @author Administrator
 *
 */
public class XnetPackSession extends Session {
    private static final Logger LOGGER = LoggerFactory.getLogger(XnetPackSession.class);
    
    private transient PackContext conn;
    //private final ThreadLocal contextCache = new ThreadLocal();
    
    // up to 256 ^ 4 / 2 - 1
    private final static int BUFFER_MAX = 256 * 256;
    
    // TODO:实际上xnet可以限制输入数据的大小（否则等待），
    // 但这里没有用（直接处理complateReadOnce）
    // 如果执行remainToRead(BUF_SIZE);
    // 则输入缓冲区会被清空
    static final int BUF_SIZE = 1024;
    
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
    
    /**
     * 超时
     */
    @Override
    public void timeout(IOBuffer readBuf, IOBuffer writeBuf) throws Exception {
	super.timeout(readBuf, writeBuf);
    }

    /**
     * 会话开始
     */
    @Override
    public void open(IOBuffer readBuf, IOBuffer writeBuf) throws Exception {
	remainToRead(BUF_SIZE);
	final int idLogin = GlobalData.addSession();
	//TODO:貌似这个类是有状态的，但不一定
	conn = new PackContext(idLogin);
	//getContext();
    }

    /**
     * 会话关闭
     */
    @Override
    public void close() {
	GlobalData.removeSession();
	conn = null;
	//setContext(null);
	super.close();
    }

    /**
     * 未完成读
     */
    @Override
    public void complateReadOnce(IOBuffer readBuf, IOBuffer writeBuf)
	    throws Exception {
	//super.complateReadOnce(readBuf, writeBuf);
	complateRead(readBuf, writeBuf);
    }

    /**
     * 已完成读
     */
    @Override
    public void complateRead(IOBuffer readBuf, IOBuffer writeBuf) {
	//System.out.println("read:" + readBuf.position());
	/*byte b1 = readBuf.getByte();
	byte b2 = readBuf.getByte();
	System.out.println(b1 + "," + b2);
	*/
	try {
	    messageProcess(readBuf);
	} catch (UnsupportedEncodingException ex) {
	    LOGGER.error("UnsupportedEncodingException:", ex.getCause());
	} catch (PackBufferException ex) {
	    LOGGER.error("PackBufferException:", ex.getCause());
	} catch (XnetPackServerException ex) {
	    LOGGER.error("XnetPackServerException:", ex.getCause());
	    ex.printStackTrace();
	} finally {
	    //
	    //remainToRead(BUF_SIZE);
	}
    }

    /**
     * 已完成写
     */
    @Override
    public void complateWrite(IOBuffer readBuf, IOBuffer writeBuf)
	    throws Exception {
	readBuf.position(0);
	remainToRead(BUF_SIZE);
    }

    public void messageProcess(final IOBuffer message) throws PackBufferException,
	    UnsupportedEncodingException, XnetPackServerException {
	//PackContext conn = getContext();
	conn.decoderBuffer.putXnetIOBuffer(message);
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
		    // TODO:这里byte[]的生成可能不利于GC
		    final byte[] data = conn.decoderBuffer
			    .getBytesByLength((int) conn.decoderLength);
		    doSecondProtocalLogic(data, conn);
		    conn.decoderState = 0;
		} else {
		    break;
		}
	    }
	}
    }

    public void readHead(final PackContext conn) throws PackBufferException,
    	XnetPackServerException {
	byte head1 = 0;
	byte head2 = 0;
	head1 = conn.decoderBuffer.getByte();
	head2 = conn.decoderBuffer.getByte();
	if (head1 != 'C' || head2 != 'T') {
	    throw new XnetPackServerException("package head error: "
		    + "head1==" + head1 + ", " + "head2==" + head2);
	}
	conn.decoderLength = conn.decoderBuffer.getInt(); // 4 bytes
	if (conn.decoderLength < 0 || conn.decoderLength >= BUFFER_MAX) {
	    throw new XnetPackServerException("package length error: "
		    + "head1==" + head1 + ", " + "head2==" + head2 + ", "
		    + "decoderLength == " + conn.decoderLength);
	}
	conn.decoderState = 1;
    }

    public void doSecondProtocalLogic(final byte[] data, final PackContext conn)
	    throws UnsupportedEncodingException {
	CommonPack.unpack(data, conn);
    }
}

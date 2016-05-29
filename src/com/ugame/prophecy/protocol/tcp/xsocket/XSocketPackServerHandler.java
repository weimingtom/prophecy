package com.ugame.prophecy.protocol.tcp.xsocket;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.channels.ClosedChannelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.connection.IConnectHandler;
import org.xsocket.connection.IDataHandler;
import org.xsocket.connection.IDisconnectHandler;
import org.xsocket.connection.INonBlockingConnection;

import com.ugame.prophecy.global.GlobalData;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.pack.CommonPack;
import com.ugame.prophecy.protocol.tcp.PackBufferException;
import com.ugame.prophecy.protocol.tcp.PackContext;

public class XSocketPackServerHandler implements IDataHandler, IConnectHandler,
	IDisconnectHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(XSocketPackServerHandler.class);

    //private PackContext conn;
    // up to 256 ^ 4 / 2 - 1
    private final static int BUFFER_MAX = 256 * 256;

    @Override
    public boolean onConnect(final INonBlockingConnection nbc) throws IOException,
	    BufferUnderflowException, MaxReadSizeExceededException {
	CommonSysLog.info(LOGGER, "onConnect " + nbc.getRemoteAddress() + ":" + nbc.getRemotePort());
	try {
	    final int idLogin = GlobalData.addSession();
	    nbc.setAttachment(new PackContext(idLogin));
	} catch (Exception ex) {
	    CommonSysLog.error(LOGGER, "XSocketPackServerHandler onConnect Exception:", ex);
	}
	return true;
    }

    @Override
    public boolean onDisconnect(final INonBlockingConnection nbc) throws IOException {
	CommonSysLog.info(LOGGER, "onDisconnect " + nbc.getRemoteAddress() + ":" + nbc.getRemotePort());
	try {
	    //FIXME:回收状态对象内存
	    nbc.setAttachment(null);
	    GlobalData.removeSession();
	} catch (Exception ex) {
	    CommonSysLog.error(LOGGER, "XSocketPackServerHandler onDisconnect Exception:", ex);
	}
	return true;
    }

    @Override
    public boolean onData(final INonBlockingConnection connection)
	    throws IOException, BufferUnderflowException,
	    ClosedChannelException, MaxReadSizeExceededException {
	CommonSysLog.info(LOGGER, "connection.available() : " + connection.available() + " bytes.");
	boolean ret;
	if(connection.available() < 0) {
	    ret = false;
	} else {
	    try {
    	    	messageProcess(connection);
	    } catch (XSocketPackServerException ex) {
		CommonSysLog.error(LOGGER, "XSocketPackServerException:", ex);
	    } catch (PackBufferException ex) {
		CommonSysLog.error(LOGGER, "PackBufferException:", ex);
	    } catch (UnsupportedEncodingException ex) {
		CommonSysLog.error(LOGGER, "UnsupportedEncodingException:", ex);
	    }
	    ret = true;
	}
	return ret;
    }

    public void messageProcess(final INonBlockingConnection connection)
	    throws PackBufferException, XSocketPackServerException, IOException {
	final PackContext conn = (PackContext) connection.getAttachment();
	conn.decoderBuffer.putConn(connection);
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
    	XSocketPackServerException {
	byte head1 = 0;
	byte head2 = 0;
	head1 = conn.decoderBuffer.getByte();
	head2 = conn.decoderBuffer.getByte();
	if (head1 != 'C' || head2 != 'T') {
	    throw new XSocketPackServerException("package head error: "
		    + "head1==" + head1 + ", " + "head2==" + head2);
	}
	conn.decoderLength = conn.decoderBuffer.getInt(); // 4 bytes
	if (conn.decoderLength < 0 || conn.decoderLength >= BUFFER_MAX) {
	    throw new XSocketPackServerException("package length error: "
		    + "head1==" + head1 + ", " + "head2==" + head2 + ", "
		    + "decoderLength == " + conn.decoderLength);
	}
	conn.decoderState = 1;
    }

    public void doSecondProtocalLogic(final byte[] data,
	    final PackContext conn)
	    throws UnsupportedEncodingException {
	CommonPack.unpack(data, conn);
    }
}

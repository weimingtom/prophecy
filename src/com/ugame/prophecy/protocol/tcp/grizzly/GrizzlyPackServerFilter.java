package com.ugame.prophecy.protocol.tcp.grizzly;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalData;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.pack.CommonPack;
import com.ugame.prophecy.protocol.tcp.PackBufferException;
import com.ugame.prophecy.protocol.tcp.PackContext;

public class GrizzlyPackServerFilter extends BaseFilter {
    private static final Logger LOGGER = LoggerFactory
	    .getLogger(GrizzlyPackServerFilter.class);

    // up to 256 ^ 4 / 2 - 1
    private final static int BUFFER_MAX = 256 * 256;

    private transient Attribute<PackContext> connAttribute = Grizzly.DEFAULT_ATTRIBUTE_BUILDER
	    .createAttribute(PackContext.ATTR_CONNECTION);

    public GrizzlyPackServerFilter() {

    }

    /**
     * FIXME:
     * 官方例子同时使用handleAccept和handleConnect添加Attribute
     * 可能是因为handleConnect为客户端版专用句柄，
     * 而handleAccept才是服务器版专用处理句柄。
     */
    @Override
    public NextAction handleAccept(FilterChainContext ctx) throws IOException {
	final int idLogin = GlobalData.addSession();
	connAttribute.set(ctx.getConnection(), new PackContext(idLogin));
	return ctx.getInvokeAction();
    }
    
    @Override
    public NextAction handleClose(FilterChainContext ctx) throws IOException {
	GlobalData.removeSession();
	connAttribute.remove(ctx.getConnection());
	return super.handleClose(ctx);
    }

    @Override
    public void exceptionOccurred(FilterChainContext ctx, Throwable error) {
	super.exceptionOccurred(ctx, error);
	CommonSysLog.error(LOGGER, "exceptionOccurred Exception:", error);
    }

    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
	try {
	    final Buffer message = ctx.getMessage();
	    messageProcess(ctx, message);
	} catch (GrizzlyServerException e) {
	    CommonSysLog.error(LOGGER, "GrizzlyServerException:", e);
	} catch (PackBufferException e) {
	    CommonSysLog.error(LOGGER, "PackBufferException:", e);
	} catch (UnsupportedEncodingException e) {
	    CommonSysLog.error(LOGGER, "UnsupportedEncodingException:", e);
	}

	return ctx.getStopAction();
    }

    public void messageProcess(final FilterChainContext ctx,
	    final Buffer message) throws GrizzlyServerException,
	    PackBufferException, UnsupportedEncodingException {
	PackContext conn = connAttribute.get(ctx.getConnection());
	conn.decoderBuffer.putGrizzlyBuffer(message);
	while (true) {
	    if (conn.protocol == PackContext.UNKNOWN) {
		// TODO:NO EXCEPTION READ
		int result = readProtocol(conn);
		if (result == PackContext.NOT_OK) {
		    break;
		}
		conn.protocol = result;
	    }
	    if (conn.protocol == PackContext.PACK) {
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
			conn.protocol = PackContext.UNKNOWN;
		    } else {
			break;
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
	if (conn.decoderBuffer.available() < 2) {
	    ret = PackContext.NOT_OK;
	} else {
	    int rpos = conn.decoderBuffer.getRpos();
	    Byte b1 = conn.decoderBuffer.getByte();
	    Byte b2 = conn.decoderBuffer.getByte();
	    // TODO:rewind
	    conn.decoderBuffer.setRpos(rpos);
	    // TODO:get magic number
	    // FIXME: HTTP协议未实现
	    if ((b1 == 'G' && b2 == 'E') || (b1 == 'P' && b2 == 'O')) {
		ret = PackContext.HTTP;
	    }
	}
	return ret;
    }

    public void readHead(final PackContext conn) throws PackBufferException,
	    GrizzlyServerException {
	byte head1 = 0;
	byte head2 = 0;
	head1 = conn.decoderBuffer.getByte();
	head2 = conn.decoderBuffer.getByte();
	if (head1 != 'C' || head2 != 'T') {
	    throw new GrizzlyServerException("package head error: " + "head1=="
		    + head1 + ", " + "head2==" + head2);
	}
	conn.decoderLength = conn.decoderBuffer.getInt(); // 4 bytes
	if (conn.decoderLength < 0 || conn.decoderLength >= BUFFER_MAX) {
	    throw new GrizzlyServerException("package length error: "
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

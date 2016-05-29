package com.ugame.prophecy.protocol.tcp.javanio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalData;
import com.ugame.prophecy.protocol.pack.CommonPack;
import com.ugame.prophecy.protocol.tcp.PackBufferException;
import com.ugame.prophecy.protocol.tcp.PackContext;

public class JavaNIOPackThread extends Thread {
    private static final Logger LOGGER = LoggerFactory
	    .getLogger(JavaNIOPackThread.class);

    // up to 256 ^ 4 / 2 - 1
    private final static int BUFFER_MAX = 256 * 256;
    private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_MAX);
    private ServerSocketChannel server;
    private Selector selector;

    public JavaNIOPackThread(Selector selector, ServerSocketChannel server) {
	this.selector = selector;
	this.server = server;
    }

    @Override
    public void run() {
	SelectionKey serverkey;
	try {
	    serverkey = server.register(selector, SelectionKey.OP_ACCEPT);
	    while (true) {
		try {
		    selector.select();
		} catch (IOException ex) {
		    LOGGER.error("select error:", ex.getCause());
		    break;
		}
		Set<SelectionKey> keys = selector.selectedKeys();
		for (Iterator<SelectionKey> i = keys.iterator(); i.hasNext();) {
		    SelectionKey key = (SelectionKey) i.next();
		    i.remove();
		    if (key == serverkey) {
			if (key.isAcceptable()) {
			    SocketChannel client;
			    try {
				client = server.accept();
				client.configureBlocking(false);
				//关闭纳格算法
				client.socket().setTcpNoDelay(true);
				SelectionKey clientkey = client.register(
					selector, SelectionKey.OP_READ);
				final int idLogin = GlobalData.addSession();
				final PackContext conn = new PackContext(
					idLogin);
				clientkey.attach(conn);
			    } catch (IOException e) {
				LOGGER.error("server setup error:", e.getCause());
			    }
			}
		    } else {
			SocketChannel client = (SocketChannel) key.channel();
			int bytesread = -1;
			try {
			    bytesread = client.read(buffer);
			} catch (IOException e1) {
			    LOGGER.error("client read error:", e1.getCause());
			    bytesread = -1;
			}
			if (bytesread == -1) {
			    GlobalData.removeSession();
			    key.attach(null);
			    key.cancel();
			    try {
				client.close();
			    } catch (IOException e) {
				e.printStackTrace();
			    }
			    continue;
			}
			try {
			    buffer.flip();
			    final PackContext conn = ((PackContext) key
					.attachment());
			    messageProcess(conn, buffer);
			    buffer.clear();
			} catch (PackBufferException ex) {
			    LOGGER.error("PackBufferException:", ex.getCause());
			} catch (JavaNIOServerException ex) {
			    LOGGER.error("JavaNIOServerException:", ex
				    .getCause());
			    ex.printStackTrace();
			} catch (UnsupportedEncodingException ex) {
			    LOGGER.error("UnsupportedEncodingException:", ex
				    .getCause());
			}
		    }
		}
	    }
	} catch (ClosedChannelException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void messageProcess(PackContext conn, final ByteBuffer buffer)
	    throws PackBufferException, UnsupportedEncodingException,
	    JavaNIOServerException {
	conn.decoderBuffer.putCindyBuffer(buffer);
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
	    JavaNIOServerException {
	byte head1 = 0;
	byte head2 = 0;
	head1 = conn.decoderBuffer.getByte();
	head2 = conn.decoderBuffer.getByte();
	if (head1 != 'C' || head2 != 'T') {
	    throw new JavaNIOServerException("package head error: " + "head1=="
		    + head1 + ", " + "head2==" + head2);
	}
	conn.decoderLength = conn.decoderBuffer.getInt(); // 4 bytes
	if (conn.decoderLength < 0 || conn.decoderLength >= BUFFER_MAX) {
	    throw new JavaNIOServerException("package length error: "
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

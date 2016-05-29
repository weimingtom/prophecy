package com.ugame.prophecy.protocol.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.mina.core.buffer.IoBuffer;
import org.glassfish.grizzly.Buffer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.xsocket.connection.INonBlockingConnection;
import xnet.core.util.IOBuffer;
//import com.google.code.yanf4j.buffer.IoBuffer;

/**
 * 用于在连接上下文中缓存接收包
 * 以防止粘包和分裂包
 * @see http://mina.apache.org/sshd/
 */
public class PackBuffer {
    private final static char[] digits = { 
	'0', '1', '2', '3', '4', '5', '6', '7', 
	'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' 
    };

    public static final int DEFAULT_SIZE = 256;
    transient private byte[] data;
    transient private int rpos;
    transient private int wpos;

    public PackBuffer() {
	final int len = getNextPowerOf2(DEFAULT_SIZE);
	this.data = new byte[len];
	this.rpos = 0;
	this.wpos = 0;
    }
    
    private void ensureCapacity(final int capacity) {
	if (data.length - wpos < capacity) {
	    final int capw = wpos + capacity;
	    final byte[] tmp = new byte[getNextPowerOf2(capw)];
	    System.arraycopy(data, 0, tmp, 0, data.length);
	    data = tmp;
	}
    }

    private static int getNextPowerOf2(final int value) {
	int output = 1;
	while (output < value) {
	    output <<= 1;
	}
	return output;
    }
    
    //------------------------------
    //TODO: 适配不同种类的buffer
    
    public void putBuffer(final IoBuffer buffer) {
	final int length = buffer.remaining();
	ensureCapacity(length);
	buffer.get(data, wpos, length);
	wpos += length;
    }
        
    public IoBuffer getBuffer() {
	IoBuffer buffer = IoBuffer.allocate(this.available());
	buffer.put(getAllBytes());
	buffer.rewind();
	return buffer;
    }
    
    public void putMessage(final ChannelBuffer message) {
	final int length = message.readableBytes();
	ensureCapacity(length);
	message.getBytes(0, data, wpos, length);
	wpos += length;	
    }
    
    public void putConn(final INonBlockingConnection connection) throws IOException {
	int length;
	length = connection.available();
	ensureCapacity(length);
	final ByteBuffer copyBuffer = ByteBuffer.wrap(data, wpos, length);
	connection.read(copyBuffer);
	wpos += length;
    }
    
    public void putCindyBuffer(final ByteBuffer buffer){
	final int length = buffer.remaining();
	ensureCapacity(length);
	buffer.get(data, wpos, length);
	wpos += length;
    }
    
    public void putGrizzlyBuffer(final Buffer buffer) {
	final int length = buffer.remaining();
	ensureCapacity(length);
	buffer.get(data, wpos, length);
	wpos += length;
    }
    
    public void putXnetIOBuffer(final IOBuffer buffer) {
	/*
	错误读法：
	（因为remaining()不是此次读入长度，而是等于最大容量limit()）
	buffer.position(0);
	putCindyBuffer(buffer.getBuf());
	安全的读法，但浪费byte[]：
	byte[] bytes = buffer.getBytes(0, buffer.limit());
	（或使用buffer.readBytes）
	*/
	/**
	 * TODO:
	 * 以下代码需要参考Xnet的代码，
	 * buffer.getBuf()取出的ByteBuffer其实就是
	 * IOBuffer的底层实现。
	 * 注意buf.limit由Session.remainToRead指定，
	 * 所以是定值。
	 * 而buffer.remaining()则是buf.limit() - buf.position()
	 * 真正读入的长度应该是position()而非remaining()
	 */
	ByteBuffer buf = buffer.getBuf();
	final int length = buf.position();
	//System.out.println("putXnetIOBuffer " + length);
	ensureCapacity(length);
	buf.position(0);
	buf.get(data, wpos, length);
	wpos += length;
	/**
	 * TODO:恢复到没有写入数据前的状态。
	 * PackBuffer的机制要求
	 * 网络框架不能缓冲任何
	 * 已经被写到PackBuffer的数据
	 */
	buf.clear();
    }
    
    public void putYanf4jBuffer(final com.google.code.yanf4j.buffer.IoBuffer buffer) {
	final int length = buffer.remaining();
	ensureCapacity(length);
	buffer.get(data, wpos, length);
	wpos += length;
    }
    
    //--------------------------------------------
    
    @Override
    public String toString() {
	return "Buffer [rpos=" + rpos + ", wpos=" + wpos + ", size="
		+ data.length + "]";
    }

    public void setRpos(int value) {
	rpos = value;
    }
    
    public int getRpos() {
	return rpos;
    }

    public int available() {
	return wpos - rpos;
    }

    public void clear() {
	rpos = 0;
	wpos = 0;
    }

    public byte getByte() throws PackBufferException {
	ensureAvailable(1);
	return data[rpos++];
    }

    public int getInt() throws PackBufferException {
	ensureAvailable(4);
	return (int)(((data[rpos++] << 24) & 0xff000000L)
		| ((data[rpos++] << 16) & 0x00ff0000L)
		| ((data[rpos++] << 8) & 0x0000ff00L)
		| ((data[rpos++]) & 0x000000ffL));
    }
    
    public byte[] getBytesByLength(final int length) throws PackBufferException {
	ensureAvailable(length);
	final byte[] buf = new byte[length];
	System.arraycopy(data, rpos, buf, 0, length);
	rpos += length;
	return buf;
    }

    public byte[] getAllBytes() {
	int length = available();
	final byte[] buf = new byte[length];
	System.arraycopy(data, rpos, buf, 0, length);
	rpos += length;
	return buf;
    }
    
    private void ensureAvailable(final int length) throws PackBufferException {
	if (available() < length) {
	    throw new PackBufferException("PackBuffer Underflow : " +
		    "ensureAvailable :" + rpos + "," + wpos + "," +
		    available() + "," + printHex(data));
	}
    }

    public static String printHex(final byte[] array) {
	return printHex(array, 0, array.length);
    }

    public static String printStringDate() {
	final Date currentTime = new Date();
	final SimpleDateFormat formatter = new SimpleDateFormat(
		"yyyy-MM-dd HH:mm:ss", Locale.CHINA);
	return formatter.format(currentTime);
    }

    public static String printHex(final byte[] array, final int offset,
	    final int len) {
	final StringBuilder sbuf = new StringBuilder();
	for (int i = 0; i < len; i++) {
	    final byte bval = array[offset + i];
	    if (i % 16 == 0 && i != 0) {
		sbuf.append('\n');
	    } else if (i % 16 != 0) {
		sbuf.append(' ');
	    }
	    if (i % 16 == 0) {
		sbuf.append("0x");
		sbuf.append(digits[(i >> 32) & 0x0F]);
		sbuf.append(digits[(i >> 28) & 0x0F]);
		sbuf.append(digits[(i >> 24) & 0x0F]);
		sbuf.append(digits[(i >> 20) & 0x0F]);
		sbuf.append(digits[(i >> 16) & 0x0F]);
		sbuf.append(digits[(i >> 12) & 0x0F]);
		sbuf.append(digits[(i >> 8) & 0x0F]);
		sbuf.append(digits[(i >> 4) & 0x0F]);
		sbuf.append(digits[(i >> 0) & 0x0F]);
		sbuf.append(" : ");
	    }
	    sbuf.append(digits[(bval >> 4) & 0x0F]);
	    sbuf.append(digits[bval & 0x0F]);
	}
	return sbuf.toString();
    }
}

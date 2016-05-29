package com.ugame.prophecy.mq.simple;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @see http://www.java2s.com/CN/Tutorial/Java/0160__Thread/ProducerandconsumerbasedonReadableByteChannelandWritableByteChannel.htm
 * @author Administrator
 *
 */
public class ByteChannelProducerConsumers {
    public static void main(String[] args) throws IOException {
	Pipe pipe = Pipe.open();
	WritableByteChannel out = pipe.sink();
	ReadableByteChannel in = pipe.source();
	ByteChannelNumberProducer producer = new ByteChannelNumberProducer(out, 200);
	ByteChannelNumberConsumer consumer = new ByteChannelNumberConsumer(in);
	producer.start();
	consumer.start();
    }
}

class ByteChannelNumberConsumer extends Thread {
    private ReadableByteChannel in;

    public ByteChannelNumberConsumer(ReadableByteChannel in) {
	this.in = in;
    }

    @Override
    public void run() {
	ByteBuffer sizeb = ByteBuffer.allocate(4);
	try {
	    while (sizeb.hasRemaining())
		in.read(sizeb);
	    sizeb.flip();
	    int howMany = sizeb.getInt();
	    sizeb.clear();
	    for (int i = 0; i < howMany; i++) {
		while (sizeb.hasRemaining()) {
		    in.read(sizeb);
		}
		sizeb.flip();
		int length = sizeb.getInt();
		sizeb.clear();
		ByteBuffer data = ByteBuffer.allocate(length);
		while (data.hasRemaining()) {
		    in.read(data);
		}
		BigInteger result = new BigInteger(data.array());
		System.out.println(result);
	    }
	} catch (IOException ex) {
	    System.err.println(ex);
	} finally {
	    try {
		in.close();
	    } catch (Exception ex) {
		// We tried
	    }
	}
    }
}

class ByteChannelNumberProducer extends Thread {
    private WritableByteChannel out;
    private int howMany;

    public ByteChannelNumberProducer(WritableByteChannel out, int howMany) {
	this.out = out;
	this.howMany = howMany;
    }

    @Override
    public void run() {
	try {
	    ByteBuffer buffer = ByteBuffer.allocate(4);
	    buffer.putInt(this.howMany);
	    buffer.flip();
	    while (buffer.hasRemaining()) {
		out.write(buffer);
	    }
	    for (int i = 0; i < howMany; i++) {
		byte[] data = new BigInteger(Integer.toString(i)).toByteArray();
		buffer = ByteBuffer.allocate(4 + data.length);
		buffer.putInt(data.length);
		buffer.put(data);
		buffer.flip();
		while (buffer.hasRemaining()) {
		    out.write(buffer);
		}
	    }
	    out.close();
	    System.err.println("Closed");
	} catch (IOException ex) {
	    System.err.println(ex);
	}
    }
}

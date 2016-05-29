package com.ugame.prophecy.serializer.amf3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.Deflater;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Output;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class AMF3Encoder implements ProtocolEncoder {

    private static int cachesize = 1024;
    private final AttributeKey DEFLATER = new AttributeKey(getClass(),
	    "deflater");
    private final SerializationContext context = new SerializationContext();
    private final Amf3Output amf3out;

    private final static boolean IS_COMPRESS = false;
    
    public AMF3Encoder() {
	// TODO Auto-generated constructor stub
	amf3out = new Amf3Output(context);
    }

    public void dispose(IoSession session) throws Exception {
	// TODO Auto-generated method stub
	amf3out.close();
    }

    @SuppressWarnings("unchecked")
    public void encode(IoSession session, Object message,
	    ProtocolEncoderOutput out) throws Exception {
	// TODO Auto-generated method stub
	IoBuffer buffer;
	if (message instanceof String) {
	    byte[] bytes = ((String) message).getBytes("UTF-8");
	    buffer = IoBuffer.allocate(bytes.length + 1);
	    buffer.put(bytes);
	    buffer.put((byte) 0x0);
	    buffer.flip();
	    out.write(buffer);
	} else {
	    Map<Object, Object> map = (Map<Object, Object>) message;
	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    amf3out.setOutputStream(stream);
	    amf3out.writeObject(map);
	    amf3out.flush();
	    byte[] bytes;
	    if(IS_COMPRESS) {
    	    	bytes = compress(session, stream.toByteArray());
	    } else {
		bytes = stream.toByteArray();
	    }
	    buffer = IoBuffer.allocate(bytes.length, false);
    	    buffer.put(bytes);
    	    buffer.flip();
	    out.write(buffer);
	    buffer.free();
	}
    }

    private byte[] compress(IoSession session, byte[] inputs) {
	Deflater deflater = (Deflater) session.getAttribute(DEFLATER);
	if (deflater == null) {
	    deflater = new Deflater();
	    session.setAttribute(DEFLATER, deflater);
	}
	deflater.reset();
	deflater.setInput(inputs);
	deflater.finish();
	byte outputs[] = new byte[0];
	ByteArrayOutputStream stream = new ByteArrayOutputStream(inputs.length);
	byte[] bytes = new byte[cachesize];
	int value;
	while (!deflater.finished()) {
	    value = deflater.deflate(bytes);
	    stream.write(bytes, 0, value);
	}
	outputs = stream.toByteArray();
	try {
	    stream.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return outputs;
    }

}

package com.ugame.prophecy.serializer.amf3;

import java.util.zip.InflaterInputStream;
import java.io.DataInputStream;
import java.io.UnsupportedEncodingException;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Input;
import flex.messaging.io.amf.ASObject;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class AMF3Decoder extends CumulativeProtocolDecoder {
    private final AttributeKey POLICY = new AttributeKey(getClass(), "policy");
    private final String security = "<policy-file-request/>";
    private final SerializationContext context = new SerializationContext();
    private final Amf3Input amf3in;

    private final static boolean IS_COMPRESS = false;
    
    public AMF3Decoder() {
	// TODO Auto-generated constructor stub
	amf3in = new Amf3Input(context);
    }

    protected boolean doDecode(IoSession session, IoBuffer in,
	    ProtocolDecoderOutput out) throws Exception {
	// TODO Auto-generated method stub
	if (isSecurityRequest(session, in)) {
	    out.write(security);
	    in.free();
	    return true;
	} else {
	    in.position(0);
	    if(IS_COMPRESS) {
		amf3in.setInputStream(new InflaterInputStream(new DataInputStream(
		    in.asInputStream())));
	    } else {
		amf3in.setInputStream(new DataInputStream(
			    in.asInputStream()));
	    }
	    Object message = amf3in.readObject();
	    if (message instanceof ASObject) {
		out.write(message);
		in.free();
		return true;
	    } else {
		in.free();
		return false;
	    }
	}
    }

    private boolean isSecurityRequest(IoSession session, IoBuffer in) {
	Boolean policy = (Boolean) session.getAttribute(POLICY);
	if (policy != null) {
	    return false;
	}
	String request = getRequest(in);
	boolean result = false;
	if (request != null) {
	    result = request.startsWith(security);
	}
	session.setAttribute(POLICY, new Boolean(result));
	return result;
    }

    private String getRequest(IoBuffer in) {
	byte[] bytes = new byte[in.limit()];
	in.get(bytes);
	String request;
	try {
	    request = new String(bytes, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    // TODO Auto-generated catch block
	    request = null;
	}
	return request;
    }

}

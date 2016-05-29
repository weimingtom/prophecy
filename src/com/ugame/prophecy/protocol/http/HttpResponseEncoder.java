package com.ugame.prophecy.protocol.http;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.log.CommonSysLog;

/**
 * @author Apache Mina 1
 * @see http://mina.apache.org/
 */
public class HttpResponseEncoder {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpResponseEncoder.class);
    
    private static final Set<Class<?>> TYPES;

    static {
        final Set<Class<?>> types = new HashSet<Class<?>>();
        types.add(HttpResponseMessage.class);
        TYPES = Collections.unmodifiableSet(types);
    }

    private static final byte[] CRLF = new byte[] { 0x0D, 0x0A };

    public void encode(final IoSession session, final HttpResponseMessage message) {
	final HttpResponseMessage msg = (HttpResponseMessage) message;
	final IoBuffer buf = IoBuffer.allocate(256);
        buf.setAutoExpand(true);
        try {
            final CharsetEncoder encoder = Charset.defaultCharset().newEncoder();
            buf.putString("HTTP/1.1 ", encoder);
            buf.putString(String.valueOf(msg.getResponseCode()), encoder);
            final int code = msg.getResponseCode();
            if (code == HttpResponseMessage.SUCCESS) {
                buf.putString(" OK", encoder);
            } else if (code == HttpResponseMessage.NOT_FOUND) {
                buf.putString(" Not Found", encoder);
            }
            buf.put(CRLF);
            for (final Iterator<Entry<String, String>> it = msg.getHeaders().entrySet().iterator(); it
                    .hasNext();) {
                final Entry<String, String> entry = (Entry<String, String>) it.next();
                buf.putString((String) entry.getKey(), encoder);
                buf.putString(": ", encoder);
                buf.putString((String) entry.getValue(), encoder);
                buf.put(CRLF);
            }
            buf.putString("Content-Length: ", encoder);
            buf.putString(String.valueOf(msg.getBodyLength()), encoder);
            buf.put(CRLF);
            buf.put(CRLF);
            buf.put(msg.getBody());
        } catch (CharacterCodingException ex) {
            CommonSysLog.error(LOGGER, "HTTP charset encode error", ex);
        }
        buf.flip();
        session.write(buf);
    }
    
    public Set<Class<?>> getMessageTypes() {
        return TYPES;
    }
}

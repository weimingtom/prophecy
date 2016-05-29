package com.ugame.prophecy.protocol.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.log.CommonSysLog;

/**
 * IE7测试时需要在域名前加上http://前缀
 * @author Apache Mina 1
 * @see http://mina.apache.org/
 */
public class HttpRequestDecoder { 
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpRequestDecoder.class);
    
    //TODO:need new String("") ?
    private static final byte[] CONTENT_LENGTH = "Content-Length:".getBytes();
    private transient final CharsetDecoder decoder = Charset.defaultCharset().newDecoder();
    public transient HttpRequestMessage request = null;
    
    /**
     * FIXME:这个方法可能有问题
     * @param in
     * @return
     */
    public boolean messageComplete(final IoBuffer in) {
        final int last = in.remaining() - 1;
        boolean ret = false;
        if (in.remaining() < 4) {
            ret = false;
        } else {
            if (in.get(0) == (byte) 'G' && in.get(1) == (byte) 'E'
                    && in.get(2) == (byte) 'T') {
                ret = (in.get(last) == (byte) 0x0A
                        && in.get(last - 1) == (byte) 0x0D
                        && in.get(last - 2) == (byte) 0x0A && in.get(last - 3) == (byte) 0x0D);
            } else if (in.get(0) == (byte) 'P' && in.get(1) == (byte) 'O'
                    && in.get(2) == (byte) 'S' && in.get(3) == (byte) 'T') {
                int eoh = -1;
                for (int i = last; i > 2; i--) {
                    if (in.get(i) == (byte) 0x0A && in.get(i - 1) == (byte) 0x0D
                            && in.get(i - 2) == (byte) 0x0A
                            && in.get(i - 3) == (byte) 0x0D) {
                        eoh = i + 1;
                        break;
                    }
                }
                if (eoh == -1) {
                    ret = false;
                } else {
                    for (int i = 0; i < last; i++) {
                        boolean found = false;
                        for (int j = 0; j < CONTENT_LENGTH.length; j++) {
                            if (in.get(i + j) != CONTENT_LENGTH[j]) {
                                found = false;
                                break;
                            }
                            found = true;
                        }
                        if (found) {
                            final StringBuilder contentLength = new StringBuilder();
                            for (int j = i + CONTENT_LENGTH.length; j < last; j++) {
                                if (in.get(j) == 0x0D) {
                                    break;
                                }
                                contentLength.append(new String(
                                        new byte[] { in.get(j) }));
                            }
                            ret = (Integer.parseInt(contentLength.toString().trim())
                                    + eoh == in.remaining());
                            break;
                        }
                    }
                }
            }
        }
        return ret;
    }

    public HttpRequestMessage decodeBody(final IoBuffer input) {
	HttpRequestMessage ret = null;
        request = new HttpRequestMessage();
        try {
            request.setHeaders(parseRequest(new StringReader(input
                    .getString(decoder))));
            ret = request;
        } catch (CharacterCodingException ex) {
            CommonSysLog.error(LOGGER, "decodeBody error", ex);
        }
        return ret;
    }

    private Map<String, String[]> parseRequest(final Reader isReader) {
        final Map<String, String[]> map = new HashMap<String, String[]>();
        final BufferedReader rdr = new BufferedReader(isReader);
        try {
            String line = rdr.readLine();
            final String[] url = line.split(" ");
            if (url.length < 3) {
                return map;
            }
            map.put("URI", new String[] { line });
            map.put("Method", new String[] { url[0].toUpperCase() });
            map.put("Context", new String[] { url[1].substring(1) });
            map.put("Protocol", new String[] { url[2] });
            while ((line = rdr.readLine()) != null && line.length() > 0) {
                final String[] tokens = line.split(": ");
                map.put(tokens[0], new String[] { tokens[1] });
            }
            if (url[0].equalsIgnoreCase("POST")) {
                final int len = Integer.parseInt(map.get("Content-Length")[0]);
                final char[] buf = new char[len];
                if (rdr.read(buf) == len) {
                    line = String.copyValueOf(buf);
                }
            } else if (url[0].equalsIgnoreCase("GET")) {
                final int idx = url[1].indexOf('?');
                if (idx == -1) {
                    line = null;
                } else {
                    map.put("Context",
                            new String[] { url[1].substring(1, idx) });
                    line = url[1].substring(idx + 1);
                }
            }
            if (line != null) {
                final String[] match = line.split("\\&");
                for (int i = 0; i < match.length; i++) {
                    String[] params = new String[1];
                    String[] tokens = match[i].split("=");
                    switch (tokens.length) {
                    case 0:
                        map.put("@".concat(match[i]), new String[] {});
                        break;
                    
                    case 1:
                        map.put("@".concat(tokens[0]), new String[] {});
                        break;
                    
                    default:
                        String name = "@".concat(tokens[0]);
                        if (map.containsKey(name)) {
                            params = map.get(name);
                            String[] tmp = new String[params.length + 1];
                            /*
                            for (int j = 0; j < params.length; j++) {
                                tmp[j] = params[j];
                            }
                            */
                            System.arraycopy(params, 0, tmp, 0, params.length);
                            //params = null;
                            params = tmp;
                        }
                        params[params.length - 1] = tokens[1].trim();
                        map.put(name, params);
                    }
                }
            }
        } catch (IOException ex) {
            CommonSysLog.error(LOGGER, "HTTP request parse error", ex);
        }
        return map;
    }
}

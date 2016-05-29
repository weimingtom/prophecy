package com.ugame.prophecy.protocol.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.log.CommonSysLog;

/**
 * @author Apache Mina 1
 * @see http://mina.apache.org/
 */
public class HttpResponseMessage {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpResponseMessage.class);
    
    public static final int SUCCESS = 200;
    public static final int NOT_FOUND = 404;
    
    private transient final Map<String, String> headers = new HashMap<String, String>();
    private transient final ByteArrayOutputStream body = new ByteArrayOutputStream(1024);
    private int responseCode = SUCCESS;

    public HttpResponseMessage() {
        headers.put("Server", "HttpServer (" + TestHttpServer.VERSION_STRING + ')');
        headers.put("Cache-Control", "private");
        headers.put("Content-Type", "text/html; charset=iso-8859-1");
        headers.put("Connection", "keep-alive");
        headers.put("Keep-Alive", "200");
        final SimpleDateFormat format = new SimpleDateFormat(
		"EEE, dd MMM yyyy HH:mm:ss zzz", Locale.getDefault());
        final String formatStr = format.format(new Date());
        headers.put("Date", formatStr);
        headers.put("Last-Modified", formatStr);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setContentType(final String contentType) {
        headers.put("Content-Type", contentType);
    }

    public void setResponseCode(final int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public void appendBody(final byte[] bytes) {
        try {
            body.write(bytes);
        } catch (IOException ex) {
            CommonSysLog.error(LOGGER, "write HTTP Body error", ex);
        }
    }

    public void appendBody(final String str) {
        try {
            body.write(str.getBytes());
        } catch (IOException ex) {
            CommonSysLog.error(LOGGER, "write HTTP Body error", ex);
        }
    }

    public IoBuffer getBody() {
        return IoBuffer.wrap(body.toByteArray());
    }

    public int getBodyLength() {
        return body.size();
    }
}

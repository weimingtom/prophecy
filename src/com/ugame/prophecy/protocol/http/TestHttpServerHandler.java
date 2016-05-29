package com.ugame.prophecy.protocol.http;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Apache Mina 1
 * @see http://mina.apache.org/
 */
public class TestHttpServerHandler extends IoHandlerAdapter {
    private final static Logger LOGGER = LoggerFactory.getLogger(TestHttpServerHandler.class);
    
    @Override
    public void sessionCreated(final IoSession session) {
	session.setAttribute("HttpRequestDecoder",
		new HttpRequestDecoder());
	session.setAttribute("HttpResponseEncoder",
		new HttpResponseEncoder());
    }
    
    @Override
    public void sessionOpened(final IoSession session) {
        session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 60);
    }

    @Override
    public void messageReceived(final IoSession session, final Object message) {
	final HttpRequestDecoder decoder = (HttpRequestDecoder) session.getAttribute("HttpRequestDecoder");
	final IoBuffer buffer = (IoBuffer)message;
	if(decoder.messageComplete(buffer)) {
	    final HttpRequestMessage requestMessage = decoder.decodeBody(buffer);
	    if(requestMessage != null) {
		LOGGER.info("HTTP Received!");
		final HttpResponseMessage response = new HttpResponseMessage();
		response.setContentType("text/plain");
		response.setResponseCode(HttpResponseMessage.SUCCESS);
		response.appendBody("CONNECTED");
		if (response != null) {
		    final HttpResponseEncoder encoder = (HttpResponseEncoder) session.getAttribute("HttpResponseEncoder");
		    encoder.encode(session, response);
		    session.close(true);
		}
	    }
	}
    }

    @Override
    public void sessionIdle(final IoSession session, final IdleStatus status) {
	LOGGER.info("Disconnecting the idle.");
        session.close(true);
    }

    @Override
    public void exceptionCaught(final IoSession session, final Throwable cause) {
	LOGGER.error("PackBufferException:", cause.getCause());
	session.close(true);
    }
}

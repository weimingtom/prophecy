package com.ugame.prophecy.protocol.tcp;

/**
 * 包缓冲异常
 * @author Administrator
 *
 */
public class PackBufferException extends Exception {
    private static final long serialVersionUID = -2130915839282130176L;

    public PackBufferException(final String message) {
	super(message);
    }
}

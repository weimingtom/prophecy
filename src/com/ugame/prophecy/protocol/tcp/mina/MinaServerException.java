package com.ugame.prophecy.protocol.tcp.mina;

/**
 * 服务器异常
 * @author Administrator
 *
 */
public class MinaServerException extends Exception {
    private static final long serialVersionUID = -2130915839282130176L;

    public MinaServerException(final String message) {
	super(message);
    }
}

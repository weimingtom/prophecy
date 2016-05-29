package com.ugame.prophecy.protocol.tcp;

import com.ugame.prophecy.global.GlobalConfig;
import com.ugame.prophecy.protocol.tcp.cindy.CindyPackServer;
import com.ugame.prophecy.protocol.tcp.grizzly.GrizzlyPackServer;
import com.ugame.prophecy.protocol.tcp.mina.MinaPackServer;
import com.ugame.prophecy.protocol.tcp.netty.NettyPackServer;
import com.ugame.prophecy.protocol.tcp.xnet.XnetPackServer;
import com.ugame.prophecy.protocol.tcp.xsocket.XSocketPackServer;
import com.ugame.prophecy.protocol.tcp.yanf4j.Yanf4jPackServer;
import com.ugame.prophecy.protocol.tcp.javanio.JavaNIOPackServer;

public class PackServerFactory {    
    public static IPackServer newServer(int serverType) {
	IPackServer server = null;
	if (serverType == GlobalConfig.MINA) {
	    server = MinaPackServer.getInstance();
	} else if (serverType == GlobalConfig.NETTY) {
	    server = NettyPackServer.getInstance();
	} else if (serverType == GlobalConfig.XSOCKET) {
	    server = XSocketPackServer.getInstance();
	} else if (serverType == GlobalConfig.CINDY) {
	    //FIXME:这个实现在
	    //连接发送完数据后立刻关闭时出现异常
	    //TODO:暂时修复可用
	    server = CindyPackServer.getInstance();
	} else if (serverType == GlobalConfig.GRIZZLY) {
	    server = GrizzlyPackServer.getInstance();
	} else if (serverType == GlobalConfig.JAVANIO) {
	    server = JavaNIOPackServer.getInstance();
	} else if (serverType == GlobalConfig.XNET) {
	    server = XnetPackServer.getInstance();
	} else if (serverType == GlobalConfig.YANF4J) {
	    server = Yanf4jPackServer.getInstance();
	}
	return server;
    }
}

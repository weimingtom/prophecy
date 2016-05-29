package com.ugame.prophecy.protocol.tcp;

public interface IPackServer {
    //启动服务器
    void start() throws Exception;
    
    //关闭服务器
    void stop() throws Exception;
}

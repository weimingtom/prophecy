package com.ugame.prophecy.protocol.tcp;

import com.ugame.prophecy.global.GlobalConfig;
import com.ugame.prophecy.protocol.http.HttpRequestDecoder;
import com.ugame.prophecy.protocol.http.HttpResponseEncoder;
import com.ugame.prophecy.serializer.ISerializer;

/**
 * 连接上下文
 * 临时连接数据，绑定在IoSession对象上的属性
 * 用于实现和逻辑之间的数据传递
 * @author Administrator
 *
 */
public class PackContext {
    public static final String ATTR_CONNECTION = "connection";
    
    public transient int packID;
    
    //COMMON
    public PackBuffer decoderBuffer = new PackBuffer();
    
    //PACK protocol
    public int decoderState = 0;
    public long decoderLength = 0;
    
    //HTTP protocol
    public HttpRequestDecoder httpDecoder = new HttpRequestDecoder();
    public HttpResponseEncoder httpEncoder = new HttpResponseEncoder();
    public ISerializer serializer = null;
    public int serializerType;
    
    //TODO:这个常数不可以赋给protocol，仅用于标记
    public final static int NOT_OK = -1;
    
    public final static int UNKNOWN = 0;
    public final static int PACK = 1;
    public final static int HTTP = 2;
    public int protocol;
    
    public PackContext(final int packid) {
	this.packID = packid;
	this.protocol = UNKNOWN;
	this.serializerType = GlobalConfig.UNKNOWN_SERIALIZER;
    }
}

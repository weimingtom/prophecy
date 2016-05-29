package com.ugame.prophecy.serializer;

import com.ugame.prophecy.global.GlobalConfig;
import com.ugame.prophecy.serializer.protobuf.ProtobufSerializer;
import com.ugame.prophecy.serializer.protostuff.runtime.ProtostuffRuntimeSerializer;
import com.ugame.prophecy.serializer.thrift.ThriftSerializer;

public class SerializerFactory {
    public static ISerializer newSerializer(int serializerType) {
	ISerializer serializer = null;
	if (serializerType == GlobalConfig.PROTOBUF) {
	    serializer = new ProtobufSerializer();
	} else if (serializerType == GlobalConfig.THRIFT) {
	    serializer = new ThriftSerializer();
	} else if (serializerType == GlobalConfig.PROTOSTUFFRUNTIME) {
	    serializer = new ProtostuffRuntimeSerializer();
	}
	return serializer;
    }
}

package com.ugame.prophecy.protocol.pack;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalData;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.tcp.PackContext;
import com.ugame.prophecy.protocol.tcp.PackBuffer;
import com.ugame.prophecy.serializer.SerializerFactory;

/**
 * pack路由器和肥大类转换器
 * @author Administrator
 *
 */
public class CommonPack {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonPack.class);
    
    /**
     * 接收路由器，根据structArray表把byte[]转为VariantType
     * TODO:使用其它实现代替java.nio.ByteBuffer
     * @param data
     * @param session
     * @param message
     * @return
     * @throws UnsupportedEncodingException 
     */
    public static VariantType unpack(final byte[] data,
	    final PackContext conn) throws UnsupportedEncodingException {
	final VariantType result = new VariantType();
	final ByteBuffer buf = ByteBuffer.wrap(data);
	try {
	    CommonSysLog.info(LOGGER, "[result]: ");
	    result.packLength = data.length;
	    result.moduleID = buf.get();
	    result.netMsgID = buf.get();
	    CommonSysLog.info(LOGGER, "moduleID:" + result.moduleID);
	    CommonSysLog.info(LOGGER, "netMsgID:" + result.netMsgID);
	    if (result.moduleID == CommonPackInfo.MODL_SERIALIZER) {
		// 路由给序列化器
		// 如果类型相同，就不需要重复创建
		if (conn.serializerType != result.netMsgID) {
		    conn.serializerType = result.netMsgID;
		    conn.serializer = SerializerFactory.newSerializer(result.netMsgID);
		}
		if (conn.serializer != null) {
		    conn.serializer.unserialize(conn, data);
		}
	    } else {
		//路由给打包器
		int index = 0;
        	for (; index < CommonPackInfo.PACK_STRUCTS.length; index++) {
        	    final CommonPackStruct pack_struct = CommonPackInfo.PACK_STRUCTS[index];
        	    if (pack_struct.moduleID == result.moduleID && 
        		pack_struct.netMsgID == result.netMsgID) {
        		int string_order = 0;
        		int int_order = 0;
        		for (int j = 0; j < pack_struct.structArray.length; j++) {
        		    if (pack_struct.structArray[j] == CommonPackInfo.INT_FIELD
        			&& int_order < CommonPackInfo.STRING_FIELD_NUM) {
        			if (int_order == 0) {
        			    result.arg301 = buf.getInt();
        			    CommonSysLog.info(LOGGER, "int[1]:0x" + Integer.toHexString(result.arg301));
        			} else if (int_order == 1) {
        			    result.arg302 = buf.getInt();
        			    CommonSysLog.info(LOGGER, "int[2]:0x" + Integer.toHexString(result.arg302));
        			}
        			int_order++;
        		    } else if (pack_struct.structArray[j] == CommonPackInfo.STRING_FIELD
        			    && string_order < CommonPackInfo.STRING_FIELD_NUM) {
        			if (string_order == 0) {
        			    final int len = buf.getShort();
        			    final byte[] bytes = new byte[len];
        			    buf.get(bytes);
        			    result.arg501 = new String(bytes, "utf-8");
        			    CommonSysLog.info(LOGGER, "string[1]:" + result.arg501);
        			} else if (string_order == 1) {
        			    final int len = buf.getShort();
        			    final byte[] bytes = new byte[len];
        			    buf.get(bytes);
        			    result.arg502 = new String(bytes, "utf-8");
        			    CommonSysLog.info(LOGGER, "string[2]:" + result.arg502);
        			}
        			string_order++;
        		    }
        		}
        		break;
        	    }
        	}
	    	if (index == CommonPackInfo.PACK_STRUCTS.length) {
	    	    CommonSysLog.error(LOGGER, "unknown netMsgID:" + result.netMsgID, null);
	    	    CommonSysLog.output(PackBuffer.printHex(data, 0, data.length));
	    	}
	    	CommonSysLog.output("[" + conn.packID + "/" + GlobalData.getNumLogin() + "]" + result.toString());
	    }
	} catch (UnsupportedEncodingException e) {
	    CommonSysLog.error(LOGGER, "UnsupportedEncodingException", e);
	}
	return result;
    }

/*
    public static IoBuffer packBuffer(final byte[] data) {
	final IoBuffer buf = IoBuffer.allocate(6 + data.length);
	buf.put((byte) 'S');
	buf.put((byte) 'R');
	buf.put((byte) (data.length >> 24));
	buf.put((byte) (data.length >> 16));
	buf.put((byte) (data.length >> 8));
	buf.put((byte) (data.length));
	buf.put(data);
	buf.flip();
	return buf;
    }
*/
}

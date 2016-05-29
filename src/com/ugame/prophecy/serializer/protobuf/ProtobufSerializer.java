package com.ugame.prophecy.serializer.protobuf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalData;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.tcp.PackContext;
import com.ugame.prophecy.serializer.ISerializer;

public class ProtobufSerializer implements ISerializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtobufSerializer.class);
    
    @Override
    public void unserialize(final PackContext conn, byte[] data) {
	//TODO: ByteArrayInputStream不需要close()
	InputStream input = new ByteArrayInputStream(data);
	try {
	    input.skip(2);
	    PBCommonPackProtos.PBCommonPack pack = 
	        PBCommonPackProtos.PBCommonPack.parseFrom(input);
	    if (pack.hasLogin()) {
		CommonSysLog.output( 
			"[" + conn.packID + "/" + GlobalData.getNumLogin() + "]" +
			"[" + this.getClass().getSimpleName() + "]" + 
			"[login]" +
			"username:" + pack.getLogin().getUsername() + "," +
			"password:" + pack.getLogin().getPassword());
	    } else if (pack.hasMove()) {
		CommonSysLog.output( 
			"[" + conn.packID + "/" + GlobalData.getNumLogin() + "]" +
			"[" + this.getClass().getSimpleName() + "]" + 
			"[move]" + 
			"username:" + pack.getMove().getUsername() + "," +
			"x:" + pack.getMove().getX() + "," + 
			"y:" + pack.getMove().getY());
	    }
	} catch (IOException e) {
	    CommonSysLog.info(LOGGER, e.getMessage());
	} 
    }
}


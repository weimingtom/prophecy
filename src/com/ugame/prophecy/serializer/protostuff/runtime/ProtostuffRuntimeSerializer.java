package com.ugame.prophecy.serializer.protostuff.runtime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.ugame.prophecy.global.GlobalData;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.tcp.PackContext;
import com.ugame.prophecy.serializer.ISerializer;

public class ProtostuffRuntimeSerializer implements ISerializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtostuffRuntimeSerializer.class);
    
    public ProtostuffRuntimeSerializer() {
	
    }
    
    /**
     * data[0] == 0xff 
     * data[1] == 0x03
     * data[2] == protobuf:1, protostuff:2
     * data[3] == login:1, move:2 
     */
    @Override
    public void unserialize(PackContext conn, byte[] data) {
	InputStream input = new ByteArrayInputStream(data);
	try {
	    input.skip(4);
	    if (data[2] == 1) {
		// TODO: protobuf format
		if (data[3] == 1) {
		    LoginStuff loginpack = new LoginStuff();
		    Schema<LoginStuff> schemaLogin = RuntimeSchema.getSchema(LoginStuff.class);
		    ProtobufIOUtil.mergeFrom(input, loginpack, schemaLogin);
		    //System.out.println(loginpack);
		    CommonSysLog.output( 
			"[" + conn.packID + "/" + GlobalData.getNumLogin() + "]" +
			"[" + this.getClass().getSimpleName() + "]" +
			"[protobuf-format]" + 
			"[login]" +
			"username:" + loginpack.username + "," +
			"password:" + loginpack.password);
		} else if (data[3] == 2) {
		    MoveStuff movepack = new MoveStuff();
		    Schema<MoveStuff> schemaMove = RuntimeSchema.getSchema(MoveStuff.class);
		    ProtobufIOUtil.mergeFrom(input, movepack, schemaMove);
		    //System.out.println(movepack);
		    CommonSysLog.output( 
			"[" + conn.packID + "/" + GlobalData.getNumLogin() + "]" +
			"[" + this.getClass().getSimpleName() + "]" + 
			"[protobuf-format]" + 
			"[move]" +
			"username:" + movepack.username + "," +
			"x:" + movepack.x + "," +
			"y:" + movepack.y);
		}
	    } else if (data[2] == 2) {
		//TODO: protostuff format
	    }
	} catch (IOException e) {
	    CommonSysLog.info(LOGGER, e.getMessage());
	}
    }

}

package com.ugame.prophecy.serializer.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalData;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.tcp.PackContext;
import com.ugame.prophecy.serializer.ISerializer;

public class ThriftSerializer implements ISerializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftSerializer.class);
    
    @Override
    public void unserialize(PackContext conn, byte[] data) {
	// TODO: 注意，如果数据的格式不对是不会抛异常的！
	if(data == null || data.length < 2) 
	    return;
	CommonPack pack = new CommonPack();
	TMemoryBuffer buffer = new TMemoryBuffer(data.length - 2);
	try {
	    buffer.write(data, 2, data.length - 2);
	    TProtocol iprot = new TBinaryProtocol(buffer);
	    pack.read(iprot);
	    if (pack.isSetLogin()) {
		CommonSysLog.output( 
			"[" + conn.packID + "/" + GlobalData.getNumLogin() + "]" +
			"[" + this.getClass().getSimpleName() + "]" + 
			"[login]" +
			"username:" + pack.getLogin().getUsername() + "," +
			"password:" + pack.getLogin().getPassword());
	    } else if (pack.isSetMove()) {
		CommonSysLog.output( 
			"[" + conn.packID + "/" + GlobalData.getNumLogin() + "]" +
			"[" + this.getClass().getSimpleName() + "]" + 
			"[move]" + 
			"username:" + pack.getMove().getUsername() + "," +
			"x:" + pack.getMove().getX() + "," + 
			"y:" + pack.getMove().getY());
	    }
	} catch (TTransportException e1) {
	    CommonSysLog.info(LOGGER, e1.getMessage());
	} catch (TException e2) {
	    CommonSysLog.info(LOGGER, e2.getMessage());
	}
    }

}

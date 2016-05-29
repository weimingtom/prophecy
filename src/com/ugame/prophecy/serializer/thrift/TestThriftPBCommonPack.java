package com.ugame.prophecy.serializer.thrift;

import java.util.Arrays;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TMemoryBuffer;

public class TestThriftPBCommonPack {
    // TType
    public static final byte STOP = 0;
    public static final byte VOID = 1;
    public static final byte BOOL = 2;
    public static final byte BYTE = 3;
    public static final byte DOUBLE = 4;
    public static final byte I16 = 6;
    public static final byte I32 = 8;
    public static final byte I64 = 10;
    public static final byte STRING = 11;
    public static final byte STRUCT = 12;
    public static final byte MAP = 13;
    public static final byte SET = 14;
    public static final byte LIST = 15;
    public static final byte ENUM = 16;

    static byte[] testloginpack = { 
	0x0c,
	// field.type = LOGIN_FIELD_DESC.type = TType.STRUCT[0x0c]

	0x00, 0x01,
	// field.id = LOGIN_FIELD_DESC.id = (short)0x01

	// ---------------------------

	0x0b,
	// field.type = USERNAME_FIELD_DESC.type = TType.STRING[0x0b]

	0x00, 0x01,
	// field.id = USERNAME_FIELD_DESC.id = (short)0x01

	0x00, 0x00, 0x00, 0x06, 0x6e, 0x61, 0x6e, 0x61, 0x6d, 0x69,
	// byte[6] = "nanami"

	0x0b,
	// field.type = PASSWORD_FIELD_DESC.type = TType.STRING[0x0b]

	0x00, 0x02,
	// field.id = PASSWORD_FIELD_DESC.id = (short)0x02

	0x00, 0x00, 0x00, 0x05, 0x31, 0x32, 0x33, 0x34, 0x35,
	// byte[5] = "12345"

	0x00,
	// FieldStop = TType.STOP

	// ---------------------------

	0x00,
	// FieldStop = TType.STOP
    };

    static byte[] testmovepack = {
	0x0c, 
	//STRUCT=12
	0x00, 0x02, 
	//field:2
	//----------------------
	0x0b, 
	//STRING=11
	0x00, 0x01, 
	//field:1
	0x00, 0x00, 0x00, 0x06, 
	//byte[6]
	0x6e, 0x61, 0x6e, 0x61, 0x6d, 0x69,
	//"nanami"
	//----------------------
	0x08,
	//I32=8
	0x00, 0x02, 
	//field:2
	0x00, 0x00, 0x00, 0x01,
	//1
	//----------------------
	0x08, 
	//I32=8
	0x00, 0x03,
	//field:3
	0x00, 0x00, 0x00, 0x02,
	//2
	//----------------------
	0x00,
	0x00,
    };
    
    public static void test1() {
	CommonPack pack = new CommonPack();
	pack.setLogin(new LoginPack().setUsername("nanami")
		.setPassword("12345"));
	TMemoryBuffer buffer = new TMemoryBuffer(255);
	TProtocol protocol = new TBinaryProtocol(buffer);
	try {
	    pack.write(protocol);
	    System.out.println(buffer.length());
	    System.out.println(buffer.inspect());
	    System.out.println(Arrays.toString("nanami".getBytes()));
	    System.out.println(pack);
	} catch (TException e) {
	    e.printStackTrace();
	}
    }
    
    public static void test2() {
	CommonPack pack = new CommonPack();
	pack.setMove(new MovePack().setUsername("nanami")
		.setX(1).setY(2));
	TMemoryBuffer buffer = new TMemoryBuffer(255);
	TProtocol protocol = new TBinaryProtocol(buffer);
	try {
	    pack.write(protocol);
	    System.out.println(buffer.length());
	    System.out.println(buffer.inspect());
	    System.out.println(Arrays.toString("nanami".getBytes()));
	    System.out.println(pack);
	} catch (TException e) {
	    e.printStackTrace();
	}
    }
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {
	test1();
	test2();
    }

}

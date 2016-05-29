package com.ugame.prophecy.serializer.protobuf;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.protobuf.GeneratedMessage;

public class TestPBCommonPack {

    public static byte[] testpack1 = { 
	0x43, 0x54, 
	//packet head "CT"
	
	0x00, 0x00, 0x00, 0x13,
	//packet length = 0x13
	
	(byte) 0xFF, 0x01, 
	// serializer[0xff], protobuf[0x01]
	
	0x0A, 
	// field_number[0x01] << 8 | wire_type(embedded message)[0x2]
	
	0x0F, 
	// embedded message, length = 0x0f 
	
	0x0A, 
	// field_number[0x01] << 3 | wire_type(string)[0x2] 
	
	0x06, 
	0x6E, 0x61, 0x6E, 0x61, 0x6D, 0x69, 
	// username = "nanami"
	
	0x12, 
	// field_number[0x02] << 3 | wire_type(string)[0x2]  
	
	0x05, 
	0x31, 0x32, 0x33, 0x34, 0x35 
	// password = "12345"
    };

    public static byte[] testpack2 = { 
	0x43, 0x54,
	//packet head "CT"
	
	0x00, 0x00, 0x00, 0x10,
	//packet length = 0x10
	
	(byte) 0xFF, 0x01, 
	// serializer[0xff], protobuf[0x01]
	
	0x12, 
	// field_number[0x02] << 8 | wire_type(embedded message)[0x2]
	
	0x0C, 
	// embedded message, length = 0x0c
	
	0x0A,
	// field_number[0x01] << 3 | wire_type(string)[0x2] 
	
	0x06, 
	0x6E, 0x61, 0x6E, 0x61, 0x6D, 0x69, 
	// username = "nanami"
	
	0x10,
	// field_number[0x02] << 3 | wire_type(int32)[0x0]
	
	0x01, 
	// x = 1
	
	0x18, 
	// field_number[0x03] << 3 | wire_type(int32)[0x0]
	
	0x02 
	// y = 2
    };

    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub
	test2(testpack2);
    }

    public static void test1() {
	PBCommonPackProtos.PBLoginPack login = PBCommonPackProtos.PBLoginPack
		.newBuilder().setUsername("nanami").setPassword("12345")
		.build();
	PBCommonPackProtos.PBCommonPack pack1 = PBCommonPackProtos.PBCommonPack
		.newBuilder().setLogin(login).build();
	writeToFile("test/pbpack1.txt", pack1);

	PBCommonPackProtos.PBMovePack move = PBCommonPackProtos.PBMovePack
		.newBuilder().setUsername("nanami").setX(1).setY(2).build();
	PBCommonPackProtos.PBCommonPack pack2 = PBCommonPackProtos.PBCommonPack
		.newBuilder().setMove(move).build();
	writeToFile("test/pbpack2.txt", pack2);
    }

    public static void test2(byte[] p) {
	//PBCommonPackProtos.PBCommonPack pack = PBCommonPackProtos.PB
	try {
	    InputStream input = new ByteArrayInputStream(p);
	    input.skip(8);
	    PBCommonPackProtos.PBCommonPack pack = 
	        PBCommonPackProtos.PBCommonPack.parseFrom(input);
	    if (pack.hasLogin()) {
		System.out.println(pack.getLogin());
	    }
	    if (pack.hasMove()) {
		System.out.println(pack.getMove());
	    }
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    /**
     * @see com.google.protobuf.CodedOutputStream
     * @param filename
     * @param pack
     */
    public static void writeToFile(String filename, GeneratedMessage pack) {
	FileOutputStream output = null;
	try {
	    output = new FileOutputStream(filename);
	    pack.writeTo(output);
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} finally {
	    if (output != null) {
		try {
		    output.close();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		output = null;
	    }
	}
    }
}

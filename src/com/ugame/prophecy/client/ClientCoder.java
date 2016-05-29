package com.ugame.prophecy.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ClientCoder {
    public final static byte MODL_DEFAULT = 0x00;
    public final static byte MODL_HALLCLIENT = 0x01;
    public final static byte MODL_HALLSERVER = 0x02;
    public final static byte MODL_ROOMCLIENT = 0x03;
    public final static byte MODL_ROOMSERVER = 0x04;
    public final static byte MODL_GAMECLIENT = 0x05;
    public final static byte MODL_GAMESERVER = 0x06;
    public final static byte MODL_GAMELOGIC = 0x10;
    // ---------------------------------------------
    public final static byte NMSG_DEFAULT = 0x00;
    public final static byte NMSG_LOGIN = 0x01;
    public final static byte NMSG_KEEPALIVE = 0x02;
    public final static byte NMSG_MOVE = 0x03;
    
    private DataInputStream in;
    
    public ClientCoder(InputStream in) {
	this.in = new DataInputStream(in);
    }
    
    public byte[] toWire(ClientMsg msg) throws IOException {
	ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
	DataOutputStream out = new DataOutputStream(byteStream);
	out.writeByte(msg.module);
	out.writeByte(msg.type);
	msg.output(out);
	out.flush();
	byte[] data = byteStream.toByteArray();
	return data;
    }

    public ClientMsg fromWire(byte[] input) throws IOException {
	ByteArrayInputStream bs = new ByteArrayInputStream(input);
	DataInputStream in = new DataInputStream(bs);
	int module = in.readByte();
	int type = in.readByte();
	ClientMsg message = new ClientMsg(module, type);
	message.input(in);
	return message;
    }
    
    public void frameMsg(byte[] message, OutputStream out) throws IOException {
	out.write('C'); //67 C
	out.write('T'); //84 T
	out.write((message.length >> 24) & 0xff);
	out.write((message.length >> 16) & 0xff);
	out.write((message.length >> 8) & 0xff);
	out.write(message.length & 0xff);
	out.write(message);
	out.flush();
    }
    
    public byte[] nextMsg() throws IOException {
	int length;
	try {
	    in.readByte();
	    in.readByte();
	    length = in.readInt();
	} catch (EOFException e) {
	    return null;
	}
	byte[] msg = new byte[length];
	in.readFully(msg);
	return msg;
    }
}

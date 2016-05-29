package com.ugame.prophecy.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientMsg {
    public int module;
    public int type;
    public String username;
    public String password;
    public int x;
    public int y;
    
    public ClientMsg(int module, int type) {
	this.module = module;
	this.type = type;
    }
    
    public void output(DataOutputStream out) throws IOException {
	if(module == ClientCoder.MODL_GAMECLIENT) {
	    if(type == ClientCoder.NMSG_LOGIN) {
		out.writeUTF(username);
		out.writeUTF(password);
	    } else if(type == ClientCoder.NMSG_MOVE) {
		out.writeUTF(username);
		out.writeInt(x);
		out.writeInt(y);
	    }
	}
    }
    
    public void input(DataInputStream in) throws IOException {
	if(module == ClientCoder.MODL_GAMECLIENT) {
	    if(type == ClientCoder.NMSG_LOGIN) {
		username = in.readUTF();
		password = in.readUTF();
	    } else if(type == ClientCoder.NMSG_MOVE) {
		username = in.readUTF();
		x = in.readInt();
		y = in.readInt();
	    }
	}
    }
}

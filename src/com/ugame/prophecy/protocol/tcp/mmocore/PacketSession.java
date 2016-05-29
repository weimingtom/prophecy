package com.ugame.prophecy.protocol.tcp.mmocore;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.mmocore.network.MMOClient;
import org.mmocore.network.MMOConnection;
import org.mmocore.network.ReceivablePacket;

public class PacketSession extends MMOClient<MMOConnection<PacketSession>> {
    public PacketSession(MMOConnection<PacketSession> con) {
	super(con);
    }

    @Override
    public boolean decrypt(ByteBuffer buf, int size) {
	System.out.println("PacketSession decrypt : " + size);
	byte[] bytes = new byte[size];
	buf.mark();
	buf.get(bytes);
	buf.reset();
	System.out.println(">>>>>> " + new String(bytes));
	return true;
    }

    @Override
    public boolean encrypt(ByteBuffer buf, int size) {
	System.out.println("PacketSession encrypt : " + size);	
	return true;
    }

    @Override
    protected void onDisconnection() {
	System.out.println("PacketSession onDisconnection");
    }

    @Override
    protected void onForcedDisconnection() {
	System.out.println("PacketSession onForcedDisconnection");	
    }
    
    @Override
    public String toString() {
	InetAddress address = getConnection().getInetAddress();
	String ret = address != null ? address.getHostAddress() : "";
	return ret;
    }
    
    public void execute(ReceivablePacket<PacketSession> packet)
    {
	System.out.println("PacketSession execute");
    }
}

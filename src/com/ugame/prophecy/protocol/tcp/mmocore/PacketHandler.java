package com.ugame.prophecy.protocol.tcp.mmocore;

import java.nio.ByteBuffer;

import org.mmocore.network.IClientFactory;
import org.mmocore.network.IMMOExecutor;
import org.mmocore.network.IPacketHandler;
import org.mmocore.network.MMOConnection;
import org.mmocore.network.ReceivablePacket;

public class PacketHandler implements IPacketHandler<PacketSession>, IClientFactory<PacketSession>, IMMOExecutor<PacketSession> {
    @Override
    public PacketSession create(MMOConnection<PacketSession> con) {
	System.out.println("PacketHandler create");
	return new PacketSession(con);
    }
    
    @Override
    public ReceivablePacket<PacketSession> handlePacket(ByteBuffer buf, PacketSession client) {
	System.out.println("PacketHandler receive");
	ReceivablePacket<PacketSession> msg = new ReceivablePacket<PacketSession>() {
	    @Override
	    protected boolean read() {
		System.out.println("ReceivablePacket read");
		return true; //如果返回true，则执行execute
	    }
	    @Override
	    public void run() {
		System.out.println("ReceivablePacket run");
	    }
	};
	return msg;
    }

    @Override
    public void execute(ReceivablePacket<PacketSession> rp) {
	System.out.println("PacketHandler execute");
	rp.getClient().execute(rp);
    }

}

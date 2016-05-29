package com.ugame.prophecy.serializer;

import com.ugame.prophecy.protocol.tcp.PackContext;

public interface ISerializer {
    public void unserialize(final PackContext conn, byte[] data);
}

package com.ugame.prophecy.protocol.pack;

/**
 * pack结构的配置项
 * @author Administrator
 *
 */
public class CommonPackStruct {
    public transient int moduleID;
    public transient int netMsgID;
    public transient int length;
    public transient byte[] structArray;

    public CommonPackStruct(final int moduleID, final int netMsgID, final int length, final String struct) {
	// TODO:检查type的唯一性
	this.moduleID = moduleID;
	this.netMsgID = netMsgID;
	this.length = length;
	this.structArray = struct.getBytes();
	for (int i = 0; i < this.structArray.length; i++) {
	    if (this.structArray[i] >= '0' && this.structArray[i] <= '9') {
		this.structArray[i] -= '0';
	    } else {
		this.structArray[i] = CommonPackInfo.NONE_FIELD;
	    }
	}
    }
}

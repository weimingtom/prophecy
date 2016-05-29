package com.ugame.prophecy.protocol.pack;

/**
 * 肥大类，用于统一保存网络接收包和相关调试信息
 * @author Administrator
 *
 */
public class VariantType {
    public byte moduleID;
    public transient byte netMsgID;
    public transient int packLength;
    
    //public byte arg101, arg102;
    //NOTE: short
    //public int arg201, arg202;
    public transient int arg301, arg302;
    //public double arg401, arg402;
    public transient String arg501, arg502;
    //public VariantType arg601, arg602;

    @Override
    public String toString() {
	String ret;
	if (moduleID == CommonPackInfo.MODL_SERIALIZER) {
	    ret = "[MODL_SERIALIZER]";
	} else {
	    switch (netMsgID) {
	    case CommonPackInfo.NMSG_LOGIN:
		ret = "[NMSG_LOGIN]{" + 
	    		"packLength:" + packLength + "," +
	    		"username:" + arg501 + "," + 
	    		"password:" + arg502 + "}";
		break;

	    case CommonPackInfo.NMSG_MOVE:
		ret = "[NMSG_MOVE]{" + 
	    		"packLength:" + packLength + "," +
	    		"username:" + arg501 + "," + 
	    		"x:" + arg301 + "," + 
	    		"y:" + arg302 + "}";
		break;
	    
	    default:
		ret = "[NMSG_UNKNOWN] => " + netMsgID;
		break;
	    }
	}
	return ret;
    }
}

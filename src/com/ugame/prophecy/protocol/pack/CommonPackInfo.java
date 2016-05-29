package com.ugame.prophecy.protocol.pack;

/**
 * pack的全局配置
 * TODO:
 * 	如果修改这里的枚举值，
 * 	需要检查PACK_STRUCTS的结构字符串是否正确。
 * @author Administrator
 *
 */
final public class CommonPackInfo {
    public static final int NONE_FIELD = 0;
    public static final int BYTE_FIELD = 1, BYTE_FIELD_NUM = 2;
    public static final int SHORT_FIELD = 2, SHORT_FIELD_NUM = 2;
    public static final int INT_FIELD = 3, INT_FIELD_NUM = 2;
    public static final int DOUBLE_FIELD = 4, DOUBLE_FIELD_NUM = 2;
    public static final int STRING_FIELD = 5, STRING_FIELD_NUM = 2;
    public static final int LIST_FIELD = 6, LIST_FIELD_NUM = 2;
    // ---------------------------------------------
    // NOTE:不要使用(byte)转换类型，
    // 因为判断相等会失败，如
    // MODL_SERIALIZER = (byte)0xff
    // 将导致 MODL_SERIALIZER != -1 
    //（0xff在解包时会变成-1）
    public final static byte MODL_DEFAULT = 0x00;
    public final static byte MODL_HALLCLIENT = 0x01;
    public final static byte MODL_HALLSERVER = 0x02;
    public final static byte MODL_ROOMCLIENT = 0x03;
    public final static byte MODL_ROOMSERVER = 0x04;
    public final static byte MODL_GAMECLIENT = 0x05;
    public final static byte MODL_GAMESERVER = 0x06;
    public final static byte MODL_GAMELOGIC = 0x10;
    public final static byte MODL_SERIALIZER = -1; //0xFF
    // ---------------------------------------------
    public final static byte NMSG_DEFAULT = 0x00;
    public final static byte NMSG_LOGIN = 0x01;
    public final static byte NMSG_KEEPALIVE = 0x02;
    public final static byte NMSG_MOVE = 0x03;
    // ----------------------------------------------
    public static final CommonPackStruct[] PACK_STRUCTS = {
	new CommonPackStruct(MODL_GAMECLIENT, NMSG_LOGIN, -1, "55"),
	new CommonPackStruct(MODL_GAMECLIENT, NMSG_MOVE, -1, "533"), 
    };
    // ----------------------------------------------
    private CommonPackInfo() {
	
    }
}

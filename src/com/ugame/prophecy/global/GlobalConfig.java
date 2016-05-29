package com.ugame.prophecy.global;

/**
 * 全局常量
 * TODO:非final的静态变量都有枚举值，
 * 目前是不可变的，以后考虑用界面热配置。
 * @author Administrator
 * 
 */
public final class GlobalConfig {
    //错误日志位置（ACCESS_PATH是当前工作路径）
    public final static String ACCESS_PATH = System.getProperty("user.dir");
    public final static String ERROR_LOG_PATH = ACCESS_PATH + "\\error.log";
    
    //默认端口号
    public final static int DEFAULT_PORT = 8899;
    
    //取消APACHE MINA的日志输出过滤器
    public final static boolean IS_ADD_LOGGER = false;
    
    //默认库的包名
    public final static String LUA_LIB_NAME = "ugame";
    
    //选择网络框架类型
    public final static int MINA = 0;
    public final static int NETTY = 1;
    public final static int XSOCKET = 2;
    public final static int CINDY = 3;
    public final static int GRIZZLY = 4;
    public final static int JAVANIO = 5;
    public final static int XNET = 6;
    public final static int YANF4J = 7;
    public final static int MMOCORE = 1001;
    //测试不同的NIO实现
    //CINDY的实现有bug -> 20110926：暂时修正
    //XNET的实现有bug -> fixed
    //建议用MINA
    public static int serverType = MINA;
    
    //界面显示方式
    public final static int TIMER = 1;
    public final static int IMME = 2;
    //建议用TIMER，以防止SWT界面被大量的输出日志卡死
    public static int outputType = TIMER;
    
    //是否把调试和错误日志显示到界面上
    //false用于粘贴到notepad2，通过数行数快速统计接收包数，
    //检查收包是否收齐。
    //NOTE:需要测试最后关闭全部连接和立刻关闭当前连接两种情况
    //建议用false，减少输出内容
    //（开发阶段请打开它，测试阶段请关闭它）
    public final static boolean showLogger = false;
    
    //选择序列化器
    public final static int UNKNOWN_SERIALIZER = 0;
    public final static int PROTOBUF = 1;
    public final static int THRIFT = 2;
    public final static int PROTOSTUFFRUNTIME = 3;
    
    // ---------------------------------------------
    private GlobalConfig() {
	
    }
}

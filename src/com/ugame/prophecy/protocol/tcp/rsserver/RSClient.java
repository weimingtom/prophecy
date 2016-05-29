package com.ugame.prophecy.protocol.tcp.rsserver;

import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.log.CommonSysLog;

public class RSClient implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RSClient.class);
    
    /**
     * 超时时间 = cycleTime * TIMEOUT_COUNT_MAX毫秒
     */
    private static final int TIMEOUT_COUNT_MAX = 2 * 1000;
    
    /**
     * 是否被初始化
     */
    public boolean initialized = false;
    /**
     * 是否掉线
     */
    public boolean disconnected = false;
    /**
     * 是否激活
     */
    public boolean isActive = false;
    /**
     * 是否被踢
     */
    public boolean isKicked = false;
    /**
     * 玩家的IP
     */
    public String connectedFrom = "";
    /**
     * 全局消息
     */
    public String globalMessage = "";
    /**
     * 用户ID
     */
    public int playerId = -1;
    /**
     * 用户名称
     */
    public String playerName = null;
    /**
     * 用户密码
     */
    public String playerPass = null;
    /**
     * 全局的用户处理句柄
     */
    public RSClientHandler handler = null;
    
    /**
     * 流缓冲区大小
     */
    public static final int bufferSize = 5000;
    
    //包体长度（用类型算出）
    public int packetSize = 0;
    //包类型（首字节）
    public int packetType = -1;
    //套接字
    private Socket mySock;
    //套接字的输入流
    private InputStream in;
    //套接字的输出流
    private OutputStream out;
    //输出缓冲
    public byte buffer[] = null;
    //读写位置
    public int readPtr;
    public int writePtr;
    // 输入流
    public RSStream inStream = null;
    // 输出流
    public RSStream outStream = null;
    // 超时时间
    public int timeOutCounter = 0;
    // 登陆包的处理方法
    public int returnCode = 2;
    
    /**
     * 初始化
     * 
     * @param s
     * @param _playerId
     */
    public RSClient(Socket s, int _playerId) {
	playerId = _playerId;
	mySock = s;
	try {
	    in = s.getInputStream();
	    out = s.getOutputStream();
	} catch (IOException e) {
	    println("BlakeScape Server: Exception!");
	    e.printStackTrace();
	}
	outStream = new RSStream(new byte[bufferSize]);
	outStream.currentOffset = 0;
	inStream = new RSStream(new byte[bufferSize]);
	inStream.currentOffset = 0;
	readPtr = writePtr = 0;
	buffer = new byte[bufferSize];
    }

    /**
     * 处理登陆包
     * 更新输入输出流缓冲
     */
    @Override
    public void run() {
	isActive = false;
	//TODO:读取登陆包信息
	try {
	    returnCode = 2;
	} catch (Exception e) {
	    println("BlakeScape Server: Exception!");
	    e.printStackTrace();
	    destruct();
	    return;
	}
	isActive = true;
	if (playerId == -1 || returnCode != 2) {
	    return;
	}
	packetSize = 0;
	packetType = -1;
	readPtr = 0;
	writePtr = 0;
	int numBytesInBuffer;
	int offset;
	this.println_debug("login");
	// 进入通信状态
	while (!disconnected) {
	    synchronized (this) {
		if (writePtr == readPtr) {
		    try {
			wait();
		    } catch (InterruptedException e) {
		    }
		}
		if (disconnected) {
		    return;
		}
		offset = readPtr;
		if (writePtr >= readPtr) {
		    numBytesInBuffer = writePtr - readPtr;
		} else {
		    numBytesInBuffer = bufferSize - readPtr;
		}
	    }
	    if (numBytesInBuffer > 0) {
		try {
		    out.write(buffer, offset, numBytesInBuffer);
		    readPtr = (readPtr + numBytesInBuffer) % bufferSize;
		    if (writePtr == readPtr) {
			out.flush();
		    }
		} catch (Exception e) {
		    println("BlakeScape Server: Exception!");
		    e.printStackTrace();
		    disconnected = true;
		}
	    }
	}
    }

    /**
     * 未激活用户的初始化
     */
    public void initialize() {

    }

    /**
     * 激活用户的更新
     */
    public void update() {
	handler.updatePlayer(this, outStream);
	flushOutStream();
    }
    
    /**
     * 更新后发送数据
     */
    public void flushOutStream() {
	if (disconnected || outStream.currentOffset == 0) {
	    return;
	}
	synchronized (this) {
	    int maxWritePtr = (readPtr + bufferSize - 2) % bufferSize;
	    for (int i = 0; i < outStream.currentOffset; i++) {
		buffer[writePtr] = outStream.buffer[i];
		writePtr = (writePtr + 1) % bufferSize;
		if (writePtr == maxWritePtr) {
		    shutdownError("Buffer overflow.");
		    disconnected = true;
		    return;
		}
	    }
	    outStream.currentOffset = 0;
	    notify();
	}
    }
    
    /**
     * 立刻发送数据
     * 把outStream的数据写到输出流out中
     * @throws IOException
     */
    public void directFlushOutStream() throws IOException {
	out.write(outStream.buffer, 0, outStream.currentOffset);
	outStream.currentOffset = 0;
    }
    
    
    /**
     * 每500毫秒执行一次
     * @return false表示退出循环，否则从头再执行process
     */
    public boolean process() {
	if (disconnected) {
	    return false;
	}
	try {
	    //超时控制
	    if (timeOutCounter++ > TIMEOUT_COUNT_MAX) {
		println("Client lost connection: timeout");
		disconnected = true;
		return false;
	    }
	    if (in == null) {
		return false;
	    }
	    int avail = in.available();
	    if(avail > 0) {
		println_debug("process -> in.available is " + avail);
	    }
	    if (avail < 6)
		return false;
	    if (packetType == -1) {
		//读取头部标志字节
		int head1 = in.read();
		int head2 = in.read();
		println_debug("head1, head2 == " + head1 + "," + head2);
		int ps = 0;
		packetType = 1;
		//读取包长度
		ps |= (in.read() & 0xff) << 24;
		ps |= (in.read() & 0xff) << 16;
		ps |= (in.read() & 0xff) << 8;
		ps |= (in.read() & 0xff);
		packetSize = ps;
		println_debug("packetType is " + packetType + " packetSize is " + packetSize);
		avail -= 6;
		return false;
	    }
	    if (avail < packetSize) {
		return false;
	    }
	    fillInStream(packetSize);
	    println_debug("fillInStream:" + packetSize);
	    timeOutCounter = 0;
	    parseIncomingPackets();
	    packetType = -1;
	} catch (Exception e) {
	    println("BlakeScape Server: Exception!");
	    e.printStackTrace();
	    disconnected = true;
	}
	return true;
    }

    /**
     * 从输入流中读固定长度的数据到inStream
     * @param forceRead
     * @throws IOException
     */
    private void fillInStream(int forceRead) throws IOException {
	inStream.currentOffset = 0;
	in.read(inStream.buffer, 0, forceRead);
    }
    
    /**
     * 解析输入包
     */
    private void parseIncomingPackets() {

    }

    /**
     * 用户回收前的工作
     */
    public void destruct() {
	if (mySock == null)
	    return;
	try {
	    println("ClientHandler: Client " + playerName
		    + " disconnected.");
	    disconnected = true;
	    if (in != null) {
		in.close();
	    }
	    if (out != null) {
		out.close();
	    }
	    mySock.close();
	    mySock = null;
	    in = null;
	    out = null;
	    inStream = null;
	    outStream = null;
	    isActive = false;
	    synchronized (this) {
		notify();
	    }
	    buffer = null;
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
    }
    
    /**
     * 掉线错误报告
     * 
     * @param errorMessage
     */
    public void shutdownError(String errorMessage) {
	println("Fatal: " + errorMessage);
	destruct();
    }
    
    /**
     * 调试输出
     * @param str
     */
    public void println_debug(String str) {
	CommonSysLog.info(LOGGER, "[client-" + playerId + "-" + playerName + "]: "
		+ str);
    }

    /**
     * 输出
     * @param str
     */
    public void println(String str) {
	CommonSysLog.info(LOGGER, "[client-" + playerId + "-" + playerName + "]: "
		+ str);
    }
    
    /**
     * 回收这个用户，初始化对象
     */
    public void clearUpdateFlags() {
	
    }

    /**
     * 移动逻辑初始化
     */
    public void preProcessing() {
	
    }
    
    /**
     * 移动逻辑
     */
    public void postProcessing() {
	
    }
    
    /**
     * 踢人
     */
    public void kick() {
	isKicked = true;
    }
}

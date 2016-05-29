package com.ugame.prophecy.protocol.rtmp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf0Input;

/**
 * 仅实现三次握手的RTMP服务器
 * @see http://bbs.9ria.com/thread-10560-1-1.html
 * @see http://www.cnweblog.com/fly2700/archive/2008/04/09/281431.html
 * @see http://wiki.gnashdev.org/RTMP
 * @see http://wiki.gnashdev.org/AMF
 * @see http://tlb.org/rtmpout.html
 */
public class TestSimpleRTMPServer {
    final static byte [] head = {
	    0x03, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xE4, 0x14, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x07, 0x63,
	    0x6F, 0x6E, 0x6E, 0x65, 0x63, 0x74, 0x00, 0x3F, (byte) 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03,
	    0x00, 0x03, 0x61, 0x70, 0x70, 0x02, 0x00, 0x08, 0x53, 0x4F, 0x53, 0x61, 0x6D, 0x70, 0x6C, 0x65,
	    0x00, 0x08, 0x66, 0x6C, 0x61, 0x73, 0x68, 0x56, 0x65, 0x72, 0x02, 0x00, 0x0E, 0x57, 0x49, 0x4E,
	    0x20, 0x31, 0x30, 0x2C, 0x32, 0x2C, 0x31, 0x35, 0x39, 0x2C, 0x31, 0x00, 0x06, 0x73, 0x77, 0x66,
	    0x55, 0x72, 0x6C, 0x06, 0x00, 0x05, 0x74, 0x63, 0x55, 0x72, 0x6C, 0x02, 0x00, 0x19, 0x72, 0x74,
	    0x6D, 0x70, 0x3A, 0x2F, 0x2F, 0x6C, 0x6F, 0x63, 0x61, 0x6C, 0x68, 0x6F, 0x73, 0x74, 0x2F, 0x53,
	    0x4F, 0x53, 0x61, 0x6D, 0x70, 0x6C, 0x65, 0x00, 0x04, 0x66, 0x70, 0x61, 0x64, 0x01, 0x00, 0x00,
	    0x0C, 0x63, 0x61, 0x70, 0x61, 0x62, 0x69, 0x6C, 0x69, 0x74, 0x69, 0x65, (byte) 0xC3, 0x73, 0x00, 0x40,
	    0x6D, (byte) 0xE0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0B, 0x61, 0x75, 0x64, 0x69, 0x6F, 0x43, 0x6F,
	    0x64, 0x65, 0x63, 0x73, 0x00, 0x40, (byte) 0xA8, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0B, 0x76,
	    0x69, 0x64, 0x65, 0x6F, 0x43, 0x6F, 0x64, 0x65, 0x63, 0x73, 0x00, 0x40, 0x6F, (byte) 0x80, 0x00, 0x00,
	    0x00, 0x00, 0x00, 0x00, 0x0D, 0x76, 0x69, 0x64, 0x65, 0x6F, 0x46, 0x75, 0x6E, 0x63, 0x74, 0x69,
	    0x6F, 0x6E, 0x00, 0x3F, (byte) 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x07, 0x70, 0x61, 0x67,
	    0x65, 0x55, 0x72, 0x6C, 0x06, 0x00, 0x00, 0x09, 0x00, 0x40, (byte) 0xDC, 0x4D, (byte) 0xC0, 0x00, 0x00, 0x00,
	    0x00,
     };
    
/*
00000600  03 00 00 00 00 00 E4 14 00 00 00 00 02 00 07 63    ...............c
00000610  6F 6E 6E 65 63 74 00 3F F0 00 00 00 00 00 00 03    onnect.?........
00000620  00 03 61 70 70 02 00 08 53 4F 53 61 6D 70 6C 65    ..app...SOSample
00000630  00 08 66 6C 61 73 68 56 65 72 02 00 0E 57 49 4E    ..flashVer...WIN
00000640  20 31 30 2C 32 2C 31 35 39 2C 31 00 06 73 77 66     10,2,159,1..swf
00000650  55 72 6C 06 00 05 74 63 55 72 6C 02 00 19 72 74    Url...tcUrl...rt
00000660  6D 70 3A 2F 2F 6C 6F 63 61 6C 68 6F 73 74 2F 53    mp://localhost/S
00000670  4F 53 61 6D 70 6C 65 00 04 66 70 61 64 01 00 00    OSample..fpad...
00000680  0C 63 61 70 61 62 69 6C 69 74 69 65 C3 73 00 40    .capabilitie.s.@
00000690  6D E0 00 00 00 00 00 00 0B 61 75 64 69 6F 43 6F    m........audioCo
000006A0  64 65 63 73 00 40 A8 EE 00 00 00 00 00 00 0B 76    decs.@.........v
000006B0  69 64 65 6F 43 6F 64 65 63 73 00 40 6F 80 00 00    ideoCodecs.@o...
000006C0  00 00 00 00 0D 76 69 64 65 6F 46 75 6E 63 74 69    .....videoFuncti
000006D0  6F 6E 00 3F F0 00 00 00 00 00 00 00 07 70 61 67    on.?.........pag
000006E0  65 55 72 6C 06 00 00 09 00 40 DC 4D C0 00 00 00    eUrl.....@.M....
000006F0  00                                                 .               
 */
    
    final static byte [] head2 = {
	/*
	    0x03,
	    //12字节长头部（包括这个字节），ChannelID为3（即Invoke通道）
            //
            //00	12 bytes  0?
	    //01	8 bytes   4?
	    //10	4 bytes   8?
	    //11	1 byte    C?
            //
            //ChannelID	Use
            //02	Ping 和ByteRead通道
            //03	Invoke通道 我们的connect() publish()和自字写的NetConnection.Call() 数据都是在这个通道的
            //04	Audio和Vidio通道
            //05 06 07	服务器保留,经观察FMS2用这些Channel也用来发送音频或视频数据

	    0x00, 0x00, 0x00,
	    //时间戳
	    
	    0x00, 0x00, (byte) 0xE4, 
	    //总长度（如果超过0x80或128就分割，头部加上0xC?字节（不计入总长度）
	    
	    0x14, 
	    //AMF类型（即Invoke） 0x01-0x06的介绍见Page 31
            //0x01	Chunk Size	changes the chunk size for packets
            //0x02	Unknown	
            //0x03	Bytes Read	send every x bytes read by both sides
            //0x04	Ping	ping is a stream control message, has subtypes
            //0x05	Server BW	the servers downstream bw
            //0x06	Client BW	the clients upstream bw 
            //0x07	Unknown	
            //0x08	Audio Data	packet containing audio
            //0x09	Video Data	packet containing video data
            //0x0A-0x0E	Unknown	
            //0x0F	FLEX_STREAM_SEND	TYPE_FLEX_STREAM_SEND
            //0x10	FLEX_SHARED_OBJECT	TYPE_FLEX_SHARED_OBJECT
            //0x11	FLEX_MESSAGE	TYPE_FLEX_MESSAGE
            //0x12	Notify	an invoke which does not expect a reply
            //0x13	Shared Object	has subtypes
            //0x14	Invoke	like remoting call, used for stream actions too.
            //0x16	StreamData	这是FMS3出来后新增的数据类型,这种类型数据中包含AudioData和VideoData
	    //
            //0x3 This specifies the content type of the RTMP packet is the number of bytes read. This is used to start the RTMP connection.
            //0x4 This specifies the content type of the RTMP message is a ping packet.
            //0x5 This specifies the content type of the RTMP message is server response of some type.
            //0x6 This specifies the content type of the RTMP packet is client request of some type.
            //0x8 This specifies the content type of the RTMP packet is an audio message.
            //0x9 This specifies the content type of the RTMP message is a video packet.
            //0x12 This specifies the content type of the RTMP message is notify.
            //0x13 This specifies the content type of the RTMP message is shared object.
            //0x14 This specifies the content type of the RTMP message is remote procedure call. This invokes the method of a Flash class remotely.


	    0x00, 0x00, 0x00, 0x00, 
	    //StreamID
	*/
	
	    0x02, 
	    0x00, 0x07, 
	    0x63, 0x6F, 0x6E, 0x6E, 0x65, 0x63, 0x74, 
	    //connect
	    //
	    //see Page 45
	    //
	    //Transaction ID 
	    //	String 
	    //	Always set to 1.                      | 
	    //Command Object 
	    //	Object 
	    //	Command information object which has the name-value pairs.                 | 
	    //Optional User Arguements
	    // 	Object  
	    //	Any optional information
	    
	    0x00, 
	    0x3F, (byte) 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	    //(Number) 1.0
	    
	    0x03,
	    //object-marker
	    
	    0x00, 0x03, 
	    0x61, 0x70, 0x70,  
	    //app
	    0x02, 
	    0x00, 0x08, 
	    0x53, 0x4F, 0x53, 0x61, 0x6D, 0x70, 0x6C, 0x65, 
	    //SOSample
	    
	    
	    0x00, 0x08, 
	    0x66, 0x6C, 0x61, 0x73, 0x68, 0x56, 0x65, 0x72, 
	    //flashVer
	    0x02, 
	    0x00, 0x0E, 
	    0x57, 0x49, 0x4E, 0x20, 0x31, 0x30, 0x2C, 0x32, 0x2C, 0x31, 0x35, 0x39, 0x2C, 0x31, 
	    //WIN 10,2,159,1
	    
	    0x00, 0x06, 
	    0x73, 0x77, 0x66, 0x55, 0x72, 0x6C,  
	    //swfURL
	    0x06, 
	    //undefined
	    
	    0x00, 0x05, 
	    0x74, 0x63, 0x55, 0x72, 0x6C, 
	    //tcUrl
	    0x02, 
	    0x00, 0x19, 
	    0x72, 0x74, 0x6D, 0x70, 0x3A, 0x2F, 0x2F, 0x6C, 0x6F, 0x63, 0x61, 0x6C, 0x68, 0x6F, 0x73, 0x74, 
	    0x2F, 0x53, 0x4F, 0x53, 0x61, 0x6D, 0x70, 0x6C, 0x65, 
	    //rtmp://localhost/SOSample
	    
	    0x00, 0x04, 
	    0x66, 0x70, 0x61, 0x64, 
	    //fpad
	    0x01, 
	    0x00, 
	    //false
	    
	    0x00, 0x0C, 
	    0x63, 0x61, 0x70, 0x61, 0x62, 0x69, 0x6C, 0x69, 0x74, 0x69, 0x65, 
	    //FIXME:
	    //(byte) 0xC3, //超过了128字节的分割包
	    0x73, 
	    //capabilities
	    0x00, 
	    0x40, 0x6D, (byte) 0xE0, 0x00, 0x00, 0x00, 0x00, 0x00,
	    //(Number) 239.0
	    
	    0x00, 0x0B, 
	    0x61, 0x75, 0x64, 0x69, 0x6F, 0x43, 0x6F, 0x64, 0x65, 0x63, 0x73, 
	    //audioCodec
	    0x00, 
	    0x40, (byte) 0xA8, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 
	    //(Number) 3191.0
	    
	    0x00, 0x0B,
	    0x76, 0x69, 0x64, 0x65, 0x6F, 0x43, 0x6F, 0x64, 0x65, 0x63, 0x73, 
	    //videoCodecs
	    0x00, 
	    0x40, 0x6F, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 
	    //(Number) 252.0
	    
	    0x00, 0x0D, 
	    0x76, 0x69, 0x64, 0x65, 0x6F, 0x46, 0x75, 0x6E, 0x63, 0x74, 0x69, 0x6F, 0x6E, 
	    //videoFunction
	    0x00, 
	    0x3F, (byte) 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	    //(Number) 1.0
	    
	    0x00, 0x07, 
	    0x70, 0x61, 0x67, 0x65, 0x55, 0x72, 0x6C,
	    //pageUrl
	    0x06, 
	    //undefined
	    
	    0x00, 0x00,
	    //空的UTF-8字符串
	    0x09, 
	    //object-end-marker
	    
	    0x00, 
	    0x40, (byte) 0xDC, 0x4D, (byte) 0xC0, 0x00, 0x00, 0x00, 0x00,
	    //(Number) 28983.0
    };
    
/*
AMF0每个标签占一个字节：
    number-marker  = 0x00   
    boolean-marker  = 0x01   
    string-marker  = 0x02   
    object-marker  = 0x03   
    movieclip-marker  = 0x04  ; reserved, not supported 
    null-marker  = 0x05   
    undefined-marker  = 0x06   
    reference-marker  = 0x07   
    ecma-array-marker  = 0x08   
    object-end-marker  = 0x09   
    strict-array-marker  = 0x0A   
    date-marker  = 0x0B   
    long-string-marker  = 0x0C   
    unsupported-marker  = 0x0D   
    recordset-marker  = 0x0E  ; reserved, not supported 
    xml-document-marker  = 0x0F   
    typed-object-marker  = 0x10   

AMF0解包结果：(含义见Page 46)
    connect
    1.0
    ASObject(2208288){
    	app=SOSample, 
    	fpad=false, 
    	flashVer=WIN 10,2,159,1, 
    	tcUrl=rtmp://localhost/SOSample, 
    	audioCodecs=3191.0, 
    	videoFunction=1.0, 
    	pageUrl=null, 
    	capabilities=239.0, 
    	swfUrl=null, 
    	videoCodecs=252.0
    }
    28983.0
    
    
    这里遗漏objectEncoding（见Page46）用于指定AMF格式
 */
    
    final static byte[] data01 = {0x0A, 0x0B, 0x01, 0x03, 0x79, 0x04, (byte) 0x81, 0x54, 0x0B, 0x65, 0x76, 0x65, 0x6E, 0x74, 0x06, 0x09,
	    0x6D, 0x6F, 0x76, 0x65, 0x03, 0x78, 0x04, (byte) 0x83, 0x5B, 0x01
    };
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
	//test1();
	test2();
	//System.out.println(String.format("0x%02x", head2.length));
    }
    
    public static void test2() throws ClassNotFoundException, IOException {
	SerializationContext context = new SerializationContext();
	Amf0Input amf0in = new Amf0Input(context);
	amf0in.setInputStream(new ByteArrayInputStream(head2));
	while(amf0in.available() > 0) {
	    //System.out.println("available:" + amf0in.available());
	    Object message = amf0in.readObject();
	    System.out.println(message);
	}
    }
    
    public static void test1()  throws IOException {
	ServerSocket ss = new ServerSocket(1935);
	System.out.println("监听1935端口");
	Socket s = ss.accept();
	DataInputStream dis = new DataInputStream(s.getInputStream());
	DataOutputStream outs = new DataOutputStream(s.getOutputStream());
	byte[] bytes = new byte[1536];
	int i = 0;
	try {
	    dis.skipBytes(1); //读C0包的0x03头部
	    while (true) {
		//System.out.println(i);
		if (i == 3) { 
		    break;//收到AMF包后立刻退出服务器
		}
		
		//i == 0:收C0包（前面已经用skipBytes收了一个字节）
		//i == 1:收C1包
		//i == 2:收首字节为0x03的粘包（AMF格式？）
		dis.read(bytes); 
		
		if (i == 0) { //收到C0包
		    outs.write(0x03);  //谜之(1536 * 2 + 1)字节
		    outs.write(bytes); 
		    outs.write(bytes);
		    outs.flush(); //发送S0包
		}
		i++;
	    }
	} catch (EOFException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} finally{
	    dis.close();
	    s.close();
	    ss.close();
	    outs.close();
	}
    }
}

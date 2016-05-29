package  
{
	import flash.display.Sprite;
	import flash.events.SecurityErrorEvent;
	import flash.events.NetStatusEvent;
	import flash.net.NetConnection;
	import flash.net.Responder;
	import flash.text.TextField;
	import flash.text.TextFieldAutoSize;
	import flash.events.MouseEvent;
	import flash.net.ObjectEncoding;
	
	public class TestRTMPEcho extends Sprite
	{
		private var txt:TextField = new TextField;
		private var cn:NetConnection;
		
		public function TestRTMPEcho() 
		{
			txt.autoSize = TextFieldAutoSize.LEFT;
			addChild(txt);
			stage.addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
			log("点击鼠标开始");
		}
		
		private function onMouseDown(e:MouseEvent):void 
		{
			txt.text = "";
			log("初始化...");
			cn = new NetConnection();
			cn.objectEncoding = ObjectEncoding.AMF0;
			cn.connect("rtmp:/127.0.0.1/echo");
			cn.addEventListener(NetStatusEvent.NET_STATUS, netStatusHandler);
			cn.addEventListener(SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);
		}
		
		private function netStatusHandler(event:NetStatusEvent):void 
		{
			switch (event.info.code) 
			{
				case "NetConnection.Connect.Success":
					log("连接成功!");
					cn.call("echo", new Responder(result, status), {a: "foo", b: "bar"});
					break;
					
				case "NetConnection.Connect.Closed":
					log("关闭连接");
					break;
				
				case "NetConnection.Connect.Failed":
					log("连接失败!");
					break;
				
				case "NetStream.Play.StreamNotFound":
					log("无法找到远程主机");
					break;
			}
		}
		
		private function result(e:Object):void
		{
			log("result");
			for (var key:String in e)
			{
				log("\t" + key + "=>" + e[key]);
			}
			cn.close();
		}
		
		private function status(e:Object):void
		{
			log("status");
			log(e.description);
			cn.close();
		}
		
		private function securityErrorHandler(event:SecurityErrorEvent):void 
		{
			txt.appendText("securityError: " + event);
		}
		
		private function log(text:String):void 
		{
			txt.appendText(text + "\n");
		}
	}
}


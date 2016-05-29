package com.ugame.prophecy.protocol.tcp.rsserver;
import java.net.Socket;

/**
 * 所有用户的处理句柄，Accept线程
 * @author Administrator
 *
 */
public class RSClientHandler {
    /**
     * 最大同时在线人数
     */
    public static final int maxPlayers = 512;
    /**
     * 在线用户列表
     */
    public RSClient players[] = new RSClient[maxPlayers];
    /**
     * 在线人数
     */
    public static int playerCount = 0;
    /**
     * 所有在线用户的用户名列表 
     */
    public static String playersCurrentlyOn[] = new String[maxPlayers];
    /**
     * 踢一个人指令
     */
    public static String kickNick = "";
    /**
     * 踢所有人指令
     */
    public static boolean kickAllPlayers = false;
    /**
     * 全局消息指令
     */
    public static String messageToAll = "";
    
    /**
     * 初始化
     */
    RSClientHandler() {
	for (int i = 0; i < maxPlayers; i++) {
	    players[i] = null;
	}
    }
    
    /**
     * 创建一个1:1客户端线程
     * @param s
     * @param connectedFrom
     */
    public void newPlayerClient(Socket s, String connectedFrom) {
	int slot = -1, i = 1;
	do {
	    if (players[i] == null) {
		slot = i;
		break;
	    }
	    i++;
	    if (i >= maxPlayers) {
		i = 0;
	    }
	} while (i <= maxPlayers);
	RSClient newClient = new RSClient(s, slot);
	newClient.handler = this;
	(new Thread(newClient)).start();
	if (slot == -1) {
	    return;
	}
	players[slot] = newClient;
	players[slot].connectedFrom = connectedFrom;
    }
    
    /**
     * 终结所有用户
     */
    public void destruct() {
	for (int i = 0; i < maxPlayers; i++) {
	    if (players[i] == null) {
		continue;
	    }
	    players[i].destruct();
	    players[i] = null;
	}
    }
    
    /**
     * 获取用户数
     * @return
     */
    public static int getPlayerCount() {
	return playerCount;
    }
    
    /**
     * 统计用户数，顺便收集所有在线用户名称
     */
    public void updatePlayerNames() {
	playerCount = 0;
	for (int i = 0; i < maxPlayers; i++) {
	    if (players[i] != null) {
		playersCurrentlyOn[i] = players[i].playerName;
		playerCount++;
	    } else {
		playersCurrentlyOn[i] = "";
	    }
	}
    }
    
    /**
     * 判断用户是否在线
     * @param playerName
     * @return
     */
    public static boolean isPlayerOn(String playerName) {
	for (int i = 0; i < maxPlayers; i++) {
	    if (playersCurrentlyOn[i] != null) {
		if (playersCurrentlyOn[i].equalsIgnoreCase(playerName)) {
		    return true;
		}
	    }
	}
	return false;
    }
    
    /**
     * 逻辑循环的一个周期，维持数据的实时
     * 每500毫秒执行一次
     */
    public void process() {
	updatePlayerNames();
	//全局指令
	if (messageToAll.length() > 0) {
	    int msgTo = 1;
	    do {
		if (players[msgTo] != null) {
		    players[msgTo].globalMessage = messageToAll;
		}
		msgTo++;
	    } while (msgTo < maxPlayers);
	    messageToAll = "";
	}
	//踢人指令
	if (kickAllPlayers) {
	    int kickID = 1;
	    do {
		if (players[kickID] != null) {
		    players[kickID].isKicked = true;
		}
		kickID++;
	    } while (kickID < maxPlayers);
	    kickAllPlayers = false;
	}
	//激活用户的游戏逻辑循环和退出保存
	for (int i = 0; i < maxPlayers; i++) {
	    if (players[i] == null || !players[i].isActive) {
		continue;
	    }
	    players[i].preProcessing();
	    while (players[i].process()) {
		;
	    }
	    players[i].postProcessing();
	    if (players[i].playerName != null && players[i].playerName.equalsIgnoreCase(kickNick)) {
		players[i].kick();
		kickNick = "";
	    }
	    if (players[i].disconnected) {
		//TODO:用户退出，保存用户信息
		removePlayer(players[i]);
		players[i] = null;
	    }
	}
	//未激活用户的游戏逻辑循环和退出保存
	for (int i = 0; i < maxPlayers; i++) {
	    if (players[i] == null || !players[i].isActive) {
		continue;
	    }
	    if (players[i].disconnected) {
		//TODO:用户退出，保存用户信息
		removePlayer(players[i]);
		players[i] = null;
	    } else {
		if (!players[i].initialized) {
		    players[i].initialize();
		    players[i].initialized = true;
		} else {
		    players[i].update();
		}
	    }
	}
	//回收剩下的所有用户槽
	for (int i = 0; i < maxPlayers; i++) {
	    if (players[i] == null || !players[i].isActive) {
		continue;
	    }
	    players[i].clearUpdateFlags();
	}
    }
        
    /**
     * 执行用户回收前的工作
     * @param plr
     */
    private void removePlayer(RSClient plr) {
	plr.destruct();
    }
    
    public void updatePlayer(RSClient plr, RSStream str) {
	
    }
}

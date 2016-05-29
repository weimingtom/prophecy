package com.ugame.prophecy.test;

public class Utility {

    public static final int PORT = 1405;

    public static String checkUrlAndReply(String serverName, String request) {
	if (request.startsWith("GET /gettimeblock HTTP")) {
	    return getPage(serverName, 512);
	} else {
	    return "Invalid URL";
	}
    }

    public static String getTimeBlock(int size) {
	StringBuilder sb = new StringBuilder();
	String nowNano;

	int i = 1;

	while (i <= size) {
	    nowNano = String.valueOf(System.nanoTime()).substring(8, 16);
	    sb.append(nowNano);
	    i++;
	}

	return sb.toString();
    }

    public static String getPage(String serverName, int size) {

	StringBuilder sb = new StringBuilder();

	sb.append("HTTP/1.1 200 OK\n");
	sb.append("Date: Sat, 07 Mar 2009 00:00:00 GMT\n");
	sb.append("Server: " + serverName + "\n");
	sb.append("Accept-Ranges: bytes\n");
	sb.append("Content-Length: $$$$\n");
	sb.append("Connection: close\n");
	sb.append("Content-Type: text/html\n");
	sb.append("\n");

	String header = sb.toString();
	String body = "<html><body><h1>" + getTimeBlock(size)
		+ "</h1></body></html>";

	header = header.replace("$$$$", String.valueOf(body.length()));
	String result = header + body;

	return result;
    }
}
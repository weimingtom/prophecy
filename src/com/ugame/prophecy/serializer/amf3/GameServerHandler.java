package com.ugame.prophecy.serializer.amf3;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import flex.messaging.io.amf.ASObject;

public class GameServerHandler extends IoHandlerAdapter {

    private static Map<Object, Object> roles;

    public void sessionOpened(IoSession session) throws Exception {
	put(session);
    }

    public void sessionClosed(IoSession session) throws Exception {
	remove(session);
    }

    public void messageReceived(IoSession session, Object message)
	    throws Exception {
	if (message instanceof ASObject) {
	    ASObject object = (ASObject) message;
	    if (object.get("event").equals("move")) {
		move(session, object);
	    } else {
		init(session);
	    }
	} else {
	    sendSecurity(session);
	}
    }

    public void exceptionCaught(IoSession session, Throwable cause)
	    throws Exception {
	cause.printStackTrace();
	session.close(true);
    }

    private void put(IoSession session) {
	if (roles == null) {
	    roles = new HashMap<Object, Object>();
	}
	if (roles.isEmpty() || !roles.containsKey(session)) {
	    Map<Object, Object> map = new HashMap<Object, Object>();
	    map.put("id", new Long(session.getId()));
	    roles.put(session, map);
	}
    }

    @SuppressWarnings("unchecked")
    private Map<Object, Object> get(IoSession session) {
	if (roles == null || roles.isEmpty() || !roles.containsKey(session)) {
	    return null;
	}
	return (Map<Object, Object>) roles.get(session);
    }

    private void remove(IoSession session) {
	if (roles == null || roles.isEmpty() || !roles.containsKey(session)) {
	    return;
	}
	Map<Object, Object> map = new HashMap<Object, Object>();
	map.put("event", "remove");
	map.put("id", new Long(session.getId()));
	send(session, map, false);
	roles.remove(session);
	System.out.println("event:remove;id:" + session.getId());
    }

    private void move(IoSession session, ASObject message) {
	Map<Object, Object> map = get(session);
	if (map == null) {
	    return;
	}
	map.put("x", message.get("x"));
	map.put("y", message.get("y"));
	roles.put(session, map);
	map.put("event", "move");
	send(session, map, true);
	System.out.println("event:move;id:" + session.getId() + ";x:"
		+ message.get("x") + ";y:" + message.get("y"));
    }

    @SuppressWarnings("unchecked")
    private void init(IoSession session) {
	Map<Object, Object>[] maps = new Map[roles.size()];
	int i = 0;
	for (Iterator<Map.Entry<Object, Object>>  iterator = roles.entrySet().iterator(); iterator
		.hasNext();) {
	    Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) iterator.next();
	    Map<Object, Object> value = (Map<Object, Object>) entry.getValue();
	    maps[i] = value;
	    i++;
	}
	Map<Object, Object> map = new HashMap<Object, Object>();
	map.put("id", new Long(session.getId()));
	map.put("event", "init");
	map.put("list", maps);
	session.write(map);
	map.remove("list");
	map.put("event", "add");
	send(session, map, false);
	System.out.println("event:init;id:" + session.getId() + ";list-size:"
		+ maps.length);
    }

    private void send(IoSession session, Map<Object, Object> map, boolean self) {
	for (Iterator<Map.Entry<Object, Object>>  iterator = roles.entrySet().iterator(); iterator
		.hasNext();) {
	    Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) iterator.next();
	    IoSession key = (IoSession) entry.getKey();
	    if (self || session.getId() != key.getId()) {
		key.write(map);
	    }
	}
    }

    private void sendSecurity(IoSession session) {
	String security = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
		+ "\n"
		+ "	<!DOCTYPE cross-domain-policy SYSTEM \"http://www.adobe.com/xml/dtds/cross-domain-policy.dtd\">"
		+ "\n" + "	<cross-domain-policy>" + "\n"
		+ "		<site-control permitted-cross-domain-policies=\"all\"/>"
		+ "\n" + "		<allow-access-from domain=\"*\" to-ports=\"*\" />"
		+ "\n" + "	</cross-domain-policy>";
	session.write(security);
    }

}

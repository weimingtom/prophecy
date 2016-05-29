package com.ugame.prophecy.serializer.fastjson;

import com.alibaba.fastjson.JSON;

public class Example {

    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub
	Group group = new Group();
	group.setId(0L);
	group.setName("admin");
	User guestUser = new User();
	guestUser.setId(2L);
	guestUser.setName("guest");
	User rootUser = new User();
	rootUser.setId(3L);
	rootUser.setName("root");
	group.getUsers().add(guestUser);
	group.getUsers().add(rootUser);
	String jsonString = JSON.toJSONString(group);
	System.out.println(jsonString);

	Group group2 = JSON.parseObject(jsonString, Group.class);
	System.out.println(JSON.toJSONString(group2));
    }
}

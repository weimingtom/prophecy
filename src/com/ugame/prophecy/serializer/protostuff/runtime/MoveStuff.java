package com.ugame.prophecy.serializer.protostuff.runtime;

public class MoveStuff {
    public String username;
    public int x;
    public int y;

    public MoveStuff() {

    }

    public MoveStuff(String username, int x, int y) {
	this.username = username;
	this.x = x;
	this.y = y;
    }

    @Override
    public String toString() {
	return "username:" + this.username + 
		", x:" + this.x + ", y:" + this.y;
    }
}

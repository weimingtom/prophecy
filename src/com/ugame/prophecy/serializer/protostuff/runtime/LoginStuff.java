package com.ugame.prophecy.serializer.protostuff.runtime;

public class LoginStuff {
    public String username;
    public String password;

    public LoginStuff() {

    }

    public LoginStuff(String username, String password) {
	this.username = username;
	this.password = password;
    }

    @Override
    public String toString() {
	return "username:" + this.username + ", password:" + this.password;
    }
}

package com.ugame.prophecy.serializer.xstream;

public class PhoneNumber {
    private int code;
    private String number;
    
    // ... constructors and methods
    public PhoneNumber() {
	
    }
    
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
    
    @Override
    public String toString() {
	return code + " - " + number;
    }
}

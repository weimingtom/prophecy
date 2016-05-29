package com.ugame.prophecy.serializer.simplexml;

import java.io.ByteArrayOutputStream;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

@Root
public class Example {
    @Element
    private String text;

    @Attribute
    private int index;

    public Example() {
	super();
    }

    public Example(String text, int index) {
	this.text = text;
	this.index = index;
    }

    public String getMessage() {
	return text;
    }

    public int getId() {
	return index;
    }

    @Override
    public String toString() {
	return "index: " + index + ", text:" + text;
    }
    
    public final static void main(String[] args) {
	Serializer serializer = new Persister();
	Example example = new Example("Example message", 123);
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	try {
	    serializer.write(example, out);
	    System.out.println("serialize => \n" + out.toString());
	    example = serializer.read(Example.class, out.toString());
	    System.out.println("deserialize => \n" + example);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}


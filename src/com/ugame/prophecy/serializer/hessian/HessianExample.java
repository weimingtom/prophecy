package com.ugame.prophecy.serializer.hessian;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import com.caucho.hessian.io.Hessian2StreamingInput;
import com.caucho.hessian.io.Hessian2StreamingOutput;

public class HessianExample {
    public HessianExample() {
	
    }

    public static void main(String[] args) throws IOException {
	TestData data = new TestData();
	data.id = 100;
	data.name = "hello";
	byte[] bytes = serialize(data);
	TestData data2 = (TestData) deserialize(bytes);
	System.out.println("data2.id == " + data2.id);
	System.out.println("data2.name == " + data2.name);	
    }

    private static Object deserialize(byte[] array) throws IOException {
	ByteArrayInputStream in = new ByteArrayInputStream(array);
	Hessian2StreamingInput hin = new Hessian2StreamingInput(in);
	return hin.readObject();
    }
    
    private static byte[] serialize(Object data) throws IOException {
	ByteArrayOutputStream out = new ByteArrayOutputStream(500);
	Hessian2StreamingOutput hout = new Hessian2StreamingOutput(out);
	hout.writeObject(data);
	return out.toByteArray();
    }
    
    private static class TestData implements Serializable {
	private static final long serialVersionUID = 5269396006793982148L;
	
	public int id;
	public String name;
    }
}


package com.ugame.prophecy.serializer.kryo;

import java.nio.ByteBuffer;
import com.esotericsoftware.kryo.serialize.StringSerializer;

public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub	
	ByteBuffer buffer = ByteBuffer.allocateDirect(256);
	StringSerializer stringSerializer = new StringSerializer();
	stringSerializer.writeObjectData(buffer, "some text");
	buffer.flip();
	String text = stringSerializer.readObjectData(buffer, String.class);
	System.out.println(text);
	
	buffer.clear();
	StringSerializer.put(buffer, "some text");
	int pos = buffer.position();
	buffer.rewind();
	for(int i = 0; i < pos; i++) {
	    byte b = buffer.get();
	    System.out.print(Integer.toHexString(b) + " ");
	}
	System.out.println();
	buffer.position(pos);
	buffer.flip();
	text = StringSerializer.get(buffer);
	System.out.println(text);
	for(int i = 0; i < text.length(); i++) {
	    System.out.print(Integer.toHexString(text.charAt(i)) + " ");
	}
	System.out.println();
    }
}

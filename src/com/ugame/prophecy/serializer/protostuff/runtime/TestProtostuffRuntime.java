package com.ugame.prophecy.serializer.protostuff.runtime;
import java.util.Arrays;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

public class TestProtostuffRuntime {
    public static void test1() {
	LoginStuff pack1 = new LoginStuff("nanami", "12345");

	// this is lazily created and cached by RuntimeSchema
	// so its safe to call RuntimeSchema.getSchema(Foo.class) over and over
	// The getSchema method is also thread-safe
	Schema<LoginStuff> schema = RuntimeSchema.getSchema(LoginStuff.class);
	LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

	byte[] data = 
	    //ProtostuffIOUtil.
	    ProtobufIOUtil.
		toByteArray(pack1, schema,
		buffer);
	System.out.println(Arrays.toString(data));
	// deser
	LoginStuff pack2 = new LoginStuff();
	//ProtostuffIOUtil.
	ProtobufIOUtil.
		mergeFrom(data, pack2, schema);
	System.out.println(pack2);	
    }

    public static void test2() {
	MoveStuff pack1 = new MoveStuff("nanami", 1, 2);

	// this is lazily created and cached by RuntimeSchema
	// so its safe to call RuntimeSchema.getSchema(Foo.class) over and over
	// The getSchema method is also thread-safe
	Schema<MoveStuff> schema = RuntimeSchema.getSchema(MoveStuff.class);
	LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

	byte[] data = 
	    //ProtostuffIOUtil.
	    ProtobufIOUtil.
		toByteArray(pack1, schema,
		buffer);
	System.out.println(Arrays.toString(data));
	// deser
	MoveStuff pack2 = new MoveStuff();
	//ProtostuffIOUtil.
	ProtobufIOUtil.
		mergeFrom(data, pack2, schema);
	System.out.println(pack2);	
    }
    
    /**
     * @see http://code.google.com/p/protostuff/wiki/ProtostuffRuntime
     * @param args
     */
    public static void main(String[] args) {
	test1();
	test2();
    }
}

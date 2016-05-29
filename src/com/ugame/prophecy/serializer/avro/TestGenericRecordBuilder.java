package com.ugame.prophecy.serializer.avro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.GenericData.Record;

import org.codehaus.jackson.node.TextNode;

public class TestGenericRecordBuilder {
    /**
     * 手动生成模式
     * @return
     */
    private static Schema recordSchema() {
	List<Field> fields = new ArrayList<Field>();
	fields.add(new Field("id", Schema.create(Type.STRING), null,
		new TextNode("0")));
	fields.add(new Field("intField", Schema.create(Type.INT), null, null));
	fields.add(new Field("anArray", Schema.createArray(Schema
		.create(Type.STRING)), null, null));
	Schema schema = Schema.createRecord("Foo", "test", "mytest", false);
	schema.setFields(fields);
	return schema;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub
	//模式数据
	Schema schema = recordSchema();
	GenericRecordBuilder builder = new GenericRecordBuilder(schema);
	
	//数据
	builder.set("intField", 1); //不能设置为null
	List<String> anArray = Arrays.asList(new String[] { "one", "two", "three" });
	builder.set("anArray", anArray);
	
	//此时构造器只包含数据，不包含模式数据
	if(builder.has("anArray")) {
	    System.out.println("builder => ");
	    System.out.println("intField: " + builder.get("intField"));
	    System.out.println("anArray: " + builder.get("anArray"));
	    //ID为null
	    System.out.println("id: " + builder.get("id"));
	}
	
	//编译，应用模式的默认值
	Record record = builder.build();
	if (record != null) {
	    System.out.println("record => ");
	    System.out.println("intField: " + record.get("intField"));
	    System.out.println("anArray: " + record.get("anArray"));
	    //ID为null
	    System.out.println("id: " + record.get("id"));
	}
	
	//清除
	builder.clear("intField");
	System.out.println("clear intField=>");
	System.out.println("has intField:" + builder.has("intField"));
	System.out.println("intField:" + builder.get("intField"));
    }
}

package com.ugame.prophecy.serializer.avro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;

/**
 * @see Avro 1.6.0 java testcase
 * @author Administrator
 * 
 */
public class TestGenericDatumWriter {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	// TODO Auto-generated method stub
	/**
	 * TODO:生成模式除了用json外，
	 * 还可以使用底层的API
	 */
	String json = "{\"type\": \"record\", " + 
		"\"name\": \"r\", " + 
		"\"fields\": [" + 
		"{ \"name\": \"f1\", \"type\": \"long\" " + 
		"}" + 
		"]}";
	/**
	 * 把字符串转换为模式
	 */
	Schema.Parser parser = new Schema.Parser();
	Schema s = parser.parse(json);
	GenericRecord r = new GenericData.Record(s);
	r.put("f1", 100L);
	ByteArrayOutputStream bao = new ByteArrayOutputStream();
	GenericDatumWriter<GenericRecord> w = new GenericDatumWriter<GenericRecord>(
		s);
	Encoder e = EncoderFactory.get().jsonEncoder(s, bao);
	w.write(r, e);
	e.flush();
	System.out.println(bao.toString());
	GenericRecord o = new GenericDatumReader<GenericRecord>(s).read(null,
		DecoderFactory.get().jsonDecoder(s,
			new ByteArrayInputStream(bao.toByteArray())));
	System.out.println("e.equals(r) => " + o.equals(r));
    }
}

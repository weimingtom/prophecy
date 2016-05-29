package com.ugame.prophecy.serializer.xstream;

import com.thoughtworks.xstream.XStream;

public class Test {
    /**
     * @see http://www.iteye.com/news/22986
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub
	PhoneNumber phone = new PhoneNumber();
	phone.setCode(123);
	phone.setNumber("1234-456");

	PhoneNumber fax = new PhoneNumber();
	fax.setCode(123);
	fax.setNumber("9999-999");

	Person joe = new Person();
	joe.setFirstname("Joe");
	joe.setLastname("Walnes");
	joe.setPhone(phone);
	joe.setFax(fax);
	
	XStream xstream = new XStream();
	xstream.alias("person", Person.class);
	xstream.alias("phonenumber", PhoneNumber.class);
	
	String xml = xstream.toXML(joe);
	System.out.println(xml);
	
	Person anothorJoe = (Person) xstream.fromXML(xml);
	System.out.println(anothorJoe);
    }
}

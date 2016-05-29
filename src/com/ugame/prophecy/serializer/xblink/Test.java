package com.ugame.prophecy.serializer.xblink;

import org.xblink.XBlink;

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
	
	XBlink.registerClassesToBeUsed(new Class[] { 
		Person.class, 
		PhoneNumber.class 
	});
	String xml = XBlink.toXml(joe);
	System.out.println(xml);
	
	Person anothorJoe = (Person) XBlink.fromXml(xml);
	System.out.println(anothorJoe);
    }
}

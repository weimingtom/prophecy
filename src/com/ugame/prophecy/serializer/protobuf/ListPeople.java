package com.ugame.prophecy.serializer.protobuf;

import com.ugame.prophecy.serializer.protobuf.AddressBookProtos.AddressBook;
import com.ugame.prophecy.serializer.protobuf.AddressBookProtos.Person;

import java.io.FileInputStream;

public class ListPeople {
    static void Print(AddressBook addressBook) {
	for (Person person : addressBook.getPersonList()) {
	    System.out.println("Person ID: " + person.getId());
	    System.out.println("  Name: " + person.getName());
	    if (person.hasEmail()) {
		System.out.println("  E-mail address: " + person.getEmail());
	    }
	    for (Person.PhoneNumber phoneNumber : person.getPhoneList()) {
		switch (phoneNumber.getType()) {
		case MOBILE:
		    System.out.print("  Mobile phone #: ");
		    break;
		case HOME:
		    System.out.print("  Home phone #: ");
		    break;
		case WORK:
		    System.out.print("  Work phone #: ");
		    break;
		}
		System.out.println(phoneNumber.getNumber());
	    }
	}
    }

    public static void main(String[] args) throws Exception {
	/*
	if (args.length != 1) {
	    System.err.println("Usage:  ListPeople ADDRESS_BOOK_FILE");
	    System.exit(-1);
	}*/
	AddressBook addressBook = AddressBook.parseFrom(new FileInputStream(
		//args[0]));
		"test/protobuf.dat"));
	Print(addressBook);
    }
}

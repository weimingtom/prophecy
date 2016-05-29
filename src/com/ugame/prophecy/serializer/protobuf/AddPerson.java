package com.ugame.prophecy.serializer.protobuf;

import com.ugame.prophecy.serializer.protobuf.AddressBookProtos.AddressBook;
import com.ugame.prophecy.serializer.protobuf.AddressBookProtos.Person;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;

public class AddPerson {
    static Person PromptForAddress(BufferedReader stdin, PrintStream stdout) throws IOException {
	Person.Builder person = Person.newBuilder();
	stdout.print("Enter person ID: ");
	person.setId(Integer.valueOf(stdin.readLine()));
	stdout.print("Enter name: ");
	person.setName(stdin.readLine());
	stdout.print("Enter email address (blank for none): ");
	String email = stdin.readLine();
	if (email.length() > 0) {
	    person.setEmail(email);
	}
	while (true) {
	    stdout.print("Enter a phone number (or leave blank to finish): ");
	    String number = stdin.readLine();
	    if (number.length() == 0) {
		break;
	    }
	    Person.PhoneNumber.Builder phoneNumber = Person.PhoneNumber
		    .newBuilder().setNumber(number);
	    stdout.print("Is this a mobile, home, or work phone? ");
	    String type = stdin.readLine();
	    if (type.equals("mobile")) {
		phoneNumber.setType(Person.PhoneType.MOBILE);
	    } else if (type.equals("home")) {
		phoneNumber.setType(Person.PhoneType.HOME);
	    } else if (type.equals("work")) {
		phoneNumber.setType(Person.PhoneType.WORK);
	    } else {
		stdout.println("Unknown phone type.  Using default.");
	    }
	    person.addPhone(phoneNumber);
	}
	return person.build();
    }
    
    public static void main(String[] args) throws Exception {
	/*
	if (args.length != 1) {
	    System.err.println("Usage:  AddPerson ADDRESS_BOOK_FILE");
	    System.exit(-1);
	}
	*/
	AddressBook.Builder addressBook = AddressBook.newBuilder();
	String outputFileName = "test/protobuf.dat"; //args[0]
	try {
	    FileInputStream input = new FileInputStream(outputFileName);
	    addressBook.mergeFrom(input);
	    input.close();
	} catch (FileNotFoundException e) {
	    System.out.println(outputFileName
		    + ": File not found.  Creating a new file.");
	}
	addressBook.addPerson(PromptForAddress(new BufferedReader(
		new InputStreamReader(System.in)), System.out));
	FileOutputStream output = new FileOutputStream(outputFileName);
	addressBook.build().writeTo(output);
	output.close();
    }
}

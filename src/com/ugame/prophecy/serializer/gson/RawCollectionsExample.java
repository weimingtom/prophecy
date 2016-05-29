package com.ugame.prophecy.serializer.gson;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class RawCollectionsExample {
    public static final String json = "['hello',5,{name:'GREETINGS',source:'guest'}]";

    static class Event {
	private String name;
	private String source;

	private Event(String name, String source) {
	    this.name = name;
	    this.source = source;
	}

	@Override
	public String toString() {
	    return String.format("(name=%s, source=%s)", name, source);
	}
    }

    public static void main(String[] args) {
	Gson gson = new Gson();
	Collection<Object> collection = new ArrayList<Object>();
	collection.add("hello");
	collection.add(5);
	collection.add(new Event("GREETINGS", "guest"));
	String json2 = gson.toJson(collection);
	System.out.println("Using Gson.toJson() on a raw collection: " + json2);
	JsonParser parser = new JsonParser();
	JsonArray array = parser.parse(json).getAsJsonArray();
	String message = gson.fromJson(array.get(0), String.class);
	int number = gson.fromJson(array.get(1), int.class);
	Event event = gson.fromJson(array.get(2), Event.class);
	System.out.printf("Using Gson.fromJson() to get: %s, %d, %s", message,
		number, event);
    }
}

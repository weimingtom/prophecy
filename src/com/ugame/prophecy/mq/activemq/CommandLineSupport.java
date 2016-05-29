package com.ugame.prophecy.mq.activemq;

import java.util.ArrayList;

import org.apache.activemq.util.IntrospectionSupport;

public final class CommandLineSupport {
    private CommandLineSupport() {

    }

    public static String[] setOptions(Object target, String[] args) {
	ArrayList<String> rc = new ArrayList<String>();
	for (int i = 0; i < args.length; i++) {
	    if (args[i] == null) {
		continue;
	    }
	    if (args[i].startsWith("--")) {
		String value = "true";
		String name = args[i].substring(2);
		int p = name.indexOf("=");
		if (p > 0) {
		    value = name.substring(p + 1);
		    name = name.substring(0, p);
		}
		if (name.length() == 0) {
		    rc.add(args[i]);
		    continue;
		}
		String propName = convertOptionToPropertyName(name);
		if (!IntrospectionSupport.setProperty(target, propName, value)) {
		    rc.add(args[i]);
		    continue;
		}
	    }
	}
	String r[] = new String[rc.size()];
	rc.toArray(r);
	return r;
    }

    private static String convertOptionToPropertyName(String name) {
	String rc = "";
	int p = name.indexOf("-");
	while (p > 0) {
	    rc += name.substring(0, p);
	    name = name.substring(p + 1);
	    if (name.length() > 0) {
		rc += name.substring(0, 1).toUpperCase();
		name = name.substring(1);
	    }
	    p = name.indexOf("-");
	}
	return rc + name;
    }
}

package com.ugame.prophecy.protocol.http;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Apache Mina 1
 * @see http://mina.apache.org/
 */
public class HttpRequestMessage {
    private Map<String, String[]> headers = null;

    public void setHeaders(final Map<String, String[]> headers) {
	this.headers = headers;
    }

    public Map<String, String[]> getHeaders() {
	return headers;
    }

    public String getContext() {
	final String[] context = (String[]) headers.get("Context");
	return context == null ? "" : context[0];
    }

    public String getParameter(final String name) {
	final String[] param = (String[]) headers.get("@".concat(name));
	return param == null ? "" : param[0];
    }

    public String[] getParameters(final String name) {
	final String[] param = (String[]) headers.get("@".concat(name));
	return param == null ? new String[] {} : param;
    }

    public String[] getHeader(final String name) {
	return (String[]) headers.get(name);
    }

    @Override
    public String toString() {
	final StringBuilder str = new StringBuilder();
	final Iterator<Entry<String, String[]>> itEntry = headers.entrySet().iterator();
	while (itEntry.hasNext()) {
	    final Entry<String, String[]> entry = (Entry<String, String[]>) itEntry.next();
	    str.append(entry.getKey() + " : "
		    + arrayToString((String[]) entry.getValue(), ',') + "\n");
	}
	return str.toString();
    }

    public static String arrayToString(final String[] str, final char sep) {
	String ret;
	if (str == null || str.length == 0) {
	    ret = "";
	} else {
	    final StringBuffer buf = new StringBuffer();
	    if (str != null) {
		for (int i = 0; i < str.length; i++) {
		    if (i > 0) {
			buf.append(sep);
		    }
		    buf.append(str[i]);
		}
	    }
	    ret = buf.toString();
	}
	return ret;
    }
}

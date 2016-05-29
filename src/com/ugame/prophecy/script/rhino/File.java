package com.ugame.prophecy.script.rhino;

import org.mozilla.javascript.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class File extends ScriptableObject {
    private static final long serialVersionUID = 2549960399774237828L;

    private String name;
    private java.io.File file;
    private LineNumberReader reader;
    private BufferedWriter writer;

    public File() {

    }

    public static Scriptable jsConstructor(Context cx, Object[] args,
	    Function ctorObj, boolean inNewExpr) {
	File result = new File();
	if (args.length == 0 || args[0] == Context.getUndefinedValue()) {
	    result.name = "";
	    result.file = null;
	} else {
	    result.name = Context.toString(args[0]);
	    result.file = new java.io.File(result.name);
	}
	return result;
    }

    @Override
    public String getClassName() {
	return "File";
    }

    public String jsGet_name() {
	return name;
    }

    public Object jsFunction_readLines() throws IOException {
	List<String> list = new ArrayList<String>();
	String s;
	while ((s = jsFunction_readLine()) != null) {
	    list.add(s);
	}
	String[] lines = list.toArray(new String[list.size()]);
	Scriptable scope = ScriptableObject.getTopLevelScope(this);
	Context cx = Context.getCurrentContext();
	return cx.newObject(scope, "Array", lines);
    }

    public String jsFunction_readLine() throws IOException {
	return getReader().readLine();
    }

    public String jsFunction_readChar() throws IOException {
	int i = getReader().read();
	if (i == -1)
	    return null;
	char[] charArray = { (char) i };
	return new String(charArray);
    }

    public static void jsFunction_write(Context cx, Scriptable thisObj,
	    Object[] args, Function funObj) throws IOException {
	write0(thisObj, args, false);
    }

    public static void jsFunction_writeLine(Context cx, Scriptable thisObj,
	    Object[] args, Function funObj) throws IOException {
	write0(thisObj, args, true);
    }

    public int jsGet_lineNumber() throws FileNotFoundException {
	return getReader().getLineNumber();
    }

    public void jsFunction_close() throws IOException {
	if (reader != null) {
	    reader.close();
	    reader = null;
	} else if (writer != null) {
	    writer.close();
	    writer = null;
	}
    }

    @Override
    protected void finalize() {
	try {
	    jsFunction_close();
	} catch (IOException e) {
	}
    }

    public Object jsFunction_getReader() {
	if (reader == null)
	    return null;
	Scriptable parent = ScriptableObject.getTopLevelScope(this);
	return Context.javaToJS(reader, parent);
    }

    public Object jsFunction_getWriter() {
	if (writer == null)
	    return null;
	Scriptable parent = ScriptableObject.getTopLevelScope(this);
	return Context.javaToJS(writer, parent);
    }

    private LineNumberReader getReader() throws FileNotFoundException {
	if (writer != null) {
	    throw Context.reportRuntimeError("already writing file \"" + name
		    + "\"");
	}
	if (reader == null)
	    reader = new LineNumberReader(file == null ? new InputStreamReader(
		    System.in) : new FileReader(file));
	return reader;
    }

    private static void write0(Scriptable thisObj, Object[] args, boolean eol)
	    throws IOException {
	File thisFile = checkInstance(thisObj);
	if (thisFile.reader != null) {
	    throw Context.reportRuntimeError("already writing file \""
		    + thisFile.name + "\"");
	}
	if (thisFile.writer == null)
	    thisFile.writer = new BufferedWriter(
		    thisFile.file == null ? new OutputStreamWriter(System.out)
			    : new FileWriter(thisFile.file));
	for (int i = 0; i < args.length; i++) {
	    String s = Context.toString(args[i]);
	    thisFile.writer.write(s, 0, s.length());
	}
	if (eol)
	    thisFile.writer.newLine();
    }

    /**
     * js> defineClass("File");
     * js> o = {};
     * [object Object]
     * js> o.__proto__ = File.prototype;
     * [object File]
     * js> o.write("hi");
     * js: called on incompatible object
     */
    private static File checkInstance(Scriptable obj) {
	if (obj == null || !(obj instanceof File)) {
	    throw Context.reportRuntimeError("called on incompatible object");
	}
	return (File) obj;
    }
}

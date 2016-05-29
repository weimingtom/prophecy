package com.ugame.prophecy.script.rhino;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;

public class Shell extends ScriptableObject {
    private static final long serialVersionUID = -5638074146250193112L;

    private boolean quitting;
    
    @Override
    public String getClassName() {
        return "global";
    }
    
    public static void main(String args[]) {
        Context cx = Context.enter();
        try {
            Shell shell = new Shell();
            cx.initStandardObjects(shell);
            String[] names = { "print", "quit", "version", "load", "help" };
            shell.defineFunctionProperties(names, Shell.class,
        	    ScriptableObject.DONTENUM);
            args = processOptions(cx, args);
            Object[] array;
            if (args.length == 0) {
                array = new Object[0];
            } else {
                int length = args.length - 1;
                array = new Object[length];
                System.arraycopy(args, 1, array, 0, length);
            }
            Scriptable argsObj = cx.newArray(shell, array);
            shell.defineProperty("arguments", argsObj,
        	    ScriptableObject.DONTENUM);
            shell.processSource(cx, args.length == 0 ? null : args[0]);
        } finally {
            Context.exit();
        }
    }

    public static String[] processOptions(Context cx, String args[]) {
        for (int i=0; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("-")) {
                String[] result = new String[args.length - i];
                for (int j=i; j < args.length; j++) {
                    result[j-i] = args[j];
                }
                return result;
            }
            if (arg.equals("-version")) {
                if (++i == args.length) {
                    usage(arg);
                }
                double d = Context.toNumber(args[i]);
                if (d == Double.NaN) {
                    usage(arg);
                }
                cx.setLanguageVersion((int) d);
                continue;
            }
            usage(arg);
        }
        return new String[0];
    }

    private static void usage(String s) {
        p("Didn't understand \"" + s + "\".");
        p("Valid arguments are:");
        p("-version 100|110|120|130|140|150|160|170");
        System.exit(1);
    }

    public void help() {
        p("");
        p("Command                Description");
        p("=======                ===========");
        p("help()                 Display usage and help messages. ");
        p("defineClass(className) Define an extension using the Java class");
        p("                       named with the string argument. ");
        p("                       Uses ScriptableObject.defineClass(). ");
        p("load(['foo.js', ...])  Load JavaScript source files named by ");
        p("                       string arguments. ");
        p("loadClass(className)   Load a class named by a string argument.");
        p("                       The class must be a script compiled to a");
        p("                       class file. ");
        p("print([expr ...])      Evaluate and print expressions. ");
        p("quit()                 Quit the shell. ");
        p("version([number])      Get or set the JavaScript version number.");
        p("");
    }

    public static void print(Context cx, Scriptable thisObj,
	    Object[] args, Function funObj){
        for (int i=0; i < args.length; i++) {
            if (i > 0) {
                System.out.print(" ");
            }
            String s = Context.toString(args[i]);
            System.out.print(s);
        }
        System.out.println();
    }

    public void quit() {
        quitting = true;
    }

    public static double version(Context cx, Scriptable thisObj,
	    Object[] args, Function funObj) {
        double result = cx.getLanguageVersion();
        if (args.length > 0) {
            double d = Context.toNumber(args[0]);
            cx.setLanguageVersion((int) d);
        }
        return result;
    }

    public static void load(Context cx, Scriptable thisObj,
                            Object[] args, Function funObj)
    {
        Shell shell = (Shell)getTopLevelScope(thisObj);
        for (int i = 0; i < args.length; i++) {
            shell.processSource(cx, Context.toString(args[i]));
        }
    }

    private void processSource(Context cx, String filename) {
        if (filename == null) {
            BufferedReader in = new BufferedReader
                (new InputStreamReader(System.in));
            String sourceName = "<stdin>";
            int lineno = 1;
            boolean hitEOF = false;
            do {
                int startline = lineno;
                System.err.print("js> ");
                System.err.flush();
                try {
                    String source = "";
                    while(true) {
                        String newline;
                        newline = in.readLine();
                        if (newline == null) {
                            hitEOF = true;
                            break;
                        }
                        source = source + newline + "\n";
                        lineno++;
                        if (cx.stringIsCompilableUnit(source)) {
                            break;
                        }
                    }
                    Object result = cx.evaluateString(this, source,
                	    sourceName, startline, null);
                    if (result != Context.getUndefinedValue()) {
                        System.err.println(Context.toString(result));
                    }
                }
                catch (WrappedException we) {
                    System.err.println(we.getWrappedException().toString());
                    we.printStackTrace();
                }
                catch (EvaluatorException ee) {
                    System.err.println("js: " + ee.getMessage());
                }
                catch (JavaScriptException jse) {
                    System.err.println("js: " + jse.getMessage());
                }
                catch (IOException ioe) {
                    System.err.println(ioe.toString());
                }
                if (quitting) {
                    break;
                }
            } while (!hitEOF);
            System.err.println();
        } else {
            FileReader in = null;
            try {
                in = new FileReader(filename);
            }
            catch (FileNotFoundException ex) {
                Context.reportError("Couldn't open file \"" + filename + "\".");
                return;
            }
            try {
                cx.evaluateReader(this, in, filename, 1, null);
            }
            catch (WrappedException we) {
                System.err.println(we.getWrappedException().toString());
                we.printStackTrace();
            }
            catch (EvaluatorException ee) {
                System.err.println("js: " + ee.getMessage());
            }
            catch (JavaScriptException jse) {
                System.err.println("js: " + jse.getMessage());
            }
            catch (IOException ioe) {
                System.err.println(ioe.toString());
            }
            finally {
                try {
                    in.close();
                }
                catch (IOException ioe) {
                    System.err.println(ioe.toString());
                }
            }
        }
    }

    private static void p(String s) {
        System.out.println(s);
    }
}


package com.ugame.prophecy.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class CounterTest {
    public static void main(String[] args) throws Exception {
        Context cx = Context.enter();
        try {
            Scriptable scope = cx.initStandardObjects();
            ScriptableObject.defineClass(scope, Counter.class);
            Scriptable testCounter = cx.newObject(scope, "Counter");
            Object count = ScriptableObject.getProperty(testCounter, "count");
            System.out.println("count = " + count);
            count = ScriptableObject.getProperty(testCounter, "count");
            System.out.println("count = " + count);
            ScriptableObject.callMethod(testCounter,
        	    "resetCount", new Object[0]);
            System.out.println("resetCount");
            count = ScriptableObject.getProperty(testCounter, "count");
            System.out.println("count = " + count);
        } finally {
            Context.exit();
        }
    }

}

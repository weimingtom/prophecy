package com.ugame.prophecy.script.rhino;

import org.mozilla.javascript.*;

public class RunScript2 {
    public static void main(String args[]) {
        Context cx = Context.enter();
        try {
            Scriptable scope = cx.initStandardObjects();
            Object jsOut = Context.javaToJS(System.out, scope);
            ScriptableObject.putProperty(scope, "out", jsOut);
            String s = "";
            for (int i=0; i < args.length; i++) {
                s += args[i];
            }
            Object result = cx.evaluateString(scope, s, "<cmd>", 1, null);
            System.err.println(Context.toString(result));
        } finally {
            Context.exit();
        }
    }

}


package com.ugame.prophecy.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class RunScript3 {
    public static void main(String args[]) {
        Context cx = Context.enter();
        try {
            Scriptable scope = cx.initStandardObjects();
            String s = "";
            for (int i=0; i < args.length; i++) {
                s += args[i];
            }
            cx.evaluateString(scope, s, "<cmd>", 1, null);
            Object x = scope.get("x", scope);
            if (x == Scriptable.NOT_FOUND) {
                System.out.println("x is not defined.");
            } else {
                System.out.println("x = " + Context.toString(x));
            }
            Object fObj = scope.get("f", scope);
            if (!(fObj instanceof Function)) {
                System.out.println("f is undefined or not a function.");
            } else {
                Object functionArgs[] = { "my arg" };
                Function f = (Function)fObj;
                Object result = f.call(cx, scope, scope, functionArgs);
                String report = "f('my args') = " + Context.toString(result);
                System.out.println(report);
            }
        } finally {
            Context.exit();
        }
    }
}


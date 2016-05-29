package com.ugame.prophecy.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Control {
    public static void main(String[] args) {
        Context cx = Context.enter();
        try {
            cx.setLanguageVersion(Context.VERSION_1_2);
            Scriptable scope = cx.initStandardObjects();
            Object result = cx.evaluateString(scope, 
        	    "obj = {a:1, b:['x','y']}",
                    "MySource", 1, null);
            Scriptable obj = (Scriptable) scope.get("obj", scope);
            System.out.println("obj " + (obj == result ? "==" : "!=") + " result");
            System.out.println("obj.a == " + obj.get("a", obj));
            Scriptable b = (Scriptable) obj.get("b", obj);
            System.out.println("obj.b[0] == " + b.get(0, b));
            System.out.println("obj.b[1] == " + b.get(1, b));
            Function fn = (Function) ScriptableObject.getProperty(obj, "toString");
            System.out.println(fn.call(cx, scope, obj, new Object[0]));
        } finally {
            Context.exit();
        }
    }
}

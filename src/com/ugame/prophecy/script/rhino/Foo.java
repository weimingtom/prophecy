package com.ugame.prophecy.script.rhino;

import org.mozilla.javascript.*;

/**
 * js> defineClass("Foo")
 * js> foo = new Foo();         <i>A constructor call, see <a href="#Foo">Foo</a> below.</i>
 * [object Foo]                 <i>The "Foo" here comes from <a href"#getClassName">getClassName</a>.</i>
 * js> foo.counter;             <i>The counter property is defined by the <code>defineProperty</code></i>
 * 0                            <i>call below and implemented by the <a href="#getCounter">getCounter</a></i>
 * js> foo.counter;             <i>method below.</i>
 * 1
 * js> foo.counter;
 * 2
 * js> foo.resetCounter();      <i>Results in a call to <a href="#resetCounter">resetCounter</a>.</i>
 * js> foo.counter;             <i>Now the counter has been reset.</i>
 * 0
 * js> foo.counter;
 * 1
 * js> bar = new Foo(37);       <i>Create a new instance.</i>
 * [object Foo]
 * js> bar.counter;             <i>This instance's counter is distinct from</i>
 * 37                           <i>the other instance's counter.</i>
 * js> foo.varargs(3, "hi");    <i>Calls <a href="#varargs">varargs</a>.</i>
 * this = [object Foo]; args = [3, hi]
 * js> foo[7] = 34;             <i>Since we extended ScriptableObject, we get</i>
 * 34                           <i>all the behavior of a JavaScript object</i>
 * js> foo.a = 23;              <i>for free.</i>
 * 23
 * js> foo.a + foo[7];
 * 57
 * js>
 */

public class Foo extends ScriptableObject {
    private static final long serialVersionUID = -3833489808933339159L;

    private int counter;
    
    public Foo() {
	
    }

    public Foo(int counterStart) {
        counter = counterStart;
    }

    @Override
    public String getClassName() {
        return "Foo";
    }

    public void jsFunction_resetCounter() {
        counter = 0;
    }

    public int jsGet_counter() {
        return counter++;
    }

    public static Object jsFunction_varargs(Context cx, Scriptable thisObj,
	    Object[] args, Function funObj)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("this = ");
        buf.append(Context.toString(thisObj));
        buf.append("; args = [");
        for (int i=0; i < args.length; i++) {
            buf.append(Context.toString(args[i]));
            if (i+1 != args.length)
                buf.append(", ");
        }
        buf.append("]");
        return buf.toString();
    }
}


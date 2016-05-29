package com.ugame.prophecy.script.rhino;

import org.mozilla.javascript.ScriptableObject;

public class Counter extends ScriptableObject {
    private static final long serialVersionUID = 438270592527335642L;
    
    private int count;
    
    public Counter(){ 
	
    }

    public void jsConstructor(int a){ 
	count = a; 
    }

    @Override
    public String getClassName(){ 
	return "Counter"; 
    }

    public int jsGet_count(){ 
	return count++; 
    }

    public void jsFunction_resetCount(){ 
	count = 0; 
    }
}

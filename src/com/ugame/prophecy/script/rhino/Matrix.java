package com.ugame.prophecy.script.rhino;

import org.mozilla.javascript.*;
import java.util.List;
import java.util.ArrayList;

/**
 * js> defineClass("Matrix")
 * js> var m = new Matrix(2); // A constructor call, see "Matrix(int dimension)"
 * js> m                      // Object.toString will call "Matrix.getClassName()"
 * [object Matrix]                     
 * js> m[0][0] = 3;
 * 3
 * js> uneval(m[0]);          // an array was created automatically!
 * [3]
 * js> uneval(m[1]);          // array is created even if we don't set a value
 * []
 * js> m.dim;                 // we can access the "dim" property
 * 2
 * js> m.dim = 3;
 * 3
 * js> m.dim;                 // but not modify the "dim" property
 * 2
 * </pre>
 */
public class Matrix implements Scriptable {
    private int dim;
    private List<Object> list;
    private Scriptable prototype, parent;
    
    public Matrix() {
	
    }

    public Matrix(int dimension) {
        if (dimension <= 0) {
            throw Context.reportRuntimeError(
                  "Dimension of Matrix must be greater than zero");
        }
        dim = dimension;
        list = new ArrayList<Object>();
    }

    @Override
    public String getClassName() {
        return "Matrix";
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return name.equals("dim");
    }

    @Override
    public boolean has(int index, Scriptable start) {
        return true;
    }


    @Override
    public Object get(String name, Scriptable start) {
        if (name.equals("dim")) {
            return new Integer(dim);
        }
        return NOT_FOUND;
    }

    @Override
    public Object get(int index, Scriptable start) {
        while (index >= list.size()) {
            list.add(null);
        }
        Object result = list.get(index);
        if (result != null)
            return result;
        if (dim > 2) {
            Matrix m = new Matrix(dim-1);
            m.setParentScope(getParentScope());
            m.setPrototype(getPrototype());
            result = m;
        } else {
            Context cx = Context.getCurrentContext();
            Scriptable scope = ScriptableObject.getTopLevelScope(start);
            result = cx.newArray(scope, 0);
        }
        list.set(index, result);
        return result;
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
	
    }

    @Override
    public void put(int index, Scriptable start, Object value) {
	
    }

    @Override
    public void delete(String id) {
	
    }

    @Override
    public void delete(int index) {
	
    }

    @Override
    public Scriptable getPrototype() {
        return prototype;
    }

    @Override
    public void setPrototype(Scriptable prototype) {
        this.prototype = prototype;
    }

    @Override
    public Scriptable getParentScope() {
        return parent;
    }

    @Override
    public void setParentScope(Scriptable parent) {
        this.parent = parent;
    }

    @Override
    public Object[] getIds() {
        return new Object[0];
    }

    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        return "[object Matrix]";
    }

    @Override
    public boolean hasInstance(Scriptable value) {
        Scriptable proto = value.getPrototype();
        while (proto != null) {
            if (proto.equals(this))
                return true;
            proto = proto.getPrototype();
        }

        return false;
    }
}

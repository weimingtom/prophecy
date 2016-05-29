package com.ugame.prophecy.script.luajava;

import com.ugame.prophecy.global.GlobalConfig;

import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;

public final class LoadLibraries {
    /**
     * Import Java functions into lua state
     * 
     * @param luaState
     * @throws LuaException
     */
    public static void open(final LuaState luaState) throws LuaException {
	luaState.getGlobal(GlobalConfig.LUA_LIB_NAME);
	if (luaState.isNil(-1)) {
	    luaState.pop(1);
	    luaState.newTable();
	    luaState.pushValue(-1);
	    luaState.setGlobal(GlobalConfig.LUA_LIB_NAME);
	}
	luaState.pushString(TraceError.class.getSimpleName());
	luaState.pushJavaFunction(new TraceError(luaState));
	luaState.setTable(-3);
	luaState.pop(1);
    }

    // -------------------------------------------------
    private LoadLibraries() {

    }
}

package com.ugame.prophecy.script.luajava;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalData;
import com.ugame.prophecy.log.CommonSysLog;

public class TraceError extends JavaFunction {
    private static final Logger LOGGER = LoggerFactory.getLogger(TraceError.class);

    public TraceError(final LuaState arg0) {
	super(arg0);
    }

    @Override
    public int execute() throws LuaException {
	final String msg = this.getParam(2).toString();
	CommonSysLog.info(LOGGER, msg);
	System.gc();
	CommonSysLog.output("[login num:" + GlobalData.getNumLogin() + "]" + msg);
	return 0;
    }
}

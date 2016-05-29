package com.ugame.prophecy.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import org.slf4j.Logger;

import com.ugame.prophecy.global.GlobalConfig;

public class CommonSysLog {    
    /**
     * FIXME:
     * 	StringBuffers can grow quite a lot, 
     * 	and so may become a source of memory leak 
     * 	(if the owning class has a long life time).
     * 
     * NOTE:
     *  注意Eclipse的库加载顺序（Java Build Path->Order and Export）
     *  slf4j-log4j12-1.6.1.jar应该放在
     *  log4j-1.2.16.jar前面，否则会出错
     *  （java -classpath同理）
     * 
     */
    public static StringBuffer outputBuffer = new StringBuffer();
    
    private static Display display = null;
    private static Text output = null;
    
    /**
     * 打在界面上
     * @param str
     */
    public static void output(final String str) {
	if(GlobalConfig.outputType == GlobalConfig.TIMER) {
	    synchronized(outputBuffer) {
	    	outputBuffer.append(getTimeString());
	    	outputBuffer.append(str);
	    	outputBuffer.append('\n');
	    }
	} else if(GlobalConfig.outputType == GlobalConfig.IMME){
	    /**
	     * NOTE: Avoid 'Invalid thread access' error. 
	     * Don't access UI directly.
	     */
	    if (output != null && !output.isDisposed() && display != null
		    && !display.isDisposed()) {
    	    	display.asyncExec(new Runnable() {
    	    	    @Override
    	    	    public void run() {
    	    		output.append(getTimeString() + str + "\n");
    	    	    }
    	    	});
	    }
	}
    }
    
    public static void error(Logger logger, String str, Throwable ex) {
	if(GlobalConfig.showLogger) {
	    output(str);
	}
	logger.error(str, ex);
    }

    public static void info(Logger logger, String str) {
	if(GlobalConfig.showLogger) {
	    output(str);
	}
	logger.info(str);
    }
    
    public static void setOutput(final Text text) {
	output = text;
    }

    public static void setDisplay(final Display disp) {
	display = disp;
    }

    public static String getTimeString() {
	return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS:", Locale
		.getDefault()).format(new Date(System.currentTimeMillis()));
    }
}

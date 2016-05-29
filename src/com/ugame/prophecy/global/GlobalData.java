package com.ugame.prophecy.global;

/**
 * 与游戏有关的全局调用
 * 
 * @author Administrator
 * 
 */
public final class GlobalData {
    private static Object numLoginLock = new Object();
    private static int numLogin = 0;
    private static int idLogin = 0;
    
    public static int addSession() {
	return addNumLogin();
    }

    public static void removeSession() {
	removeNumLogin();
    }
    
    private static int addNumLogin() {
	synchronized (numLoginLock) {
	    numLogin++;
	    idLogin++;
	    return idLogin;
	}
    }

    private static void removeNumLogin() {
	synchronized (numLoginLock) {
	    numLogin--;
	}
    }
    
    //TODO:这个是否需要锁？
    public static int getNumLogin() {
	synchronized (numLoginLock) {
	    return numLogin;
	}
    }
    
    // ---------------------------------------------

    private GlobalData() {

    }
}

package com.ugame.prophecy.ui;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ugame.prophecy.global.GlobalConfig;
import com.ugame.prophecy.script.luajava.LoadLibraries;
import com.ugame.prophecy.log.CommonSysLog;
import com.ugame.prophecy.protocol.tcp.IPackServer;
import com.ugame.prophecy.protocol.tcp.PackServerFactory;

/**
 * 主启动界面
 * @author Administrator
 *
 */
public class GUIMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(GUIMain.class);
    //参数
    private final static int SHELL_WIDTH = 862;
    private final static int SHELL_HEIGHT = 676;
    private final static String ICON_NAME = "icon.gif";
    private final static int TIMER_INTERVAL = 200;
    //窗体
    public transient Shell shell;
    public transient CTabFolder tabFolder;
    public transient CTabItem logItem;
    public transient CTabItem settingItem;
    public transient LogTab logTab;
    public transient Shell dlgImme;
    //控件
    public transient Button button1;
    public transient Button button2;
    public transient Button button3;
    public transient Button button4;
    public transient Label label1;
    public transient Text text1;
    //Lua状态机
    public transient LuaState luaState = LuaStateFactory.newLuaState();
    //服务器实例
    private transient IPackServer server;

    public GUIMain() {
	server = PackServerFactory.newServer(GlobalConfig.serverType);
    }
    
    /**
     * 
     * @throws IOException
     * @throws LuaException
     */
    public void init() throws IOException, LuaException {
	//Lua配置
	luaState.openLibs();
	LoadLibraries.open(luaState);
	//显示
	final Display display = new Display();
	CommonSysLog.setDisplay(display);
	//窗口
	shell = new Shell(display, SWT.MAX | SWT.MIN | SWT.CLOSE | SWT.TITLE
		| SWT.RESIZE);
	shell.setText("GameCenter");
	shell.setLayout(new FormLayout());
	shell.setImage(new Image(Display.getCurrent(), GUIMain.class.getResourceAsStream(ICON_NAME)));
	//控件
	tabFolder = new CTabFolder(shell, 0);
	tabFolder.setLayoutData(new GridData(1808));
	logItem = new CTabItem(tabFolder, 0);
	logTab = new LogTab(tabFolder);
	logItem.setControl(logTab);
	logItem.setText("Server Logs");
	settingItem = new CTabItem(tabFolder, 0);
	settingItem.setText("Server Settings");
	button1 = new Button(shell, SWT.PUSH);
	button1.setText("&Clean");
	button1.addListener(SWT.Selection, new Listener() {
	    @Override
	    public void handleEvent(final Event event) {
		logTab.logOutput.setText("");
	    }
	});
	button2 = new Button(shell, SWT.PUSH);
	button2.setText("&Reload");
	button2.addListener(SWT.Selection, new Listener() {
	    @Override
	    public void handleEvent(final Event event) {
		CommonSysLog.output("Run Script...");
		final int err = luaState.LdoFile("script/main.lua");
		if (err != 0) {
		    switch (err) {
		    case 1:
			CommonSysLog.error(LOGGER, "Runtime error. " + luaState.toString(-1), null);
			break;

		    case 2:
			CommonSysLog.error(LOGGER, "File not found. " + luaState.toString(-1), null);
			break;

		    case 3:
			CommonSysLog.error(LOGGER, "Syntax error. " + luaState.toString(-1), null);
			break;

		    case 4:
			CommonSysLog.error(LOGGER, "Memory error. " + luaState.toString(-1), null);
			break;

		    default:
			CommonSysLog.error(LOGGER, "Error. " + luaState.toString(-1), null);
			break;
		    }
		}
	    }
	});
	button3 = new Button(shell, SWT.PUSH);
	button3.setText("Run &Script");
	button3.addListener(SWT.Selection, new Listener() {
	    @Override
	    public void handleEvent(final Event event) {
		if (dlgImme != null && dlgImme.isDisposed()) {
		    dlgImme = null;
		}
		if (dlgImme == null) {
		    dlgImme = new Shell(shell, SWT.MAX | SWT.MIN | SWT.CLOSE
			    | SWT.TITLE | SWT.RESIZE | SWT.MODELESS);
		    dlgImme.setSize(648, 521);
		    dlgImme.setText("Immediate Window");
		    dlgImme.setLayout(new FormLayout());
		    final Text memoImme = new Text(dlgImme, SWT.BORDER
			    | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		    memoImme.setBackground(new Color(Display.getCurrent(),
			    new RGB(0, 0, 0x80)));
		    memoImme.setForeground(new Color(Display.getCurrent(),
			    new RGB(0xff, 0xff, 0)));
		    memoImme.setText("ugame.TraceError(" + "'Hello World')");
		    final Combo combo = new Combo(dlgImme, SWT.READ_ONLY);
		    combo.setItems(new String[] { "User manager service" });
		    final Button buttonImmeClose = new Button(dlgImme, SWT.PUSH);
		    buttonImmeClose.setText("&Close");
		    buttonImmeClose.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
			    dlgImme.close();
			}
		    });
		    final Button buttonImmeRun = new Button(dlgImme, SWT.PUSH);
		    buttonImmeRun.setText("&Run");
		    buttonImmeRun.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
			    final int err = luaState.LdoString(
				    memoImme.getText());
			    if (err != 0) {
				switch (err) {
				case 1:
				    CommonSysLog.error(LOGGER, "Runtime error. " + luaState.toString(-1), null);
				    break;

				case 2:
				    CommonSysLog.error(LOGGER, "File not found. " + luaState.toString(-1), null);
				    break;

				case 3:
				    CommonSysLog.error(LOGGER, "Syntax error. " + luaState.toString(-1), null);
				    break;

				case 4:
				    CommonSysLog.error(LOGGER, "Memory error. " + luaState.toString(-1), null);
				    break;

				default:
				    CommonSysLog.error(LOGGER, "Error. " + luaState.toString(-1), null);
				    break;
				}
			    }
			}
		    });
		    //TAB页布局
		    FormData data;
		    data = new FormData();
		    data.bottom = new FormAttachment(100, -5);
		    data.right = new FormAttachment(100, -5);
		    buttonImmeClose.setLayoutData(data);
		    data = new FormData();
		    data.bottom = new FormAttachment(100, -5);
		    data.right = new FormAttachment(buttonImmeClose, -5);
		    buttonImmeRun.setLayoutData(data);
		    data = new FormData();
		    data.bottom = new FormAttachment(100, -5);
		    data.left = new FormAttachment(0, 5);
		    combo.setLayoutData(data);
		    data = new FormData();
		    data.top = new FormAttachment(0, 5);
		    data.left = new FormAttachment(0, 5);
		    data.bottom = new FormAttachment(buttonImmeClose, -5);
		    data.right = new FormAttachment(100, -5);
		    memoImme.setLayoutData(data);
		    //立即窗口
		    final Point dlgSize = dlgImme.getSize();
		    final Rectangle disRect = Display.getCurrent().getBounds();
		    dlgImme.setLocation((disRect.width - dlgSize.x) / 2,
			    (disRect.height - dlgSize.y) / 2);
		    dlgImme.open();
		}
	    }
	});
	//界面控件
	label1 = new Label(shell, SWT.NONE);
	label1.setText("Max Line Count : ");
	text1 = new Text(shell, SWT.BORDER);
	text1.setText("100000");
	button4 = new Button(shell, SWT.CHECK);
	button4.setText("&Shown on Screen");
	button4.setSelection(true);
	//界面布局
	FormData data;
	data = new FormData();
	data.bottom = new FormAttachment(100, -5);
	data.right = new FormAttachment(100, -5);
	button1.setLayoutData(data);
	data = new FormData();
	data.bottom = new FormAttachment(100, -5);
	data.right = new FormAttachment(button1, -5);
	button2.setLayoutData(data);
	data = new FormData();
	data.bottom = new FormAttachment(100, -5);
	data.right = new FormAttachment(button2, -5);
	button3.setLayoutData(data);
	data = new FormData();
	data.bottom = new FormAttachment(100, -10);
	data.left = new FormAttachment(0, 5);
	label1.setLayoutData(data);
	data = new FormData();
	data.bottom = new FormAttachment(100, -5);
	data.left = new FormAttachment(label1, 5);
	text1.setLayoutData(data);
	data = new FormData();
	data.bottom = new FormAttachment(100, -5);
	data.left = new FormAttachment(text1, 15);
	button4.setLayoutData(data);
	data = new FormData();
	data.top = new FormAttachment(0, 5);
	data.left = new FormAttachment(0, 5);
	data.bottom = new FormAttachment(button1, -5);
	data.right = new FormAttachment(100, -5);
	tabFolder.setLayoutData(data);
	if(GlobalConfig.outputType == GlobalConfig.TIMER) {
	    //用定时器优化日志输出
	    display.timerExec(TIMER_INTERVAL, new Runnable() {
		@Override
    		public void run() {
		    //虽然StringBuffer线程安全，
		    //但在读取toString()和setLength(0)清空之间
		    //存在交错执行的append输入
		    //所以要锁住防止交错
		    synchronized(CommonSysLog.outputBuffer) {
			if(CommonSysLog.outputBuffer.length() > 0) {
			    if (!logTab.logOutput.isDisposed()) {
				logTab.logOutput.append(CommonSysLog.outputBuffer.toString()); 
			    }
			    CommonSysLog.outputBuffer.setLength(0);
			}
		    }
		    display.timerExec(TIMER_INTERVAL, this);
		}
	    });
	}
	//
	setShellCenter(SHELL_WIDTH, SHELL_HEIGHT);
	shell.open();
	try {
    	    //启动服务器
	    server.start();
    	    while (!shell.isDisposed()) {
    		if (!display.readAndDispatch()) {
    		    display.sleep();
    		}
    	    }
    	} catch (Exception ex){
    	   CommonSysLog.error(LOGGER, "server start error.", ex);
    	}
    }
    
    /**
     * 居中
     * @param posX
     * @param posY
     */
    public final void setShellCenter(final int posX, final int posY) {
	final Rectangle rect = shell.getDisplay().getBounds();
	if (rect.width > posX) {
	    if (rect.height > posY) {
		shell.setSize(posX, posY);
		shell.setLocation((rect.width - posX) / 2,
			(rect.height - posY) / 2);
	    } else {
		shell.setSize(posX, posY);
		shell.setLocation((rect.width - posX) / 2, rect.height);
	    }
	} else if (rect.height > posY) {
	    shell.setSize(posX, posY);
	    shell.setLocation(rect.width, (rect.height - posY) / 2);
	} else {
	    shell.setSize(posX, posY);
	    shell.setLocation(rect.width, rect.height);
	}
    }

    /**
     * 主入口
     * @param args
     */
    public final static void main(final String[] args) {
	final GUIMain gui = new GUIMain();
	try {
	    gui.init();
	} catch (Exception e) {
	    PrintWriter writer = null;
	    try {
		writer = new PrintWriter(new BufferedWriter(new FileWriter(
			GlobalConfig.ERROR_LOG_PATH)));
		writer.write(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale
			.getDefault()).format(new Date(System
			.currentTimeMillis()))
			+ "\r\n");
		e.printStackTrace(writer);
		writer.write("\r\n");
	    } catch (IOException ex) {
		do {
		} while (false);
	    } finally {
		writer.close();
	    }
	    final Display display = Display.getCurrent();
	    if (display != null) {
		final MessageBox messageBox = new MessageBox(
			new Shell(display), 33);
		messageBox.setText("Error");
		messageBox.setMessage(e.toString());
		messageBox.open();
	    }
	} finally {
	    //关闭服务器
	    try {
		if(gui.server != null) {
		    gui.server.stop();
		}
	    } catch (Exception ex) {
		CommonSysLog.error(LOGGER, "server stop error", ex);
	    } finally {
		//销毁图形界面
    	    	final Display display = Display.getCurrent();
    	    	if (display != null && !display.isDisposed()) {
    	    	    try {
    	    		display.dispose();
    	    	    } catch (SWTException e) {
    	    		e.printStackTrace();
    	    	    }
    	    	}
    	    	//以防万一，强行退出
    	    	System.exit(0);
	    }
	}
    }
}

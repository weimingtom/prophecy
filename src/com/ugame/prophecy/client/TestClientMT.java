package com.ugame.prophecy.client;

/**
 * See also:
 * 	http://www.javalobby.org/forums/thread.jspa?threadID=16867&tstart=0
 */
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.Vector;
import javax.swing.JFrame;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.Math;
import java.net.Socket;

/**
 * MyGameClientEx 是 MyGameClient的多线程版
 * 
 * @author Administrator
 * 
 */
public class TestClientMT {
    static final long serialVersionUID = 0L;
    public static final int WINDOW_WIDTH = 800; // 窗口长度
    public static final int WINDOW_HEIGHT = 600; // 窗口宽度

    private static final boolean USE_JAR_IMAGE = true;
    private static final String IMG_DIR = "assets/LB.PNG"; // 角色图片
    private static final String IMG_DIR2 = "LB.PNG";

    private static final int CHARACTER_COUNT = 10; // 角色多少
    private static final int TIMER_INTEVAL = 2; // 定时器间隔毫秒数
    private static final int FPS = 10; // fps（如果角色数量太大，这个数值决定CPU占有量）
    private static final int FRAME_DELAY = 1000 / FPS; // 屏幕刷新间隔毫秒数
    // 20ms. implies 50fps (1000/20) = 50

    public static final int RATE_FACT = 20; // 角色的速度因子

    public static void main(String[] args) {
	JFrame frame = new JFrame("My Game Demo");
	Canvas gui = new Canvas();
	frame.getContentPane().add(gui);
	frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
	// 居中
	frame.setLocationRelativeTo(null);
	frame.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		System.exit(0); // 自动关闭所有while(true)循环，但不考虑资源释放（在这里没有问题）
	    }
	});
	GameLoop game = new GameLoop(gui);
	Thread gameThread = new Thread(game);
	gameThread.setPriority(Thread.MIN_PRIORITY);
	frame.setVisible(true); // start AWT painting.
	// 必须在窗口出现之后启动
	gameThread.start(); // start Game processing.
	// 创建多线程（继承thread）
	for (int i = 0; i < CHARACTER_COUNT; i++) {
	    bench b = new bench(i, game.points);
	    b.start();
	}
    }

    /**
     * 游戏主线程，负责UI
     * 
     * @author Administrator
     * 
     */
    private static class GameLoop implements Runnable {
	private boolean isRunning;
	private Canvas gui;
	private long cycleTime;
	private long lastTime;

	/**
	 * <p>
	 * Description: Return an Image based on the supplied image identifier.
	 * The image is assumed to reside at the defined location within the
	 * same repository as this class.
	 * 
	 * @see http://www.jspcn.net/htmlnews/11500614567031131.html
	 */
	private static Image getImageFromJar(final String imageId, Class c) {
	    Image image = null;
	    final InputStream inputStream = c.getResourceAsStream(imageId);
	    if (inputStream != null) {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
		    final byte[] bytes = new byte[1024];
		    int read = 0;
		    while ((read = inputStream.read(bytes)) >= 0) {
			byteArrayOutputStream.write(bytes, 0, read);
		    }
		    image = Toolkit.getDefaultToolkit().createImage(
			    byteArrayOutputStream.toByteArray());
		} catch (IOException exception) {
		    exception.printStackTrace();
		}
	    }
	    return image;
	}

	public GameLoop(Canvas canvas) {
	    gui = canvas;
	    isRunning = true;
	    gui.addMouseListener(new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
		    onMousePressed(e);
		}
	    });
	    if (USE_JAR_IMAGE) {
		imgCharacter = getImageFromJar(IMG_DIR2, GameLoop.class);
	    } else {
		imgCharacter = Toolkit.getDefaultToolkit().createImage(IMG_DIR);
	    }
	    for (int i = 0; i < CHARACTER_COUNT; i++) {
		Player c = new Player();
		c.setPoint(new Point(0, 0));
		c.setTargetPoint(new Point(0, 0));
		points.add(c);
	    }
	}

	@Override
	public void run() {
	    cycleTime = System.currentTimeMillis();
	    gui.createBufferStrategy(2);
	    BufferStrategy strategy = gui.getBufferStrategy();
	    // Game Loop
	    while (isRunning) {
		if (System.currentTimeMillis() - lastTime > TIMER_INTEVAL) {
		    onTimer();
		    lastTime = System.currentTimeMillis();
		}
		updateGameState();
		updateGUI(strategy);
		synchFramerate();
	    }
	}

	private void synchFramerate() {
	    cycleTime = cycleTime + FRAME_DELAY;
	    long difference = cycleTime - System.currentTimeMillis();
	    try {
		Thread.sleep(Math.max(0, difference));
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}

	public void shutdown() {
	    isRunning = false;
	}

	/**
	 * 逻辑入口
	 */
	private void updateGameState() {

	}

	/**
	 * UI入口
	 */
	private void updateGUI(BufferStrategy strategy) {
	    Graphics g = strategy.getDrawGraphics();
	    g.setColor(Color.WHITE);
	    g.fillRect(0, 0, gui.getWidth(), gui.getHeight());
	    g.setColor(Color.BLACK);
	    // //////////////////////////////////////
	    // arbitrary rendering logic
	    // g.drawString("Hello, world!", 0, 10);
	    g.setColor(Color.RED);
	    for (int i = 0; i < points.size(); i++) {
		for (int index = 0; index < points.size(); index++) {
		    Point p = (Point) (points.elementAt(index).getPoint());
		    g.drawImage(imgCharacter, p.x, p.y, gui);
		    g.drawString("角色" + index, p.x, p.y);
		}
	    }
	    // //////////////////////////////////////
	    g.dispose();
	    strategy.show();
	}

	/**
	 * 事件入口
	 */
	public void onMousePressed(MouseEvent e) {
	    // points.add( new Point(e.getX(), e.getY()));
	    // for(int i = 0; i < points.size(); i++)
	    // points.get(i).setTargetPoint(new Point(e.getX(), e.getY()+ i *
	    // 100));
	    // 使用刷屏线程后不需要repaint()，避免闪屏
	    // gui.repaint();
	}

	/**
	 * 可以在这里模拟多客户端，但为了方便起见和使用单独线程， 这里的操作挪到bench.onTimer()完成
	 */
	public void onTimer() {
	    // for(int i = 0; i < points.size(); i++) {
	    // points.elementAt(i).move();
	    // }
	}

	// 变量
	private Vector<Player> points = new Vector<Player>();
	private Image imgCharacter;
    }

    /**
     * 测试线程类，用于模拟每个客户端（角色的操作） Test code per thread.
     */
    private static class bench extends Thread {
	private int threadNum;
	private boolean isRunning;
	private long cycleTime;
	private Vector<Player> points;

	public bench(int threadNum, Vector<Player> points) {
	    this.threadNum = threadNum;
	    this.points = points;
	    this.isRunning = true;
	}

	@Override
	public void run() {
	    // run不会阻塞线程,所以在这里连接socket
	    startConnect();
	    long lastTime = System.currentTimeMillis();
	    cycleTime = System.currentTimeMillis();
	    if (points == null) {
		return;
	    }
	    // Test Loop
	    while (isRunning) {
		if (System.currentTimeMillis() - lastTime > TIMER_INTEVAL) {
		    onTimer();
		    lastTime = System.currentTimeMillis();
		}
		synchFramerate();
	    }
	}

	private void onTimer() {
	    // 假设其线程安全
	    Player ch = points.elementAt(threadNum);
	    boolean isChanged = ch.move();
	    if (isChanged) {
		String strMsg = "setlocation," + this.threadNum + ","
			+ ch.getTargetPoint().x + "," + ch.getTargetPoint().y;
		sendMsg(strMsg);
	    }
	}

	private void synchFramerate() {
	    cycleTime = cycleTime + FRAME_DELAY;
	    long difference = cycleTime - System.currentTimeMillis();
	    try {
		Thread.sleep(Math.max(0, difference));
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}

	/**
	 * 每个线程有独立的连接 
	 * 
	 * 注意，如果连接建立失败，这个方法会阻塞，所以最好放在run中处理
	 */
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	public final static int DEFAULT_PORT = 1234;
	private boolean bConnected;

	public void startConnect() {
	    bConnected = false;
	    try {
		socket = new Socket("127.0.0.1", DEFAULT_PORT);
		bConnected = true;
		System.out.println("Connection OK");
		in = new BufferedReader(new InputStreamReader(socket
			.getInputStream()));
		out = new PrintWriter(socket.getOutputStream());
	    } catch (IOException e) {
		// e.printStackTrace();
		bConnected = false;
		System.out.println("Connection failed");
	    }
	}

	/**
	 * 测试交互，这里用readLine可以制造阻塞
	 * 
	 * @param str
	 */
	public void sendMsg(String str) {
	    try {
		if (bConnected) {
		    out.println(str);
		    // 注意！！！如果不刷新，将不发送，所谓的Nagle算法
		    out.flush();
		    String strRead = in.readLine();
		    // System.out.println(str);
		    if (strRead != null) {
			// System.out.println(strRead);
		    }
		}
	    } catch (Exception e) {
		bConnected = false;
		// e.printStackTrace();
		System.out.println("Write/Read failed");
	    }
	}
    }
}

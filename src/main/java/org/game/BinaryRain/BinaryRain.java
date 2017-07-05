package org.game.BinaryRain;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * 数字彩虹雨
 *
 */
public class BinaryRain extends JDialog {
	
	private static final long serialVersionUID = 1L;
	
	private Color foreground, background;// 前景色、背景色
	private Dimension size;//Dimension类封装单个对象中组件的高度和宽度（精确到整数）
	private char[] RAIN_CHARACTERS;// 字符数组
	private boolean isColorful;// 颜色是否铺满
	private boolean hasMusic;// 是否播放音乐
	private AudioClip music;// 音频对象
	private boolean isStart = false;// 是否开始
	private RainPanel panel = new RainPanel();// RainPanel对象
	private ArrayList<Rain> rains = new ArrayList<Rain>();
	private Font rainFont = new Font("arial", Font.BOLD, 15);;// 创建文字雨的字体
	
	/**
	 * 通过构造方法初始化
	 */
	private BinaryRain() {
		
		try {
			initProperties();
			init();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Failed to init.\n" + e, 
					"BinaryRain", JOptionPane.ERROR_MESSAGE);
			System.exit(1);// 非正常退出
		}
		
	}
	
	
	/**
	 * 读取配置文件并初始化
	 * 
	 * @throws Exception
	 */
	private void initProperties() throws Exception {
		
		Properties props = new Properties(); 
		File file = new File(System.getProperty("user.dir") 
				+ "/src/main/resources/BinaryRain.properties");// 获取配置文件
		boolean dw = true, dh = true, df =true, db = true, dc = true, dcf = true;

		if (file.exists() && file.isFile()) {
			InputStream fis = new FileInputStream(file);// 创建文件输入流
			props.load(fis);// 加载属性文件

			// 获取前景色，默认default
			String strFore = props.getProperty("foreground", "default").toLowerCase();
			if (!"default".equals(strFore)) {
				df = false;
				foreground = getColor(strFore);// 获取颜色
				if (foreground == null) {
					foreground = Color.getColor(strFore, Color.GREEN);// 获取颜色对象，默认绿色
				}
			}
			
			// 获取背景色，默认default
			String strBack = props.getProperty("background", "default").toLowerCase();
			if (!"default".equals(strBack)) {
				db = false;
				background = getColor(strBack);// 获取颜色
				if (background == null) {
					background = Color.getColor(strBack, Color.GREEN);// 获取颜色对象，默认绿色
				}
			}
			
			// 获取宽度
			size = new Dimension();
			String strW = props.getProperty("width", "default").toLowerCase();
			if (!"default".equals(strW)) {
				dw = false;
				size.width = Integer.valueOf(strW);
			}
			
			// 获取高度
			String strH = props.getProperty("height", "default").toLowerCase();
			if (!"default".equals(strH)) {
				dh = false;
				size.width = Integer.valueOf(strH);
			}
			
			// 获取字符数组
			String strC = props.getProperty("characters", "default");
			if (!"default".equalsIgnoreCase(strC)) {
				dc = false;
				String[] cs = strC.split(",");
				RAIN_CHARACTERS = new char[cs.length];
				for (int i = 0, s = RAIN_CHARACTERS.length; i < s; i++) {
					RAIN_CHARACTERS[i] = cs[i].charAt(0);
				}
			}
			
			// 判断颜色是否铺满
			String strCF = props.getProperty("colorful", "default");
			if (!"default".equalsIgnoreCase(strCF)) {
				dcf = false;
				isColorful = Boolean.valueOf(strCF);
			}
			
			// 判断是否播放音乐
			String strM = props.getProperty("music", "default");
			if (!"default".equalsIgnoreCase(strM)) {
				File musicFile = new File(System.getProperty("user.dir") 
						+ "/src/main/resources/" + strM);
				if (musicFile.exists() && musicFile.isFile()) {
					if ((music = Applet.newAudioClip(musicFile.toURI().toURL())) != null) {
						hasMusic = true;
					}
				}
			}
			
			fis.close();
			
		}
		
		if (dw & dh) {// 高度和宽度都是default，获取屏幕高和宽
			size = Toolkit.getDefaultToolkit().getScreenSize();
		} else if (dw) {// 宽度是default，获取屏幕宽度
			size.width = Toolkit.getDefaultToolkit().getScreenSize().width;
		} else if (dh) {// 高度是default，获取屏幕高度
			size.height = Toolkit.getDefaultToolkit().getScreenSize().height;
		}
		
		if (df) {// 前景色是default
			foreground = Color.GREEN;
		}
		
		if (db) {// 背景色是default
			background = Color.BLACK;
		}
		
		if (dc) {// 字符数组是的default
			RAIN_CHARACTERS = new char[126 - 33 + 1];
			for (int c = 0, i = 33, l = RAIN_CHARACTERS.length; c < l; c++, i++) {
				RAIN_CHARACTERS[c] = (char) i;
			}
		}
		
		if (dcf) {// 颜色铺满是default
			isColorful = false;
		}
		
	}
	
	/**
	 * 根据字符串获取其对应的颜色
	 * 
	 * @param color
	 * 				颜色
	 * @return
	 */
	private Color getColor(String color) {
		
		if (color == null || color.isEmpty()) {
			return null;
		}
		if (color.startsWith("#")) {
			int i = Integer.valueOf(color.substring(1), 16);
			return new Color(i);
		}
		// [\\d]:数字		[\\p{Blank}]：空格或制表符
		if (color.matches("[\\d]+[\\p{Blank}]*,[\\p{Blank}]*[\\d]+[\\p{Blank}]*,[\\p{Blank}]*[\\d]+")) {
			String[] cs = color.split("[\\p{Blank}]*,[\\p{Blank}]");
			if (cs.length != 3) {
				return null;
			}
			int r = Integer.valueOf(cs[0]);
			int g = Integer.valueOf(cs[1]);
			int b = Integer.valueOf(cs[2]);
			return new Color(r, g, b);
		}
		return null;
		
	}
	
	/**
	 * 初始化窗口
	 */
	private void init() {
		
		setAlwaysOnTop(true);// 设置窗口靠前
		setResizable(false);// 不能改变大小
		setUndecorated(true);// 设置此frame窗口失去边框和标题栏的修饰（必须在setVisible之前）
		setTitle("Binary Rain");// 设置标题
		// 创建一个BufferedImage对象
		BufferedImage cursor = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB_PRE);
		setCursor(Toolkit.getDefaultToolkit().createCustomCursor(cursor, 
				new Point(8, 8), "Disable Cursor"));// 确认光标的形状
		setSize(size);// 设置窗口大小
		setLocationRelativeTo(null);// 设置窗口相对于指定组件的位置，null表示位于屏幕的中央
		
		addKeyListener(new KeyAdapter() {// 新增一个按键侦听器
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.isAltDown() && e.getKeyCode() == KeyEvent.VK_F4) 
						|| (e.getKeyCode() == KeyEvent.VK_ESCAPE)) {// 按下Alt+F4或者Esc键
					setVisible(false);// 设置窗口不可见
					System.exit(0);// 正常停止程序
				}
			}
		});
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (isRaining()) {
					stop();
				}
				System.exit(0);// 正常停止程序
			}
		});
		
		add(panel, BorderLayout.CENTER);// 将指定组件添加到此容器
		
	}
	
	/**
	 * 重写setVisible方法，当不显示时停止创建文字雨
	 */
	public void setVisible(boolean flag) {
		
		super.setVisible(flag);
		if (!flag) {
			stop();
		}
		
	}
	
	/**
	 * 是否开始创建文字雨
	 * 
	 * @return
	 */
	public boolean isRaining() {
		
		return isStart;
		
	}
	
	/**
	 * 停止文字雨
	 */
	public void stop() {
		
		isStart = false;// 开始标识置为false
		if (hasMusic) {// 播放音乐
			music.stop();// 停止播放音乐
		}
		
	}
	
	/**
	 * 开始一个新的线程，创建一条文字雨，使用synchronized保证一个时间内只有一条线程可以执行
	 */
	private synchronized void newRain() {
		
		Rain r = new Rain(getRandomLength(), (int) (Math.random() * size.width), 
				(int) (Math.random() * -60 * 15), (int) (Math.random() * 8 + 2), 
				(float) (Math.random() * 10 + 10));
		rains.add(r);
		new Thread(r).start();
		
	}
	
	/**
	 * 获取随机长度(10-50)
	 * 
	 * @return
	 */
	public int getRandomLength() {
		
		return (int) (Math.random() * 40 + 10);
		
	}
	
	/**
	 * 获取随机字符串
	 * 
	 * @return
	 */
	public String getRandomChar() {
		
		return String.valueOf(RAIN_CHARACTERS[(int) (Math.random() * RAIN_CHARACTERS.length)]);
		
	}
	
	/**
	 * 获取面板大小
	 * 
	 * @return
	 */
	public Dimension getFrameSize() {
		
		return size;
		
	}
	
	public void start() {
		
		if (hasMusic) {// 播放音乐
			music.loop();// 循环播放
		}
		
		for (int c = 0, s = 108; c < s; c++) {// 创建108条文字雨
			newRain();
		}
		
		isStart = true;
		
		for (Rain r: rains) {
			new Thread(r).start();
		}
		
		new Thread(new Runnable() {
			
			public void run() {
			
				while (isStart) {
					panel.repaint();
				}
				
			}
		}).start();
		
	}
	
	/**
	 * 创建文字雨面板
	 *
	 */
	private final class RainPanel extends JPanel {
		
		private static final long serialVersionUID = 1L;

		public RainPanel() {
			
		}

		private final Color[] COLORS = new Color[] {// 文字雨文本颜色集合
			new Color(255, 0, 0),
            new Color(255, 165, 0),
            new Color(255, 255, 0),
            new Color(0, 255, 0),
            new Color(0, 127, 0),
            new Color(0, 127, 255),
            new Color(139, 0, 255)
        };

		@Override
		public void paint(Graphics g) {
			
			if (isStart) {
				BufferedImage img = new BufferedImage(size.width, size.height, 
						BufferedImage.TYPE_INT_RGB);// 创建一个BufferedImage对象
				Graphics2D g2 = (Graphics2D) img.getGraphics();// 获取Graphics2D对象
				g2.setColor(background);// 设置颜色
				g2.fillRect(0, 0, size.width, size.height);// 用预定的颜色填充一个矩形
				g2.setColor(foreground);// 设置颜色
				// 克隆所有文字雨信息到Collection中
				@SuppressWarnings("unchecked")
				Collection<Rain> collection = (Collection<Rain>) rains.clone();
				for (Iterator<Rain> it = collection.iterator(); it.hasNext();) {
					Rain r = it.next();
					if (r.isEnd()) {// 该条文字雨已经结束
						rains.remove(r);// 将该条文字雨从集合中移除
						newRain();// 创建一条新的文字雨
						continue;
					}
					if (isColorful) {// 颜色铺满
						g2.setFont(rainFont.deriveFont(r.getSize()));// 设置文字雨文本大小
						String[] ss = r.getRainChars();// 获取文字雨文本内容
						int x = r.getX();// 获取文字雨X轴坐标
						int y = r.getY() - ss.length * 15;// 获取文字雨Y轴坐标
						for (int i = 0, sl = ss.length; i < sl; i++) {
							if (i < 7) {
								g2.setColor(COLORS[i]);
							} else {
								g2.setColor(COLORS[i % 7]);
							}
							g2.drawString(ss[i], x, y);
							y += 15;
						}
					} else {
						g2.setFont(rainFont.deriveFont(r.getSize()));// 设置文字雨文本大小
						String[] ss = r.getRainChars();// 获取文字雨文本内容
						int x = r.getX();// 获取文字雨X轴坐标
						int y = r.getY() - ss.length * 15;// 获取文字雨Y轴坐标
						for (String s: ss) {
							g2.drawString(s, x, y);
							y += 15;
						}
					}
				}
				g.drawImage(img, 0, 0, this);// 绘制指定图像
			}
			
		}
		
	}
	
	/**
	 * 通过线程创建一条条文字雨
	 *
	 */
	private final class Rain implements Runnable {
		
		private int rainSpeed;// 下雨的速度
		private final String[] rainChars;// 下雨的文本
		private int rainX, rainY;// 文本坐标系
		private float fontSize;// 文本大小

		/**
		 * 初始化一条文字雨
		 * 
		 * @param length
		 * 					文字雨的文本长度
		 * @param x
		 * 					x坐标
		 * @param y
		 * 					y坐标
		 * @param speed
		 * 					下雨速度
		 * @param size
		 * 					文本大小
		 */
		public Rain(int length, int x, int y, int speed, float size) {
			
			if (speed < 1) {
				throw new RuntimeException("The speed must be greater than or equal to 1.");
			}
			
			if (length < 5) {
				length = getRandomLength();
			}
			
			if (size < 1.0f) {
				size = 15.0f;
			}
			
			rainChars = new String[length + 1];
			for (int i = 0; i < length; i++) {
				rainChars[i] = getRandomChar();
			}
			rainChars[length] = " ";
			this.rainX = x;
			this.rainY = y;
			this.rainSpeed = speed;
			this.fontSize = size;
			
		}

		/**
		 * 执行文字雨
		 */
		public void run() {
			
			while (isRaining() && rainY < getFrameSize().height + (rainChars.length + 1) * 15) {
				if (rainSpeed <= 0) {// 文字雨的速度小于等于0，结束
					break;
				}
				try {
					Thread.sleep(rainSpeed);// 睡眠
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				rainY += 2;// 每次向下运动2
			}
			rainSpeed = -1;// 文字雨结束，速度置为-1
			
		}
		
		/**
		 * 获取文本内容
		 * 
		 * @return
		 */
		public String[] getRainChars() {
			
			return rainChars;
			
		}
		
		/**
		 * 获取文字雨X轴坐标
		 * 
		 * @return
		 */
		public int getX() {
			
			return rainX;
			
		}
		
		/**
		 * 获取文字雨Y轴坐标
		 * 
		 * @return
		 */
		public int getY() {
			
			return rainY;
			
		}
		
		/**
		 * 获取文字雨文本大小
		 * 
		 * @return
		 */
		public float getSize() {
			
			return fontSize;
			
		}
		
		/**
		 * 判断文字雨是否结束
		 * 
		 * @return
		 */
		public boolean isEnd() {
			
			return rainSpeed <= 0;
			
		}
		
	}
	
	public static void main(String[] args) {
		
		BinaryRain rain = new BinaryRain();
		rain.setVisible(true);
		rain.start();
		
	}
	
}

package CleverMonkey.MineCraft;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jbox2d.common.Vec2;

import CleverMonkey.Tracker.Tracker;

public class Window {

	// 模拟帧间隔时间（毫秒）。
	protected int k_frameTime = 500;

	// 显示地图的JLable。
	protected JLabel m_leftLabel = new JLabel("", JLabel.CENTER);
	// 显示Camera的JLable。
	protected JLabel m_rightLabel1 = new JLabel("Camera");
	// 显示Alpha图的JLable。
	protected JLabel m_rightLabel2 = new JLabel("Alpha");
	// 显示Beta的JLable。
	protected JLabel m_rightLabel3 = new JLabel("Beta");
	// 显示null的JLable。
	protected JLabel m_rightLabel4 = new JLabel("null");
	// 已缩放的地图原图。
	protected Image m_mapImg;
	// 地图图片原点位置。
	protected Point m_mapImgOrigin = new Point();
	// 唯一的Car对象。
	protected Car m_car = new Car();
	// 引用的Tracker对象。
	protected Tracker m_tracker;
	// 唯一定时器任务对象。
	protected TimerTask m_timerTask = new TimerTask() {
		@Override
		public void run() {
			try {
				m_car.Run();
				OnDraw();
			} catch (Exception e1) {
				e1.printStackTrace();
				this.cancel();
			}
		}
	};
	// 地图图片缩放比例。
	protected float m_mapImgScale;
	// 各图片绘制区域定义。
	protected int m_rightImg1W;
	protected int m_rightImg1H;
	protected int m_rightImg2W;
	protected int m_rightImg2H;
	protected int m_rightImg3W;
	protected int m_rightImg3H;
	protected int m_rightImg4W;
	protected int m_rightImg4H;

	public static void main(String[] args) {
		new Window().Init();
	}

	// 窗口绘制函数。
	public void OnDraw() {

		if (m_mapImg == null) {
			JOptionPane.showMessageDialog(null, "地图未被载入。", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Map.
		BufferedImage img = new BufferedImage(m_mapImg.getWidth(null),
				m_mapImg.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics graphics = img.getGraphics();
		graphics.drawImage(m_mapImg, 0, 0, null);

		// 绘制小车。
		graphics.setColor(Color.GREEN);
		Vec2 vec2L = new Vec2();
		Vec2 vec2R = new Vec2();
		m_car.GetLocation(vec2L, vec2R);
		// 坐标转换。
		vec2L.mulLocal(m_mapImgScale);
		vec2R.mulLocal(m_mapImgScale);

		graphics.fillRect((int) vec2L.x - 2, (int) vec2L.y - 2, 5, 5);
		graphics.fillRect((int) vec2R.x - 2, (int) vec2R.y - 2, 5, 5);
		graphics.drawLine((int) vec2L.x, (int) vec2L.y, (int) vec2R.x,
				(int) vec2R.y);

		// 绘制图像传感器框。
		int midX = Math.round((vec2L.x + vec2R.x) / 2);
		int midY = Math.round((vec2L.y + vec2R.y) / 2);

		// 角度。
		double theta = vec2L.x == vec2R.x ? Math.PI / 2 : Math
				.atan((vec2L.y - vec2R.y) / (vec2L.x - vec2R.x));
		if (vec2L.x > vec2R.x)
			theta += Math.PI;

		int x1 = (int) Math.round(midX - m_car.k_cameraWidth / 2
				* m_mapImgScale * Math.cos(theta));
		int y1 = (int) Math.round(midY - m_car.k_cameraWidth / 2
				* m_mapImgScale * Math.sin(theta));
		int x2 = (int) Math.round(midX + m_car.k_cameraWidth / 2
				* m_mapImgScale * Math.cos(theta));
		int y2 = (int) Math.round(midY + m_car.k_cameraWidth / 2
				* m_mapImgScale * Math.sin(theta));
		graphics.drawLine(x1, y1, x2, y2);
		x1 = (int) Math.round(x2 + m_car.k_cameraHeight * m_mapImgScale
				* Math.sin(theta));
		y1 = (int) Math.round(y2 - m_car.k_cameraHeight * m_mapImgScale
				* Math.cos(theta));
		graphics.drawLine(x1, y1, x2, y2);
		x2 = (int) Math.round(x1 - m_car.k_cameraWidth * m_mapImgScale
				* Math.cos(theta));
		y2 = (int) Math.round(y1 - m_car.k_cameraWidth * m_mapImgScale
				* Math.sin(theta));
		graphics.drawLine(x1, y1, x2, y2);
		x1 = (int) Math.round(midX - m_car.k_cameraWidth / 2 * m_mapImgScale
				* Math.cos(theta));
		y1 = (int) Math.round(midY - m_car.k_cameraWidth / 2 * m_mapImgScale
				* Math.sin(theta));
		graphics.drawLine(x1, y1, x2, y2);

		ImageIcon icon = new ImageIcon(img);
		m_leftLabel.setIcon(icon);

		// Camera.
		icon = new ImageIcon(m_car.GetShortcut().getScaledInstance(
				m_rightImg1W, m_rightImg1H, Image.SCALE_SMOOTH));
		m_rightLabel1.setIcon(icon);

		// AlphaPattern.
		icon = new ImageIcon(m_tracker.GetAlphaPatternImg(true)
				.getScaledInstance(m_rightImg2W, m_rightImg2H,
						Image.SCALE_SMOOTH));
		m_rightLabel2.setIcon(icon);

		// BetaPattern.
		icon = new ImageIcon(m_tracker.GetBetaPatternImg(true)
				.getScaledInstance(m_rightImg3W, m_rightImg3H,
						Image.SCALE_SMOOTH));
		m_rightLabel3.setIcon(icon);
	}

	// 初始化窗口。
	protected void Init() {
		// 主窗口。
		JFrame mainFrame = new JFrame("CMMineCraft");

		mainFrame.setLayout(new BorderLayout());

		// 菜单。
		JMenuBar mainMenuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu simulateMenu = new JMenu("Simulate");
		JMenuItem fileSetMapMenuItem = new JMenuItem("Set the map...");
		JMenuItem fileQuitMenuItem = new JMenuItem("Quit");
		JMenuItem simulateSetCarMenuItem = new JMenuItem("Set the car");
		JMenuItem simulateRunMenuItem = new JMenuItem("Run");
		JMenuItem simulateStopMenuItem = new JMenuItem("Stop");

		mainFrame.add(mainMenuBar, BorderLayout.NORTH);
		mainMenuBar.add(fileMenu);
		mainMenuBar.add(simulateMenu);
		fileMenu.add(fileSetMapMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(fileQuitMenuItem);
		simulateMenu.add(simulateSetCarMenuItem);
		simulateMenu.addSeparator();
		simulateMenu.add(simulateRunMenuItem);
		simulateMenu.add(simulateStopMenuItem);

		// 框架。
		JPanel mainPanel = new JPanel();
		JPanel leftPanel = new JPanel();
		JPanel rightPanel = new JPanel();
		JPanel rightPanel1 = new JPanel();
		JPanel rightPanel2 = new JPanel();
		JPanel rightPanel3 = new JPanel();
		JPanel rightPanel4 = new JPanel();

		mainPanel.setLayout(new GridLayout(1, 2));
		leftPanel.setLayout(new BorderLayout());
		rightPanel.setLayout(new GridLayout(2, 2));
		// 使用borderLayout以居中JLabel显示图片。
		rightPanel1.setLayout(new BorderLayout());
		rightPanel2.setLayout(new BorderLayout());
		rightPanel3.setLayout(new BorderLayout());
		rightPanel4.setLayout(new BorderLayout());
		mainFrame.add(mainPanel, BorderLayout.CENTER);
		mainPanel.add(leftPanel);
		mainPanel.add(rightPanel);
		rightPanel.add(rightPanel1);
		rightPanel.add(rightPanel2);
		rightPanel.add(rightPanel3);
		rightPanel.add(rightPanel4);
		leftPanel.add(m_leftLabel);
		rightPanel1.add(m_rightLabel1, BorderLayout.CENTER);
		rightPanel2.add(m_rightLabel2);
		rightPanel3.add(m_rightLabel3);
		rightPanel4.add(m_rightLabel4);

		// 菜单事件。
		fileSetMapMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 使用标准文件对话框载入图片。
				FileDialog loadFileDlg = new FileDialog(mainFrame, "选择地图图片",
						FileDialog.LOAD);
				loadFileDlg.setVisible(true);

				try {
					BufferedImage img = ImageIO.read(new File(loadFileDlg
							.getDirectory() + loadFileDlg.getFile()));
					// 设置小车的地图。
					m_car.SetMap(img);

					// 等比缩放以适应窗口。参数"-1"表示等比缩放。
					if ((float) leftPanel.getHeight() / img.getHeight() > (float) leftPanel
							.getWidth() / img.getWidth()) {
						m_mapImgScale = (float) leftPanel.getWidth()
								/ img.getWidth();
						m_mapImg = img.getScaledInstance(leftPanel.getWidth(),
								-1, Image.SCALE_SMOOTH);
					} else {
						m_mapImgScale = (float) leftPanel.getHeight()
								/ img.getHeight();
						m_mapImg = img.getScaledInstance(-1,
								leftPanel.getHeight(), Image.SCALE_SMOOTH);
					}
					// 等待。
					m_mapImg = new ImageIcon(m_mapImg).getImage();

					m_mapImgOrigin.x = (leftPanel.getWidth() - m_mapImg
							.getWidth(null)) / 2;
					m_mapImgOrigin.y = (leftPanel.getHeight() - m_mapImg
							.getHeight(null)) / 2;

					// 绘制地图。
					ImageIcon iconL = new ImageIcon(m_mapImg);
					m_leftLabel.setIcon(iconL);
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				// 计算各区域图像的显示大小。
				// 等比缩放以适应窗口。参数"-1"表示等比缩放。
				Image img;
				img = m_car.GetShortcut();
				if ((double) rightPanel1.getHeight() / img.getHeight(null) > (double) rightPanel1
						.getWidth() / img.getWidth(null)) {
					m_rightImg1W = rightPanel1.getWidth();
					m_rightImg1H = -1;
				} else {
					m_rightImg1W = -1;
					m_rightImg1H = rightPanel1.getHeight();
				}

				img = m_tracker.GetAlphaPatternImg(false);
				if ((double) rightPanel2.getHeight() / img.getHeight(null) > (double) rightPanel2
						.getWidth() / img.getWidth(null)) {
					m_rightImg2W = rightPanel2.getWidth();
					m_rightImg2H = -1;
				} else {
					m_rightImg2W = -1;
					m_rightImg2H = rightPanel2.getHeight();
				}

				img = m_tracker.GetBetaPatternImg(false);
				if ((double) rightPanel3.getHeight() / img.getHeight(null) > (double) rightPanel3
						.getWidth() / img.getWidth(null)) {
					m_rightImg3W = rightPanel3.getWidth();
					m_rightImg3H = -1;
				} else {
					m_rightImg3W = -1;
					m_rightImg3H = rightPanel3.getHeight();
				}
			}
		});

		fileQuitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		simulateSetCarMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				leftPanel.addMouseListener(new MouseListener() {
					@Override
					public void mouseReleased(MouseEvent arg0) {
					}

					@Override
					public void mouseClicked(MouseEvent e) {
					}

					@Override
					public void mouseEntered(MouseEvent e) {
					}

					@Override
					public void mouseExited(MouseEvent e) {
					}

					@Override
					public void mousePressed(MouseEvent e) {
						Point point = e.getPoint();
						Vec2 vec2 = new Vec2(point.x, point.y);
						// 转换到窗口地图坐标。
						vec2.x -= m_mapImgOrigin.x;
						vec2.y -= m_mapImgOrigin.y;
						// 转换到地图图片坐标。
						vec2.x /= m_mapImgScale;
						vec2.y /= m_mapImgScale;
						m_car.SetCar(vec2);

						OnDraw();
					}
				});
			}
		});

		simulateRunMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Timer().schedule(m_timerTask, 2000, k_frameTime);
			}
		});

		simulateStopMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_timerTask.cancel();
			}
		});

		// 设置。
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.pack();
		mainFrame.setVisible(true);

		// 初始化对象引用。
		m_tracker = m_car.GetTracker();

	}
}

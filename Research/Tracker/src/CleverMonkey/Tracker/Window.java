package CleverMonkey.Tracker;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Window {

	// 显示原图的JLable。
	protected JLabel m_leftLabel = new JLabel("原图", JLabel.CENTER);
	// 显示模式图的JLable。
	protected JLabel m_rightLabel1 = new JLabel("Alpha模式图");
	// 显示棋盘图的JLable。
	protected JLabel m_rightLabel2 = new JLabel("Beta模式图");
	// 显示结果的JLable。
	protected JLabel m_rightLabel3 = new JLabel("结果");
	// 显示null的JLable。
	protected JLabel m_rightLabel4 = new JLabel("null");
	// 唯一的CMTrcker对象。
	Tracker m_tracker = new Tracker();

	public static void main(String[] args) {
		new Window().Init();
	}

	/** 初始化窗口。 */
	protected void Init() {
		// 主窗口。
		JFrame mainFrame = new JFrame("CMTracker");

		mainFrame.setLayout(new BorderLayout());

		// 菜单。
		JMenuBar mainMenuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem fileOpenMenuItem = new JMenuItem("Open...");
		JMenuItem fileQuitMenuItem = new JMenuItem("Quit");

		mainFrame.add(mainMenuBar, BorderLayout.NORTH);
		mainMenuBar.add(fileMenu);
		fileMenu.add(fileOpenMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(fileQuitMenuItem);

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
		fileOpenMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 使用标准文件对话框载入图片。
				FileDialog loadFileDlg = new FileDialog(mainFrame, "选择测试图片", FileDialog.LOAD);
				loadFileDlg.setVisible(true);

				try {
					BufferedImage img = ImageIO.read(new File(loadFileDlg.getDirectory() + loadFileDlg.getFile()));

					// 分析。
					m_tracker.AnalyseImg(img);

					// 显示结果。

					// 等比缩放以适应窗口。参数"-1"表示等比缩放。
					img = m_tracker.GetOriginalImg(true);
					ImageIcon iconL;
					if ((double) leftPanel.getHeight() / img.getHeight(null) > (double) leftPanel.getWidth()
							/ img.getWidth(null))
						iconL = new ImageIcon(img.getScaledInstance(leftPanel.getWidth(), -1, Image.SCALE_SMOOTH));
					else
						iconL = new ImageIcon(img.getScaledInstance(-1, leftPanel.getHeight(), Image.SCALE_SMOOTH));
					m_leftLabel.setIcon(iconL);

					// 等比缩放以适应窗口。参数"-1"表示等比缩放。
					img = m_tracker.GetAlphaPatternImg(true);
					ImageIcon iconR1;
					if ((double) rightPanel1.getHeight() / img.getHeight(null) > (double) rightPanel1.getWidth()
							/ img.getWidth(null))
						iconR1 = new ImageIcon(img.getScaledInstance(rightPanel1.getWidth(), -1, Image.SCALE_SMOOTH));
					else
						iconR1 = new ImageIcon(img.getScaledInstance(-1, rightPanel1.getHeight(), Image.SCALE_SMOOTH));
					m_rightLabel1.setIcon(iconR1);

					// 等比缩放以适应窗口。参数"-1"表示等比缩放。
					img = m_tracker.GetBetaPatternImg(true);
					ImageIcon iconR2;
					if ((double) rightPanel2.getHeight() / img.getHeight(null) > (double) rightPanel2.getWidth()
							/ img.getWidth(null))
						iconR2 = new ImageIcon(img.getScaledInstance(rightPanel2.getWidth(), -1, Image.SCALE_SMOOTH));
					else
						iconR2 = new ImageIcon(img.getScaledInstance(-1, rightPanel2.getHeight(), Image.SCALE_SMOOTH));
					m_rightLabel2.setIcon(iconR2);

					// 结果字串。
					m_rightLabel3.setText(m_tracker.GetResult().toString());

				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		fileQuitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		// Finished.
		mainFrame.pack();
		mainFrame.setVisible(true);
	}
}

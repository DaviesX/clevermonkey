package Test;

import MineCraft.src.CleverMonkey.MineCraft.LinearTransform;
import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.jbox2d.common.Vec2;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public final class App {

        // 显示地图的JLable。
        protected JLabel m_drawRegionLabel = new JLabel("", JLabel.CENTER);
        // 显示Camera的JLable。
        protected JLabel m_cameraLabel = new JLabel("Camera");
        // 显示Alpha图的JLable。
        protected JLabel m_alphLabel = new JLabel("Alpha");
        // 显示Beta的JLable。
        protected JLabel m_betaLabel = new JLabel("Beta");
        // 显示null的JLable。
        protected JLabel m_nullLabel = new JLabel("null");
        // 地图图片原点位置。
        protected Vec2 m_mapImgOrigin = new Vec2();

        // 模拟数据上下文对象。
        private final SimulationContext m_simCtx;
        // 绘制区域的屏幕对象。
        private final Screen m_drawRegionScreen = new Screen(0, 0);
        // 主窗口。
        private final JFrame m_mainFrame;

        // 模拟器演变速度。
        private final float k_simDeltaT = 0.1f;

        public App() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
                m_simCtx = SimulationContext.GetInstance();
                // 已经可以开始运行。
                m_simCtx.Start();

                // 设置模拟数据上下文。
                m_simCtx.BeginModification();
                {
                        m_simCtx.GetDrawer().SetDrawingTarget(m_drawRegionLabel);
                        m_simCtx.GetSimulation().SetDeltaT(k_simDeltaT);
                }
                m_simCtx.EndModification();

                // 设置皮肤。
                try {
                        // 使用GTK或者系统默认皮肤。
                        String systemLAF = UIManager.getSystemLookAndFeelClassName();
                        String gtkLAF = null;
                        for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
                                if (laf.getName().equals("GTK+")) {
                                        gtkLAF = laf.getClassName();
                                }
                        }
                        if (gtkLAF != null) {
                                UIManager.setLookAndFeel(gtkLAF);
                        } else {
                                UIManager.setLookAndFeel(systemLAF);
                        }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                        // 使用跨平台皮肤后备方案。
                        System.out.println("Not supported");
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                }

                // 主窗口。
                m_mainFrame = new JFrame("CMMineCraft");

                m_mainFrame.setLayout(new BorderLayout());

                // 菜单。
                JMenuBar mainMenuBar = new JMenuBar();
                JMenu fileMenu = new JMenu("File");
                JMenuItem fileSetMapMenuItem = new JMenuItem("Set the map...");
                JMenuItem fileQuitMenuItem = new JMenuItem("Quit");

                JMenu simulateMenu = new JMenu("Simulate");
                JMenuItem simulateSetCarMenuItem = new JMenuItem("Set the car");
                JMenuItem simulateRunMenuItem = new JMenuItem("Run");
                JMenuItem simulateStopMenuItem = new JMenuItem("Stop");

                JMenu strategyMenu = new JMenu("Select Strategy");
                JMenuItem strategyOrthoVeloMenuItem = new JMenuItem("OrthoVelo");
                JMenuItem strategyCurveFittingMenuItem = new JMenuItem("Curve Fitting");

                m_mainFrame.add(mainMenuBar, BorderLayout.NORTH);
                mainMenuBar.add(fileMenu);
                mainMenuBar.add(simulateMenu);

                fileMenu.add(fileSetMapMenuItem);
                fileMenu.addSeparator();
                fileMenu.add(fileQuitMenuItem);

                simulateMenu.add(simulateSetCarMenuItem);
                simulateMenu.addSeparator();
                simulateMenu.add(simulateRunMenuItem);
                simulateMenu.add(simulateStopMenuItem);

                strategyMenu.add(strategyOrthoVeloMenuItem);
                strategyMenu.add(strategyCurveFittingMenuItem);

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
                m_mainFrame.add(mainPanel, BorderLayout.CENTER);
                mainPanel.add(leftPanel);
                mainPanel.add(rightPanel);
                rightPanel.add(rightPanel1);
                rightPanel.add(rightPanel2);
                rightPanel.add(rightPanel3);
                rightPanel.add(rightPanel4);
                leftPanel.add(m_drawRegionLabel);
                rightPanel1.add(m_cameraLabel, BorderLayout.CENTER);
                rightPanel2.add(m_alphLabel);
                rightPanel3.add(m_betaLabel);
                rightPanel4.add(m_nullLabel);

                // 菜单事件。
                fileSetMapMenuItem.addActionListener((ActionEvent e) -> {
                        // 使用标准文件对话框载入图片。
                        FileDialog loadFileDlg = new FileDialog(m_mainFrame, "选择地图图片",
                                                                FileDialog.LOAD);
                        loadFileDlg.setVisible(true);

                        try {
                                String path = loadFileDlg.getDirectory() + loadFileDlg.getFile();
                                if (path == null || "".equals(path)) {
                                        // 用户没有选择文件。
                                        return;
                                }

                                Map map = new Map(new FileInputStream(path));
                                m_simCtx.BeginModification();
                                {
                                        m_simCtx.SetMap(map);
                                }
                                m_simCtx.EndModification();
                        } catch (Exception ex) {
                                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                        }
                });

                fileQuitMenuItem.addActionListener((ActionEvent e) -> {
                        m_simCtx.Stop();
                        m_mainFrame.dispatchEvent(new WindowEvent(m_mainFrame, WindowEvent.WINDOW_CLOSING));
                });

                simulateSetCarMenuItem.addActionListener((ActionEvent arg0) -> {
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
                                        Vec2 selected = new Vec2(e.getPoint().x, e.getPoint().y);
                                        Vec2 position = LinearTransform.Apply2Point(
                                                m_drawRegionScreen.ToEuclidSpace(m_mapImgOrigin, 1 / 100.0f), selected);

                                        ITracingStrategy strategy = ITracingStrategyFactory.CreateStrategy(
                                                ITracingStrategyFactory.Strategy.OrthoVelo,
                                                m_simCtx.GetMap(), true, m_alphLabel, m_betaLabel, m_nullLabel);
                                        EntityCar car = new EntityCar(position, strategy);

                                        m_simCtx.BeginModification();
                                        {
                                                m_simCtx.SetCar(car);
                                        }
                                        m_simCtx.EndModification();
                                }
                        });
                });

                simulateRunMenuItem.addActionListener((ActionEvent e) -> {
                        m_simCtx.Start();
                });

                simulateStopMenuItem.addActionListener((ActionEvent e) -> {
                        m_simCtx.Stop();
                });

                strategyOrthoVeloMenuItem.addActionListener((ActionEvent e) -> {
                        m_simCtx.BeginModification();
                        {
                                ITracingStrategy strategy = ITracingStrategyFactory.CreateStrategy(
                                        ITracingStrategyFactory.Strategy.OrthoVelo,
                                        m_simCtx.GetMap(), true, m_alphLabel, m_betaLabel, m_nullLabel);
                                m_simCtx.GetCar().ChangeStrategy(strategy);
                        }
                        m_simCtx.EndModification();
                });

                strategyCurveFittingMenuItem.addActionListener((ActionEvent e) -> {
                        m_simCtx.BeginModification();
                        {
                                ITracingStrategy strategy = ITracingStrategyFactory.CreateStrategy(
                                        ITracingStrategyFactory.Strategy.CurveFitting,
                                        m_simCtx.GetMap(), true, m_alphLabel, m_betaLabel, m_nullLabel);
                                m_simCtx.GetCar().ChangeStrategy(strategy);
                        }
                        m_simCtx.EndModification();
                });

                // 完成设置。
                m_mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                m_mainFrame.pack();
                m_mainFrame.setVisible(true);
        }

        public static void main(String[] args) {
                try {
                        App app = new App();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                        Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
}

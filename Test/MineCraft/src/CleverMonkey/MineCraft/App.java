/*
 * Copyright (C) 2016 Sheldon, davis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package MineCraft.src.CleverMonkey.MineCraft;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.jbox2d.common.Vec2;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * @author Sheldon, davis
 */
public final class App {

        // 显示地图的JLable。
        private final JLabel m_drawRegionLabel = new JLabel("", JLabel.CENTER);
        // 显示Camera的JLable。
        private final JLabel m_cameraLabel = new JLabel("Camera");
        // 显示Alpha图的JLable。
        private final JLabel m_alphLabel = new JLabel("Alpha");
        // 显示Beta的JLable。
        private final JLabel m_betaLabel = new JLabel("Beta");
        // 显示null的JLable。
        private final JLabel m_nullLabel = new JLabel("null");
        // 主窗口。
        private final JFrame m_mainFrame;
        
        // 地图图片原点位置。
        protected Vec2 m_mapImgOrigin = new Vec2();
        // 模拟数据上下文对象。
        private final SimulationContext m_simCtx;
        // 绘制区域的屏幕对象。
        private final Screen m_drawRegionScreen = new Screen(0, 0);
        // 模拟器演变速度。
        private final float k_simDeltaT = 0.1f;
        // 跟踪策略。
        private ITracingStrategyFactory.Strategy m_strategy = ITracingStrategyFactory.Strategy.OrthoVelo;

        private ITracingStrategy __GenerateStrategyFromAppState() {
                return ITracingStrategyFactory.CreateStrategy(
                        ITracingStrategyFactory.Strategy.OrthoVelo,
                        m_simCtx.GetMap(), true, m_alphLabel, m_betaLabel, m_nullLabel);
        }

        public App() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
                m_simCtx = SimulationContext.GetInstance();

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
                        JOptionPane.showMessageDialog(null, "GTK+ or system theme are not supported. Applying fallback crossplatform theme", 
                                                      "CMonkey", JOptionPane.ERROR_MESSAGE);
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

                }

                // 创建主窗口UI。
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

                JMenu benchmarkMenu = new JMenu("Benchmark");
                JMenuItem loadDefaultBenchmarkMenuItem = new JMenuItem("Load default benchmark");

                mainMenuBar.add(fileMenu);
                mainMenuBar.add(simulateMenu);
                mainMenuBar.add(strategyMenu);
                mainMenuBar.add(benchmarkMenu);

                fileMenu.add(fileSetMapMenuItem);
                fileMenu.addSeparator();
                fileMenu.add(fileQuitMenuItem);

                simulateMenu.add(simulateSetCarMenuItem);
                simulateMenu.addSeparator();
                simulateMenu.add(simulateRunMenuItem);
                simulateMenu.add(simulateStopMenuItem);

                strategyMenu.add(strategyOrthoVeloMenuItem);
                strategyMenu.add(strategyCurveFittingMenuItem);

                benchmarkMenu.add(loadDefaultBenchmarkMenuItem);

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

                // 设置事件。
                m_drawRegionLabel.addComponentListener(new ComponentListener() {
                        @Override
                        public void componentResized(ComponentEvent e) {
                                m_drawRegionScreen.Resize(e.getComponent().getWidth(), e.getComponent().getHeight());
                        }

                        @Override
                        public void componentMoved(ComponentEvent e) {
                        }

                        @Override
                        public void componentShown(ComponentEvent e) {
                        }

                        @Override
                        public void componentHidden(ComponentEvent e) {
                        }
                });
                // 菜单事件。
                fileSetMapMenuItem.addActionListener((ActionEvent e) -> {
                        // 使用标准文件对话框载入图片。
                        FileDialog loadFileDlg = new FileDialog(m_mainFrame, "选择地图图片",
                                                                FileDialog.LOAD);
                        loadFileDlg.setVisible(true);

                        try {
                                if (loadFileDlg.getDirectory() == null || loadFileDlg.getFile() == null) {
                                        // 用户没有选择文件。
                                        return;
                                }
                                String path = loadFileDlg.getDirectory() + loadFileDlg.getFile();
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
                                        m_simCtx.BeginModification();
                                        {
                                                Map map = m_simCtx.GetMap();
                                                if (map == null) {
                                                        JOptionPane.showMessageDialog(null, 
                                                                "You haven't loaded the map yet. Failed to set the car position.", 
                                                                "CMonkey", JOptionPane.ERROR_MESSAGE);
                                                } else {
                                                        Vec2 selected = new Vec2(e.getPoint().x, e.getPoint().y);
                                                        Vec2 position = LinearTransform.Apply2Point(
                                                                m_drawRegionScreen.ToEuclidSpace(m_mapImgOrigin, map.GetScale()), selected);

                                                        m_simCtx.SetCar(new EntityCar(position, __GenerateStrategyFromAppState()));
                                                }
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
                                m_strategy = ITracingStrategyFactory.Strategy.OrthoVelo;
                                EntityCar car = m_simCtx.GetCar();
                                if (car != null) {
                                        car.ChangeStrategy(__GenerateStrategyFromAppState());
                                } else {
                                        JOptionPane.showMessageDialog(null, "Car have not been set, but strategy is applied", "CMonkey",
                                                                      JOptionPane.INFORMATION_MESSAGE);
                                }
                        }
                        m_simCtx.EndModification();
                });

                strategyCurveFittingMenuItem.addActionListener((ActionEvent e) -> {
                        m_simCtx.BeginModification();
                        {
                                m_strategy = ITracingStrategyFactory.Strategy.CurveFitting;
                                EntityCar car = m_simCtx.GetCar();
                                if (car != null) {
                                        car.ChangeStrategy(__GenerateStrategyFromAppState());
                                } else {
                                        JOptionPane.showMessageDialog(null, "Car have not been set, but strategy is applied", "CMonkey",
                                                                      JOptionPane.INFORMATION_MESSAGE);
                                }
                        }
                        m_simCtx.EndModification();
                });

                loadDefaultBenchmarkMenuItem.addActionListener((ActionEvent e) -> {
                        Map map;
                        try {
                                map = new Map(new FileInputStream("Test/MineCraft/MapImg/未标题-1.jpg"));
                        } catch (IOException ex) {
                                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                                JOptionPane.showMessageDialog(null, "Benchmark map is not present in the directory. Stop proceeding.", 
                                                              "CMonkey", JOptionPane.INFORMATION_MESSAGE);
                                return;
                        }
                        m_simCtx.BeginModification();
                        {
                                m_simCtx.SetMap(map);
                                m_simCtx.SetCar(new EntityCar(new Vec2(0.2f, 0.1f), __GenerateStrategyFromAppState()));
                        }
                        m_simCtx.EndModification();
                });

                // 完成窗口UI设置。
                m_mainFrame.getContentPane().add(mainMenuBar, BorderLayout.NORTH);
                m_mainFrame.getContentPane().add(mainPanel, BorderLayout.CENTER);
                m_mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                m_mainFrame.pack();
                m_mainFrame.setVisible(true);

                // 已经可以开始运行。
                m_simCtx.Start();

                // 设置模拟数据上下文。
                m_simCtx.BeginModification();
                {
                        m_simCtx.GetDrawer().SetDrawingTarget(leftPanel);
                        m_simCtx.GetSimulation().SetDeltaT(k_simDeltaT);
                }
                m_simCtx.EndModification();
        }

        public static void main(String[] args) {
                try {
                        App app = new App();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                        JOptionPane.showMessageDialog(null, "App starts with failures. Program exiting.", 
                                                      "CMonkey", JOptionPane.ERROR_MESSAGE);
                        Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
}

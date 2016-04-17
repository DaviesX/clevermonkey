/*
 * Copyright (C) 2016 davis
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
package CleverMonkey.MineCraft;

import com.sun.corba.se.impl.orbutil.concurrent.Mutex;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author davis
 */
class Runner implements Runnable {

        // 模拟帧间隔时间（毫秒）。
        private final int k_frameTime = 120;
        // 模拟数据上下文。
        private final SimulationContext m_ctx;
        // 运行状态。
        private boolean m_is2run = true;

        public Runner(SimulationContext ctx) {
                m_ctx = ctx;
        }

        @Override
        public void run() {
                while (m_is2run) {
                        long s = System.currentTimeMillis();

                        m_ctx.BeginModification();
                        {
                                // 模拟
                                m_ctx.GetSimulation().TimeEvolution();
                                // 呈现
                                List<IDrawable> batch = new ArrayList<>();
                                batch.add(m_ctx.GetMap());
                                batch.add(m_ctx.GetCar());
                                m_ctx.GetDrawer().Draw(batch);
                        }
                        m_ctx.EndModification();

                        long e = System.currentTimeMillis();
                        while (e - s < k_frameTime && m_is2run) {
                                try {
                                        Thread.sleep(1);
                                } catch (InterruptedException ex) {
                                        Logger.getLogger(SimulationContext.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                e = System.currentTimeMillis();
                        }
                }
        }

        public void Stop() {
                m_is2run = false;
        }
}

/**
 * 模拟数据上下文。
 *
 * @author davis
 */
public class SimulationContext {

        private static final SimulationContext CTX = new SimulationContext();

        public static SimulationContext GetInstance() {
                return CTX;
        }

        private SimulationContext() {
        }

        // Thread and tasks
        private Runner m_runner = null;
        private Thread m_runnerThr = null;
        private final Mutex m_mutex = new Mutex();     // 使用非标准的Oracle Proprietary API

        public void Start() {
                if (m_runner == null) {
                        m_runner = new Runner(this);
                        m_runnerThr = new Thread(m_runner);
                        m_runnerThr.start();
                }
        }

        public boolean Stop() {
                if (m_runner == null) {
                        return false;
                }
                m_runner.Stop();
                try {
                        m_runnerThr.join();
                } catch (InterruptedException ex) {
                        Logger.getLogger(SimulationContext.class.getName()).log(Level.SEVERE, null, ex);
                        m_runner = null;
                        m_runnerThr = null;
                        return false;
                }
                m_runner = null;
                m_runnerThr = null;
                return true;
        }

        private boolean __Lock() {
                try {
                        m_mutex.acquire();
                } catch (InterruptedException ex) {
                        Logger.getLogger(SimulationContext.class.getName()).log(Level.SEVERE, null, ex);
                        return false;
                }
                return true;
        }

        private boolean __Unlock() {
                m_mutex.release();
                return true;
        }

        // Context
        private final Simulation m_sim = new Simulation(0);
        private final Drawer m_drawer = new Drawer();
        private EntityCar m_car = null;
        private Map m_map = null;

        public Simulation GetSimulation() {
                return m_sim;
        }

        public Drawer GetDrawer() {
                return m_drawer;
        }

        public void SetCar(EntityCar car) {
                m_car = car;
                m_sim.AddPhysEntity(car);
        }

        public EntityCar GetCar() {
                return m_car;
        }

        public void RemoveCar() {
                m_sim.RemovePhysEntity(m_car);
                m_car = null;
        }

        public void SetMap(Map map) {
                m_map = map;
                m_sim.SetWorldScale(map.GetScale());
        }

        public Map GetMap() {
                return m_map;
        }

        public void BeginModification() {
                __Lock();
        }

        public void EndModification() {
                __Unlock();
        }
}

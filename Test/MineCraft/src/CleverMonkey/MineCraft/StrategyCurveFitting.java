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
package MineCraft.src.CleverMonkey.MineCraft;

import javax.swing.JComponent;
import org.jbox2d.common.Vec2;

/**
 * 曲线拟合策略。
 * 
 * @author davis
 */
public class StrategyCurveFitting implements ITracingStrategy {
        
        private final Map m_map;
        private final boolean m_is2Debug;
        private final JComponent m_alpha;
        private final JComponent m_beta;
        private final JComponent m_gamma;
        
        /*
         * 应该由ITracingStrategyFactory来构造这个对象。
         */
        public StrategyCurveFitting(Map map, boolean is2Debug, JComponent alpha, JComponent beta, JComponent gamma) {
                m_map = map;
                m_is2Debug = is2Debug;
                m_alpha = alpha;
                m_beta = beta;
                m_gamma = gamma;
        }
        
        @Override
        public float ComputeFrontWheelAngularVelocity() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public float TimeEvolution(Vec2 centroid, Vec2 centroidVelocity, float dt, Simulation.Clock t) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public float ComputeFrontWheelAngle() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public float ComputeFrontWheelVelocity() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

}

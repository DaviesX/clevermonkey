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

import java.awt.Graphics;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * 绘制区域，被动让swing安排绘制任务，以防影响其他JComponents。
 * 
 * @author davis
 */
class DrawRegion extends JPanel {

        private List<IDrawable> m_batch = null;

        @Override
        protected void paintComponent(Graphics g) {
                if (m_batch == null) {
                        return;
                }
                super.paintComponent(g);
                m_batch.stream().forEach((drawable) -> {
                        if (drawable != null)
                                drawable.Draw(g, super.getWidth(), super.getHeight());
                });
                m_batch = null;
        }

        public void Draw(List<IDrawable> batch) {
                m_batch = batch;
                repaint();
        }
}

/**
 * 绘制。
 *
 * @author davis
 */
public class Drawer {

        private final DrawRegion m_compTarget = new DrawRegion();

        public Drawer() {
        }

        public Drawer(JComponent target) {
                target.add(m_compTarget);
        }

        public void SetDrawingTarget(JComponent target) {
                target.add(m_compTarget);
        }

        public void Draw(List<IDrawable> batch) {
                m_compTarget.Draw(batch);
        }
}

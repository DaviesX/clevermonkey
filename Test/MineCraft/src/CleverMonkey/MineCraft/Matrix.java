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

/**
 * 任意大小的矩阵。
 * 
 * @author davis
 */
public class Matrix {
        private float[][] m_values;
        
        public Matrix(int m, int n) {
                m_values = new float[m][n];
        }
        
        public Matrix(float[][] values) {
               m_values = values; 
        }
        
        public Matrix(Matrix t) {
               int m = t.m_values.length;
               int n = t.m_values[0].length;
               m_values = new float[m][n];
               for (int i = 0; i < m; i ++) {
                       System.arraycopy(t.m_values[i], 0, m_values[i], 0, n);
               }
        }
        
        public Matrix(float[] vector, boolean isColumnVector) {
                if (isColumnVector) {
                        m_values = new float[vector.length][1];
                        for (int i = 0; i < vector.length; i ++) {
                                m_values[i][1] = vector[i];
                        }
                } else {
                        m_values = new float[1][vector.length];
                        System.arraycopy(vector, 0, m_values[1], 0, vector.length);
                }
        }
        
        public void AssignAt(int i, int j, float value) {
                m_values[i][j] = value;
        }
        
        public float ValueAt(int i, int j) {
                return m_values[i][j];
        }
        
        public int M() {
                return m_values.length;
        }
        
        public int N() {
                return m_values[0].length;
        }
        
        public void Inverse() {
                int m = M(), n = N();
                Matrix augmented = new Matrix(m, n);
                for (int j = 0; j < m; j ++) {
                        for (int i = 0; i < n; i ++) {
                                if (i == j) {
                                        augmented.AssignAt(i, j, 1);
                                }
                        }
                }
                for (int i = 0; i < m - 1; i ++) {
                        int l = i + 1;
                        while (m_values[l][i] != 0 && l < m) { l ++; }
                        for (int k = l; k < m; k ++) {
                                if (m_values[k][i] == 0) continue;
                                float factor = -m_values[k][i]/m_values[l][i];
                                for (int j = i; j < n; j ++) {
                                        m_values[k][j] += m_values[i][j]*factor;
                                        augmented.m_values[k][j] += augmented.m_values[i][j]*factor;
                                }
                        }
                }
                for (int i = m - 1; i > 0; i --) {
                        int l = i - 1;
                        while (m_values[l][i] != 0 && l < m) { l --; }
                        for (int k = i - 1; k >= 0; k --) {
                                float factor = -m_values[k][i]/m_values[l][i];
                                for (int j = i; j < n; j ++) {
                                        m_values[k][j] += m_values[i][j]*factor;
                                        augmented.m_values[k][j] += augmented.m_values[i][j]*factor;
                                }
                        }
                }
                for (int i = 0; i < m; i ++) {
                        if (m_values[i][i] == 0) throw new IllegalArgumentException("not invertible");
                        augmented.m_values[i][i] /= m_values[i][i];
                }
                m_values = augmented.m_values;
        }
        
        public Matrix MulTransponse() {
                int m = M(), n = N();
                int r = N(), s = M();
                Matrix out = new Matrix(m, s);
                for (int i = 0; i < m; i ++) {
                        for (int j = 0; j < s; j ++) {
                                float dotProduct = 0;
                                for (int k = 0; k < n; k ++) {
                                        dotProduct += m_values[i][k]*m_values[j][k];
                                }
                                out.AssignAt(i, j, dotProduct);
                        }
                }
                return out;
        }
        
        public Matrix Mul(Matrix t) {
                int m = M(), n = N();
                int r = t.M(), s = t.N();
                if (n != r) {
                        throw new IllegalArgumentException("n != r, where n = " + n + ", " + " r = " + r);
                }
                Matrix out = new Matrix(m, s);
                for (int i = 0; i < m; i ++) {
                        for (int j = 0; j < s; j ++) {
                                float dotProduct = 0;
                                for (int k = 0; k < n; k ++) {
                                        dotProduct += m_values[i][k]*t.m_values[k][j];
                                }
                                out.AssignAt(i, j, dotProduct);
                        }
                }
                return out;
        }
}

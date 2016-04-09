package CleverMonkey.Tracker;

import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics;

/** CMTracker�����ڸ���ͼ����������˶�����Ŀ����Ϣ�� */
public class Tracker {
	// �������ö�١�
	public enum ResultType {
		Unk, Run, Stop
	}

	// ģʽͼ�߶ȡ�
	protected final int k_patternHeight = 160;
	// ģʽͼ��ȡ�
	protected final int k_patternWidth = 90;
	// �б���ɫ���ޡ�
	protected final Color k_appointedColorMax = new Color(15, 15, 15);
	// �б���ɫ���ޡ�
	protected final Color k_appointedColorMin = new Color(0, 0, 0);
	// ·���߿����ޡ�
	protected final int k_trackWidthMin = 20;
	// ȱ���϶���ޡ�
	protected final int k_gapMax = 1;
	// ֹͣ����С��ȡ�
	protected final int k_stopLineWidthMin = 80;
	// ָֹͣ���������С����ֹͣ����Ŀ��
	protected final int k_stopLineCounterMin = 50;
	// �˶�����Ŀ���б����
	protected final int k_baseLine = 40;

	// �����ԭͼ��
	protected BufferedImage m_originalImg;
	// Alphaģʽͼͼ��
	protected BufferedImage m_alphaImg = new BufferedImage(k_patternWidth,
			k_patternHeight, BufferedImage.TYPE_INT_RGB);
	// Betaģʽͼͼ��
	protected BufferedImage m_betaImg = new BufferedImage(k_patternWidth,
			k_patternHeight, BufferedImage.TYPE_INT_RGB);
	// Alphaģʽͼ���顣
	protected boolean[][] m_patternAlpha = new boolean[k_patternWidth][k_patternHeight];
	// Betaģʽͼ���顣
	protected boolean[][] m_patternBeta = new boolean[k_patternWidth][k_patternHeight];
	// ��Ч�б�ǡ���¼��Ӧ���Ƿ���Ч��
	// ��Ч�ж��壺��ʶ��·���ߵ���У������������ϵ���С�
	protected boolean[] m_isValidLine = new boolean[k_patternHeight];
	// ֹͣ�б�ǡ���¼��Ӧ���Ƿ�Ϊֹͣ����С�
	// ֹͣ�ж��壺����Ч·����Ȳ�С�ڹ涨��ֹͣ��ǿ�ȵ��С�
	protected boolean[] m_isStopLine = new boolean[k_patternHeight];
	// ģʽͼ��ԭͼ��ת��ϵ����
	public int m_patternToImgScale;
	// �����-�˶�����Ŀ��㡣��Ŀ��������Ȼ����ͷ����ϵ��ԭ�������Ͻǣ���
	protected Point m_targetPoint = new Point(0, 0);
	// �����-������͡�
	protected ResultType m_resultType = ResultType.Unk;

	// �ӿڡ�

	// �������ͼ��
	// ͼ�������16��9�ĳ���ȡ�
	public ResultType AnalyseImg(BufferedImage img) {
		m_originalImg = img;

		// �������̡��������ĵ���
		if (!ToAlpha())
			return ResultType.Unk;
		if (!ToBeta())
			return ResultType.Unk;
		if (!ToGamma())
			return ResultType.Unk;
		if (!ToOmega())
			return ResultType.Unk;
		// Finished.
		return m_resultType;
	}

	// ��ȡ�������
	// @return �����-�˶�����Ŀ��㡣
	public Point GetResult() {
		return m_targetPoint;
	}

	// ��ͼƬ����ʽ���ԭͼ��
	// @isResultDrawed �Ƿ���ƽ���㡣
	// @return ԭͼ��
	public BufferedImage GetOriginalImg(boolean isResultDrawed) {

		// �����ƽ������ֱ�ӷ���ԭͼ��
		if (isResultDrawed == false)
			return m_originalImg;

		// ���򲹳���ƽ���㼰���ߡ�
		Graphics graphics = m_originalImg.getGraphics();
		graphics.setColor(Color.GRAY);
		graphics.drawLine(0, k_baseLine, m_originalImg.getWidth() - 1,
				k_baseLine);
		graphics.setColor(Color.ORANGE);
		graphics.fillRect(m_targetPoint.x - 15, m_targetPoint.y - 15, 30, 30);
		return m_originalImg;
	}

	// ��ͼƬ����ʽ���Alphaģʽͼ��
	// @isResultDrawed �Ƿ���ƽ���㡣
	// @return Alphaģʽͼ��
	public BufferedImage GetAlphaPatternImg(boolean isResultDrawed) {

		// ��һ�б��������ݲ�ӳ�����ͼƬ�����С�
		for (int x = 0; x < k_patternWidth; ++x) {
			for (int y = 0; y < k_patternHeight; ++y) {
				// Ŀ�����ɫΪ��ɫ����������ɫΪ��ɫ��
				if (m_patternAlpha[x][y] == true)
					m_alphaImg.setRGB(x, y, Color.BLACK.getRGB());
				else
					m_alphaImg.setRGB(x, y, Color.WHITE.getRGB());
			}
		}
		// �����ƽ������ֱ�ӷ��ء�
		if (isResultDrawed == false || m_patternToImgScale == 0)
			return m_alphaImg;

		// ���򲹳���ƽ���㼰���ߡ�
		Graphics graphics = m_alphaImg.getGraphics();
		graphics.setColor(Color.GRAY);
		graphics.drawLine(0, k_baseLine, k_patternWidth - 1, k_baseLine);
		graphics.setColor(Color.ORANGE);
		graphics.fillRect(m_targetPoint.x / m_patternToImgScale - 1,
				m_targetPoint.y / m_patternToImgScale - 1, 3, 3);
		return m_alphaImg;
	}

	// ��ͼƬ����ʽ���Betaģʽͼ��
	// @isResultDrawed �Ƿ���ƽ���㡣
	// @return Betaģʽͼ��
	public BufferedImage GetBetaPatternImg(boolean isResultDrawed) {

		// ��һ�б��������ݲ�ӳ����䵽�µ�ͼƬ�����С�
		for (int x = 0; x < k_patternWidth; ++x) {
			for (int y = 0; y < k_patternHeight; ++y) {
				// Ŀ�����ɫΪ��ɫ��ֹͣ����ɫΪ��ɫ����������ɫΪ��ɫ��
				if (m_patternBeta[x][y] == true)
					if (m_isStopLine[y] == true)
						m_betaImg.setRGB(x, y, Color.RED.getRGB());
					else
						m_betaImg.setRGB(x, y, Color.BLACK.getRGB());
				else
					m_betaImg.setRGB(x, y, Color.WHITE.getRGB());
			}
		}
		// �����ƽ������ֱ�ӷ��ء�
		if (isResultDrawed == false || m_patternToImgScale == 0)
			return m_betaImg;

		// ���򲹳���ƽ���㼰���ߡ�
		Graphics graphics = m_betaImg.getGraphics();
		graphics.setColor(Color.GRAY);
		graphics.drawLine(0, k_baseLine, k_patternWidth - 1, k_baseLine);
		graphics.setColor(Color.ORANGE);
		graphics.fillRect(m_targetPoint.x / m_patternToImgScale - 1,
				m_targetPoint.y / m_patternToImgScale - 1, 3, 3);
		return m_betaImg;
	}

	// ���ߺ�����

	// ��һ��ģʽ��������ԭʼ����ͼ����Ϊ�ڰ�ģʽͼ��
	// ��������һ��һ����С��ԭͼ�������ӳ�䵽ģʽͼ���ء�ӳ��ʱ���Զ��б�������ɫ�Ƿ���Ŀ����ɫ�����С�
	// �����̿��Թ���ɫ������������š�
	protected boolean ToAlpha() {
		Color color;
		// ��ɫ�����ۼ�����
		int red = 0;
		int green = 0;
		int blue = 0;

		// ��ʼ��ģʽͼ��ԭͼ��ת��ϵ����
		m_patternToImgScale = m_originalImg.getHeight() / k_patternHeight;

		for (int x = 0; x < k_patternWidth; ++x) {
			for (int y = 0; y < k_patternHeight; ++y) {
				// New Method ����ȡ������
				// �ֱ�ɨ�����е����أ�ȡ�����е��������ص���ɫƽ��ֵ��Ϊ��Ч������ɫ��������������š�
				// ��ѡ������зֱ�������߽���ƫ�����У���ȡ�߽���Ƶĵ����С�ʹ������ԭͼ�ϵķֲ���ƽ����
				// NOTICE ��һģʽͼ���Ӷ�Ӧ�����СĬ�ϲ�С��3*3���ء�
				for (int i = 0; i < m_patternToImgScale; ++i) {
					color = new Color(m_originalImg.getRGB(x
							* m_patternToImgScale + i, y * m_patternToImgScale
							+ 2));
					red += color.getRed();
					green += color.getGreen();
					blue += color.getBlue();

					color = new Color(m_originalImg.getRGB(x
							* m_patternToImgScale + i, (y + 1)
							* m_patternToImgScale - 3));
					red += color.getRed();
					green += color.getGreen();
					blue += color.getBlue();
				}

				red /= m_patternToImgScale * 2;
				green /= m_patternToImgScale * 2;
				blue /= m_patternToImgScale * 2;

				if (red >= k_appointedColorMin.getRed()
						&& red <= k_appointedColorMax.getRed()
						&& green >= k_appointedColorMin.getGreen()
						&& green <= k_appointedColorMax.getGreen()
						&& blue >= k_appointedColorMin.getBlue()
						&& blue <= k_appointedColorMax.getBlue())
					m_patternAlpha[x][y] = true;
				else
					m_patternAlpha[x][y] = false;
			}
		}

		return true;
	}

	// ��Ӧ�����ļ򻯰档�Լ����̣�����ʶ���ȶ��Լ��ɹ��ʵĴ��۽��ͼ��㸴�Ӷȡ�
	protected boolean ToAlphaS() {
		Color color;
		// ��ɫ�����ۼ�����
		int red = 0;
		int green = 0;
		int blue = 0;

		// ��ʼ��ģʽͼ��ԭͼ��ת��ϵ����
		m_patternToImgScale = m_originalImg.getHeight() / k_patternHeight;

		for (int x = 0; x < k_patternWidth; ++x) {
			for (int y = 0; y < k_patternHeight; ++y) {
				// Old Method �ĵ�ȡ������
				// ȡģʽͼ��Ӧ��ԭʼͼ������ĸ��ǹ��ĸ������ɫƽ��ֵ��Ϊ���������ɫ����ֵ������жϸ���ɫֵ�Ƿ�ΪĿ���б���ɫ�����ģʽͼ��

				// ���Ͻ�
				color = new Color(m_originalImg.getRGB(x * m_patternToImgScale,
						y * m_patternToImgScale));
				red = color.getRed();
				green = color.getGreen();
				blue = color.getBlue();
				// ���Ͻ�
				color = new Color(m_originalImg.getRGB((x + 1)
						* m_patternToImgScale - 1, y * m_patternToImgScale));
				red += color.getRed();
				green += color.getGreen();
				blue += color.getBlue();
				// ���½�
				color = new Color(m_originalImg.getRGB((x + 1)
						* m_patternToImgScale - 1, (y + 1)
						* m_patternToImgScale - 1));
				red += color.getRed();
				green += color.getGreen();
				blue += color.getBlue();
				// ���½�
				color = new Color(m_originalImg.getRGB(x * m_patternToImgScale,
						(y + 1) * m_patternToImgScale - 1));
				red += color.getRed();
				green += color.getGreen();
				blue += color.getBlue();

				red /= 4;
				green /= 4;
				blue /= 4;

				if (red >= k_appointedColorMin.getRed()
						&& red <= k_appointedColorMax.getRed()
						&& green >= k_appointedColorMin.getGreen()
						&& green <= k_appointedColorMax.getGreen()
						&& blue >= k_appointedColorMin.getBlue()
						&& blue <= k_appointedColorMax.getBlue())
					m_patternAlpha[x][y] = true;
				else
					m_patternAlpha[x][y] = false;
			}
		}

		return true;
	}

	// �ڶ���ģʽ���������ڰ�ģʽͼ����Ϊ����ȫ·����ͼ����ʶ��ָֹͣ�
	// ������Ѱ��ÿһ�е�·�������е���Ϊ���е�·���ߵ㡣����������Ч�лᱻ�Թ���
	// �����̿��Դ����޹�·��������ɢ�������ȱ����š�
	// NOTICE �ú��������߼�������������ͼ��
	protected boolean ToBeta() {
		// ����ͨ����Ч����X�����ۼ�����
		int pointX = 0;
		// ����ͨ����Ч���ؼ�������
		int pointCounter = 0;
		// ����ͨ�߼�����Ч���ڵ�ÿһ��϶�ڵļ�϶���������
		int gapCounter = 0;
		// ��Ч��ǡ����״μ�¼��Ч������á�
		boolean isSet = false;

		// ����ɨ�衣
		for (int y = 0; y < k_patternHeight; ++y) {
			// �������ݡ�
			pointX = 0;
			pointCounter = 0;
			gapCounter = 0;
			isSet = false;
			m_isValidLine[y] = false;
			m_isStopLine[y] = false;

			for (int x = 0; x < k_patternWidth; ++x) {
				// ����PatternBeta.
				m_patternBeta[x][y] = false;

				if (m_patternAlpha[x][y] == true) {
					gapCounter = 0;
					pointX += x;
					++pointCounter;
				} else if (pointCounter != 0) {
					if (gapCounter < k_gapMax) {
						++gapCounter;
					} else {
						// ������ͨ�߿�ȴ���Ԥ����С��ȲŽ��д���������������������´��б�
						if (pointCounter >= k_trackWidthMin) {
							// ����Ѿ���¼�����ݣ����������������Ч������������ݣ�����ʣ�ಿ��ֱ�ӿ�ʼ��һ��ɨ�衣
							// ���˵�������ǵ�������ķ������ʽ�С�����Բ�ȡ����д�룬ʧ������Ĳ��ԡ�
							// ���ʵ�ʲ��Ա���������������ʽϴ󣬿��Ը�����ʱ�����洢���ݣ����д��Ĳ��ԡ�
							if (isSet == true) {
								m_isValidLine[y] = false;
								for (int i = 0; i < k_patternWidth; ++i)
									m_patternBeta[i][y] = false;
								pointCounter = 0;
								break;
							}
							// ��֤�Ƿ�Ϊֹͣ����С�
							if (pointCounter >= k_stopLineWidthMin) {
								m_isStopLine[y] = true;
							}
							// ��¼���ݡ�
							isSet = true;
							// ȡ�е�Ϊ·���ߵ㡣
							m_patternBeta[Math.round(((float) pointX)
									/ pointCounter)][y] = true;
							m_isValidLine[y] = true;
						}
						// �������Ϊ��һ���б�׼����
						pointX = 0;
						pointCounter = 0;
						gapCounter = 0;
					}
				}
			}

			// ����ɨ����ϡ������Ե�����
			if (pointCounter >= k_trackWidthMin) {
				// ����Ѿ���¼�����ݣ����������������Ч������������ݣ�����ʣ�ಿ��ֱ�ӿ�ʼ��һ��ɨ�衣
				// ���˵�������ǵ�������ķ������ʽ�С�����Բ�ȡ����д�룬ʧ������Ĳ��ԡ�
				// ���ʵ�ʲ��Ա���������������ʽϴ󣬿��Ը�����ʱ�����洢���ݣ����д��Ĳ��ԡ�
				if (isSet == true) {
					m_isValidLine[y] = false;
					for (int i = 0; i < k_patternWidth; ++i)
						m_patternBeta[i][y] = false;
					continue;
				}
				// ��֤�Ƿ�Ϊֹͣ����С�
				if (pointCounter >= k_stopLineWidthMin) {
					m_isStopLine[y] = true;
				}
				// ȡ�е�Ϊ��Ч�㡣
				m_patternBeta[Math.round(((float) pointX) / pointCounter)][y] = true;
				m_isValidLine[y] = true;
			}
		}

		// Finished.
		return true;
	}

	// ��Ӧ�����ļ򻯰档�Խ��;��ȵĴ��۽��ͼ��㸴�Ӷȡ�
	protected boolean ToBetaS() {
		// ����ͨ����Ч����X�����ۼ�����
		int pointX = 0;
		// ����ͨ����Ч���ؼ�������
		int pointCounter = 0;
		// ����ͨ�߼�����Ч���ڵ�ÿһ��϶�ڵļ�϶���������
		int gapCounter = 0;

		// ����ɨ�衣
		for (int y = 0; y < k_patternHeight; ++y) {
			// �������ݡ�
			pointX = 0;
			pointCounter = 0;
			gapCounter = 0;
			m_isValidLine[y] = false;
			m_isStopLine[y] = false;

			for (int x = 0; x < k_patternWidth; ++x) {
				// ����PatternBeta.
				m_patternBeta[x][y] = false;

				if (m_patternAlpha[x][y] == true) {
					gapCounter = 0;
					pointX += x;
					++pointCounter;
				} else if (pointCounter != 0) {
					if (gapCounter < k_gapMax) {
						++gapCounter;
					} else {
						// ���̵���Ч��ͨ������Բ�����ɨ�衣
						if (pointCounter < k_trackWidthMin) {
							// �������Ϊ��һ���б�׼����
							pointX = 0;
							pointCounter = 0;
							gapCounter = 0;
						}
						// �����˳������������ݡ�
						else {
							break;
						}
					}
				}
			}
			// �ٴ��б���Ϊ�˴����Ե�����
			if (pointCounter >= k_trackWidthMin) {
				// ȡ�е�Ϊ��Ч�㡣
				m_patternBeta[Math.round(((float) pointX) / pointCounter)][y] = true;
				m_isValidLine[y] = true;
				// ��֤�Ƿ�Ϊֹͣ����С�
				if (pointCounter >= k_stopLineWidthMin) {
					m_isStopLine[y] = true;
				}
			}
		}

		// Finished.
		return true;
	}

	// ������ģʽ��������ϲ���ȫ·����ͼ�ĺ���ȱʧ���֡�
	// ������ʹ��·����ȱʧ�������˵���Ч�������Ϸ��̲�������ʹ�ø÷��̲�ȫ·����ͼ�����ڲ������ȱʧ���ֻ�ʹ����ʧ�ܡ�
	// �����̿��Դ����жϻ�����
	protected boolean ToGamma() {

		for (int i = 0; i < k_patternHeight; ++i) {
			// Ѱ�Ҷϵ㡣
			if (m_isValidLine[i] == false) {
				// ����ߵ����˵㡣
				Point startPoint = new Point();
				Point endPoint = new Point();

				// �����������㡣
				int x = 0;
				int y = i - 1;
				// ���·���ߴӵײ���ʼȱʧ���������Ϊ��Чֵ�������յ��������
				if (y == -1) {
					startPoint.setLocation(-1, -1);
				} else {
					for (; x < k_patternWidth && m_patternBeta[x][y] != true; ++x)
						;
					assert (x == k_patternWidth);
					startPoint.setLocation(x, y);
				}

				// ����������յ㡣
				for (y += 2; y < k_patternHeight && m_isValidLine[y] != true; ++y)
					;
				// ����Ҳ���������յ㡣
				if (y >= k_patternHeight) {
					// �Եױ��е���Ϊ�յ㡣
					endPoint.setLocation(k_patternWidth / 2,
							k_patternHeight - 1);
				} else {
					for (x = 0; x < k_patternWidth
							&& m_patternBeta[x][y] != true; ++x)
						;
					assert (x == k_patternWidth);
					endPoint.setLocation(x, y);
				}

				// ������������
				if (startPoint.y == -1) {
					// ȱʧ���ִ����б���߲��ִ�ͼ�񶥲��������޷�����������
					if (endPoint.y > k_baseLine) {
						m_resultType = ResultType.Unk;
						return false;
					}
					// ȱʧ�����ڻ����ϲ��ִ�ͼ�񶥲���������ϡ�
					else {
						// �����ò��֡�
						i = endPoint.y;
						continue;
					}
				}

				// �������ȱʧ���ݡ���Ϸ���Ϊ y=ax+b��
				// ��б��Ϊ0ʱ��
				if (endPoint.x - startPoint.x == 0) {
					for (y = startPoint.y + 1; y < endPoint.y; ++y) {
						m_patternBeta[x][y] = true;
						m_isValidLine[y] = true;
					}
				} else {
					double a = (double) (endPoint.y - startPoint.y)
							/ (endPoint.x - startPoint.x);
					double b = startPoint.y - a * startPoint.x;
					for (y = startPoint.y + 1; y < endPoint.y; ++y) {
						x = (int) Math.round((y - b) / a);
						m_patternBeta[x][y] = true;
						m_isValidLine[y] = true;
					}
				}

				// ��������ϲ��֡�
				i = endPoint.y;
			}
		}

		return true;
	}

	// ��ȡ���շ������������Ŀ����Ƶ㡣
	// �����Ǽ���·�������б���ߵĽ��㡣
	protected boolean ToOmega() {
		// ����Ŀ����Ƶ�x���ꡣ
		int x = 0;
		for (; x < k_patternWidth && m_patternBeta[x][k_baseLine] != true; ++x)
			;

		// ����Betaģʽͼ�ҳ���Ŀ���������ת����ԭͼ�����Ա������
		m_targetPoint.setLocation(x * m_patternToImgScale, k_baseLine
				* m_patternToImgScale);

		m_resultType = ResultType.Run;

		// ��֤�Ƿ�Ϊָֹͣ������б����Ϊֹͣ�����б�
		if (m_isStopLine[k_baseLine] == true) {
			int start = k_baseLine - 1;
			int end = k_baseLine + 1;
			for (; m_isStopLine[start] != false; --start)
				;
			for (; m_isStopLine[end] != false; ++end)
				;
			if (end - start - 1 >= k_stopLineCounterMin)
				m_resultType = ResultType.Stop;
		}

		return true;
	}
}

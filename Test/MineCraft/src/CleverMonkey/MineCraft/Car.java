package CleverMonkey.MineCraft;

import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.DistanceJointDef;

import CleverMonkey.MotionArithmetic.MotionArithmetic;
import CleverMonkey.Tracker.Tracker;

public class Car {

	// ����λ��(m)���������ص�ת��ϵ����
	protected final int k_mToPixelScale = 7000;
	// ͼ�񴫸����ֱ��ʡ�
	protected final int k_cameraHeight = 1280;
	// ͼ�񴫸����ֱ��ʡ�
	protected final int k_cameraWidth = 720;
	// С��������ȡ�
	protected final float k_carWidthHalf = 0.07f;
	// ģ�ⲽ���ʱ�䣨�룩��
	protected final float k_stepTime = 0.1f;

	// Ψһ��ͼ��ʶ��������
	public Tracker m_tracker = new Tracker();
	// Ψһ���˶�����������
	protected MotionArithmetic m_motionArithmetic = new MotionArithmetic();
	// ΨһWorld����
	protected World m_world;
	// ��ͼͼ�����
	protected BufferedImage m_mapImg;
	// ͼ�񴫸������ͼ��
	protected BufferedImage m_cameraImg = new BufferedImage(k_cameraWidth,
			k_cameraHeight, BufferedImage.TYPE_INT_ARGB);
	// С����λ�ö�����塣
	protected Body m_carLeftBody;
	// С����λ�ö�����塣
	protected Body m_carRightBody;
	// �����ٶȡ�
	protected Vec2 vLeft = new Vec2();
	// �����ٶȡ�
	protected Vec2 vRight = new Vec2();
	// �˶�����Ŀ��㡣
	protected Point m_targetPoint = new Point();
	// �˶�����Ŀ����С������ϵ��ʾ��
	protected Vec2 m_targetVec2 = new Vec2();
	//
	Tracker.ResultType m_resultType;

	// �ӿڡ�

	// ���õ�ͼ��
	public void SetMap(BufferedImage mapImg) {
		m_mapImg = mapImg;
	}

	// ����С����ָ��λ�ã�ͨ��С����λ�ö��塣λ���Ե�ͼͼ�����궨�塣
	public void SetCar(Vec2 leftVec2) {

		if (m_mapImg == null) {
			JOptionPane.showMessageDialog(null, "���������ͼ��", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// ��ʼ��JBox2D��
		// ����Ҫʹ��������
		m_world = new World(new Vec2(0, 0), false);

		// ����С����
		// ��ת���ꡣ��Ϊ��ͼͼ������ϵ�����Ͻ�Ϊԭ�㣬������������ϵ�����½�Ϊԭ�㡣
		leftVec2.y = m_mapImg.getHeight() - leftVec2.y;
		// ����ͼͼ������ת�����������ꡣ
		leftVec2.x /= k_mToPixelScale;
		leftVec2.y /= k_mToPixelScale;
		// Left.
		PolygonShape carShape = new PolygonShape();
		carShape.setAsBox(0.02f, 0.02f);

		FixtureDef carFixtureDef = new FixtureDef();
		carFixtureDef.density = 1.0f;
		carFixtureDef.shape = carShape;

		BodyDef carBodyDef = new BodyDef();
		carBodyDef.type = BodyType.DYNAMIC;
		carBodyDef.position.set(leftVec2.x, leftVec2.y);
		carBodyDef.angle = 0.0f;

		m_carLeftBody = m_world.createBody(carBodyDef);
		m_carLeftBody.createFixture(carFixtureDef);

		// Right.
		carBodyDef.position.set(leftVec2.x + k_carWidthHalf * 2, leftVec2.y);

		m_carRightBody = m_world.createBody(carBodyDef);
		m_carRightBody.createFixture(carFixtureDef);

		// �����ؽڡ�
		DistanceJointDef carDistanceJointDef = new DistanceJointDef();
		carDistanceJointDef.initialize(
				m_carLeftBody,
				m_carRightBody,
				new Vec2(m_carLeftBody.getPosition().x, m_carLeftBody
						.getPosition().y),
				new Vec2(m_carRightBody.getPosition().x, m_carRightBody
						.getPosition().y));
		carDistanceJointDef.collideConnected = false;
		m_world.createJoint(carDistanceJointDef);

		// ����ͼ�񴫸���ͼ��
		UpdateCameraImg();
		// ���¸������������ݡ�
		m_resultType = m_tracker.AnalyseImg(m_cameraImg);
	}

	// �˶�����һ�㡣
	public boolean Run() {

		// ��ȡ�˶�Ŀ��㡣
		// ��Ϊÿ�����к��Ѹ��¸��������ݣ��˴������ٴθ��¡�
		m_targetPoint.setLocation(m_tracker.GetResult());

		// ת��Ŀ��㵽С������ϵ��ʾ��
		m_targetVec2.set(m_targetPoint.x, m_targetPoint.y);
		m_targetVec2.x -= k_cameraWidth / 2;
		m_targetVec2.y = k_cameraHeight - m_targetVec2.y;
		m_targetVec2.x /= k_mToPixelScale;
		m_targetVec2.y /= k_mToPixelScale;

		// �����˶�Ŀ��㣬��ȡ�˶�ѧ������
		m_motionArithmetic.GetMotionArgu(vLeft, vRight, m_targetVec2,
				m_resultType);
		// �����˶�ѧ����������С��λ�á�
		// �����ٶ�ת��Ϊ�ٶ��������ٶ�������Զ��ֱ��С����������ߡ�
		// ����Ƕȡ�
		Vec2 vec2L = m_carLeftBody.getPosition();
		Vec2 vec2R = m_carRightBody.getPosition();
		double theta = vec2L.x == vec2R.x ? Math.PI / 2 : Math
				.atan((vec2L.y - vec2R.y) / (vec2L.x - vec2R.x));
		if (vec2L.x > vec2R.x)
			theta += Math.PI;
		// Ӧ����ת����
		vLeft.x = (float) (vLeft.x * Math.cos(theta) - vLeft.y
				* Math.sin(theta));
		vLeft.y = (float) (vLeft.x * Math.sin(theta) + vLeft.y
				* Math.cos(theta));
		vRight.x = (float) (vRight.x * Math.cos(theta) - vRight.y
				* Math.sin(theta));
		vRight.y = (float) (vRight.x * Math.sin(theta) + vRight.y
				* Math.cos(theta));
		// Ӧ���ٶȡ�
		m_carLeftBody.setLinearVelocity(vLeft);
		m_carRightBody.setLinearVelocity(vRight);

		// ��Box2D�Ƽ��������С�
		m_world.step(k_stepTime, 10, 8);

		// ����ͼ�񴫸���ͼ��
		UpdateCameraImg();
		// ���¸������������ݡ�
		m_resultType = m_tracker.AnalyseImg(m_cameraImg);

		return true;
	}

	// ��ȡС��λ�á�λ���Ե�ͼͼ�����궨�塣
	public void GetLocation(Vec2 left, Vec2 right) {
		Vec2 vec2 = new Vec2(m_carLeftBody.getPosition().mul(k_mToPixelScale));
		// ��ת���ꡣ
		vec2.y = m_mapImg.getHeight() - vec2.y;
		left.set(vec2);
		vec2.set(m_carRightBody.getPosition().mul(k_mToPixelScale));
		// ��ת���ꡣ
		vec2.y = m_mapImg.getHeight() - vec2.y;
		right.set(vec2);
	}

	// ��ȡͼ�񴫸���ͼ������á�
	public final BufferedImage GetShortcut() {
		return m_cameraImg;
	}

	// ��ȡ�����������á�
	public final Tracker GetTracker() {
		return m_tracker;
	}

	// ���ߺ�����

	// ����ͼ�񴫸���ͼ��
	protected void UpdateCameraImg() {
		// С��λ�õĵ�ͼ���ꡣ
		Vec2 vec2L = m_carLeftBody.getPosition().mul(k_mToPixelScale);
		Vec2 vec2R = m_carRightBody.getPosition().mul(k_mToPixelScale);
		// ��ת���ꡣ
		vec2L.y = m_mapImg.getHeight() - vec2L.y;
		vec2R.y = m_mapImg.getHeight() - vec2R.y;
		// �Ƕȡ�
		double theta = vec2L.x == vec2R.x ? Math.PI / 2 : Math
				.atan((vec2L.y - vec2R.y) / (vec2L.x - vec2R.x));
		if (vec2L.x > vec2R.x)
			theta += Math.PI;
		// С���е㡣
		int midX = (int) Math.round((vec2L.x + vec2R.x) / 2);
		int midY = (int) Math.round((vec2L.y + vec2R.y) / 2);

		// ͼ�񴫸���ԭ�㡣
		int cameraOriginX = (int) Math.round(midX - k_cameraWidth / 2
				* Math.cos(theta) + k_cameraHeight * Math.sin(theta));
		int cameraOriginY = (int) Math.round(midY - k_cameraWidth / 2
				* Math.sin(theta) - k_cameraHeight * Math.cos(theta));

		// ÿ��ͼ�񴫸���������һӳ�临�ơ�
		for (int x = 0; x < k_cameraWidth; ++x) {
			for (int y = 0; y < k_cameraHeight; ++y) {
				// �Ƚ�ͼ�񴫸���ͼ�����������ת���󣬺����ͼ�񴫸���ԭ������Ӷ����ͼ�񴫸������ص���ͼ���ص�һ��һӳ�䡣
				int rgb = m_mapImg.getRGB(
						(int) (Math.round(Math.cos(theta) * x - Math.sin(theta)
								* y) + cameraOriginX),
						(int) (Math.round(Math.sin(theta) * x + Math.cos(theta)
								* y) + cameraOriginY));
				m_cameraImg.setRGB(x, y, rgb);
			}
		}
	}

}

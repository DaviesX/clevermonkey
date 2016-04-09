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

	// 物理单位米(m)到物理像素的转换系数。
	protected final int k_mToPixelScale = 7000;
	// 图像传感器分辨率。
	protected final int k_cameraHeight = 1280;
	// 图像传感器分辨率。
	protected final int k_cameraWidth = 720;
	// 小车物理半宽度。
	protected final float k_carWidthHalf = 0.07f;
	// 模拟步间隔时间（秒）。
	protected final float k_stepTime = 0.1f;

	// 唯一的图像识别器对象。
	public Tracker m_tracker = new Tracker();
	// 唯一的运动运算器对象。
	protected MotionArithmetic m_motionArithmetic = new MotionArithmetic();
	// 唯一World对象。
	protected World m_world;
	// 地图图像对象。
	protected BufferedImage m_mapImg;
	// 图像传感器输出图像。
	protected BufferedImage m_cameraImg = new BufferedImage(k_cameraWidth,
			k_cameraHeight, BufferedImage.TYPE_INT_ARGB);
	// 小车左位置定义刚体。
	protected Body m_carLeftBody;
	// 小车右位置定义刚体。
	protected Body m_carRightBody;
	// 左轮速度。
	protected Vec2 vLeft = new Vec2();
	// 右轮速度。
	protected Vec2 vRight = new Vec2();
	// 运动控制目标点。
	protected Point m_targetPoint = new Point();
	// 运动控制目标点的小车坐标系表示。
	protected Vec2 m_targetVec2 = new Vec2();
	//
	Tracker.ResultType m_resultType;

	// 接口。

	// 设置地图。
	public void SetMap(BufferedImage mapImg) {
		m_mapImg = mapImg;
	}

	// 放置小车于指定位置，通过小车左位置定义。位置以地图图像坐标定义。
	public void SetCar(Vec2 leftVec2) {

		if (m_mapImg == null) {
			JOptionPane.showMessageDialog(null, "请先载入地图。", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 初始化JBox2D。
		// 不需要使用重力。
		m_world = new World(new Vec2(0, 0), false);

		// 创建小车。
		// 反转坐标。因为地图图像坐标系以左上角为原点，物理世界坐标系已左下角为原点。
		leftVec2.y = m_mapImg.getHeight() - leftVec2.y;
		// 将地图图像坐标转换到物理坐标。
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

		// 创建关节。
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

		// 更新图像传感器图像。
		UpdateCameraImg();
		// 更新跟踪器对象数据。
		m_resultType = m_tracker.AnalyseImg(m_cameraImg);
	}

	// 运动到下一点。
	public boolean Run() {

		// 获取运动目标点。
		// 因为每次运行后已更新跟踪器数据，此处无需再次更新。
		m_targetPoint.setLocation(m_tracker.GetResult());

		// 转换目标点到小车坐标系表示。
		m_targetVec2.set(m_targetPoint.x, m_targetPoint.y);
		m_targetVec2.x -= k_cameraWidth / 2;
		m_targetVec2.y = k_cameraHeight - m_targetVec2.y;
		m_targetVec2.x /= k_mToPixelScale;
		m_targetVec2.y /= k_mToPixelScale;

		// 传入运动目标点，获取运动学参数。
		m_motionArithmetic.GetMotionArgu(vLeft, vRight, m_targetVec2,
				m_resultType);
		// 传入运动学参数，更新小车位置。
		// 将线速度转换为速度向量，速度向量永远垂直于小车定义点连线。
		// 计算角度。
		Vec2 vec2L = m_carLeftBody.getPosition();
		Vec2 vec2R = m_carRightBody.getPosition();
		double theta = vec2L.x == vec2R.x ? Math.PI / 2 : Math
				.atan((vec2L.y - vec2R.y) / (vec2L.x - vec2R.x));
		if (vec2L.x > vec2R.x)
			theta += Math.PI;
		// 应用旋转矩阵。
		vLeft.x = (float) (vLeft.x * Math.cos(theta) - vLeft.y
				* Math.sin(theta));
		vLeft.y = (float) (vLeft.x * Math.sin(theta) + vLeft.y
				* Math.cos(theta));
		vRight.x = (float) (vRight.x * Math.cos(theta) - vRight.y
				* Math.sin(theta));
		vRight.y = (float) (vRight.x * Math.sin(theta) + vRight.y
				* Math.cos(theta));
		// 应用速度。
		m_carLeftBody.setLinearVelocity(vLeft);
		m_carRightBody.setLinearVelocity(vRight);

		// 按Box2D推荐参数运行。
		m_world.step(k_stepTime, 10, 8);

		// 更新图像传感器图像。
		UpdateCameraImg();
		// 更新跟踪器对象数据。
		m_resultType = m_tracker.AnalyseImg(m_cameraImg);

		return true;
	}

	// 获取小车位置。位置以地图图像坐标定义。
	public void GetLocation(Vec2 left, Vec2 right) {
		Vec2 vec2 = new Vec2(m_carLeftBody.getPosition().mul(k_mToPixelScale));
		// 反转坐标。
		vec2.y = m_mapImg.getHeight() - vec2.y;
		left.set(vec2);
		vec2.set(m_carRightBody.getPosition().mul(k_mToPixelScale));
		// 反转坐标。
		vec2.y = m_mapImg.getHeight() - vec2.y;
		right.set(vec2);
	}

	// 获取图像传感器图像的引用。
	public final BufferedImage GetShortcut() {
		return m_cameraImg;
	}

	// 获取跟踪器的引用。
	public final Tracker GetTracker() {
		return m_tracker;
	}

	// 工具函数。

	// 更新图像传感器图像。
	protected void UpdateCameraImg() {
		// 小车位置的地图坐标。
		Vec2 vec2L = m_carLeftBody.getPosition().mul(k_mToPixelScale);
		Vec2 vec2R = m_carRightBody.getPosition().mul(k_mToPixelScale);
		// 翻转坐标。
		vec2L.y = m_mapImg.getHeight() - vec2L.y;
		vec2R.y = m_mapImg.getHeight() - vec2R.y;
		// 角度。
		double theta = vec2L.x == vec2R.x ? Math.PI / 2 : Math
				.atan((vec2L.y - vec2R.y) / (vec2L.x - vec2R.x));
		if (vec2L.x > vec2R.x)
			theta += Math.PI;
		// 小车中点。
		int midX = (int) Math.round((vec2L.x + vec2R.x) / 2);
		int midY = (int) Math.round((vec2L.y + vec2R.y) / 2);

		// 图像传感器原点。
		int cameraOriginX = (int) Math.round(midX - k_cameraWidth / 2
				* Math.cos(theta) + k_cameraHeight * Math.sin(theta));
		int cameraOriginY = (int) Math.round(midY - k_cameraWidth / 2
				* Math.sin(theta) - k_cameraHeight * Math.cos(theta));

		// 每个图像传感器像素逐一映射复制。
		for (int x = 0; x < k_cameraWidth; ++x) {
			for (int y = 0; y < k_cameraHeight; ++y) {
				// 先将图像传感器图像的坐标点乘旋转矩阵，后加上图像传感器原点坐标从而完成图像传感器像素到地图像素的一对一映射。
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

package CleverMonkey.MotionArithmetic;

import org.jbox2d.common.Vec2;

import CleverMonkey.Tracker.Tracker;

public class MotionArithmetic {

	// 小车两轮间距的一半。
	protected final float k_carWidthHalf = 0.07f;

	// 中心点本底速度(m/s)。
	protected float m_vBase = 0.05f;

	// 接口。

	// 通过提供的运动目标点及运动参数，算出对应的运动学控制参数。
	public void GetMotionArgu(Vec2 vLeft, Vec2 vRight, final Vec2 target,
			Tracker.ResultType resultType) {
		// 角位移。
		double theta = Math.abs(Math.atan(target.x / target.y));
		// 瞬心半径。
		double r = Math.sqrt((target.x * target.x + target.y * target.y)
				/ (2 * (1 + Math.cos(theta))));

		if (target.x >= 0) {
			vLeft.set(0, (float) (m_vBase * (1 + k_carWidthHalf / r)));
			vRight.set(0, (float) (m_vBase * (1 - k_carWidthHalf / r)));
		} else {
			vLeft.set(0, (float) (m_vBase * (1 - k_carWidthHalf / r)));
			vRight.set(0, (float) (m_vBase * (1 + k_carWidthHalf / r)));
		}

		if (resultType == resultType.Stop) {
			vLeft.set(0, 0);
			vRight.set(0, 0);
		} else if (resultType == resultType.Unk) {
			vLeft.set(0, m_vBase);
			vRight.set(0, m_vBase);
		}
	}

	public void GetMotionArgu2(Vec2 vLeft, Vec2 vRight, final Vec2 target,
			Tracker.ResultType resultType) {
		double l0 = Math.sqrt(target.x * target.x + target.y * target.y);
		double theta1 = Math.atan((double) target.y / target.x);
		double delta = Math.PI - theta1;
		double a = 8 * Math.cos(delta) - 4;
		double b = 8 * 2 * k_carWidthHalf * Math.cos(delta) - 4 * 2
				* k_carWidthHalf;
		double c = 2 * 2 * k_carWidthHalf * 2 * k_carWidthHalf
				* Math.cos(delta) - 4 * l0 * l0 - 2 * k_carWidthHalf * 2
				* k_carWidthHalf;
		double r1 = (-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a);
		double r2 = (-b - Math.sqrt(b * b - 4 * a * c)) / (2 * a);
		double r = r1 >= 0 ? r1 : r2;
		double theta = Math.PI - 2 * theta1;
		double t = target.y / m_vBase;

		vLeft.set(0, (float) (r * theta / t));
		vRight.set(0, (float) ((2 * k_carWidthHalf + r) * theta / t));
	}
}

package clevermonkey.center;

import android.graphics.Point;

/**
 * Created by Sheldon on 2016/3/22.
 */


public class MotorDriver {

    //閫熷害鍖归厤鏁扮粍銆
    float[] m_vLeft = new float[101];
    float[] m_vRight = new float[101];

    public Point GetMotorArgu(float vLeft, float vRight) {
        //椹卞姩绾ф暟銆
        int left = 0;
        int right = 0;

        for (; left < 101; ++left) {
            if (vLeft <= m_vLeft[left])
                break;
        }
        left = vLeft - m_vLeft[left - 1] > m_vLeft[left] - vLeft ? left : left - 1;
        left -= 51;

        for (; right < 101; ++right) {
            if (vRight <= m_vRight[right])
                break;
        }
        right = right - m_vRight[right - 1] > m_vRight[right] - vRight ? right : right - 1;
        right -= 51;

        return new Point(left, right);
    }

    ;
}

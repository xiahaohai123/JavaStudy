package math;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class TrigonometricCoordinateCalculator {

    /**
     * 计算从 X 轴正方向开始旋转的某点圆弧坐标
     * 使用计算机直角坐标系
     * @param circle 圆心
     * @param r      圆弧对应的半径长度
     * @param angle  旋转角度
     * @return 目标点坐标
     */
    public static Point calPoint(Point circle, double r, double angle) {
        double dx = r * cos(angle2Radian(angle));
        double dy = r * sin(angle2Radian(angle));
        double x = circle.x + dx;
        double y = circle.y - dy;
        return new Point(x, y);
    }

    /** 角度值转弧度值 */
    private static double angle2Radian(double angle) {
        return angle / 180 * PI;
    }
}

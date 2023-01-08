package math;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BezierCurveCalculator {

    private static final Log log = LogFactory.getLog(BezierCurveCalculator.class);

    public static void main(String[] args) {
        calculate(0, 0, 2, 1, -Math.sqrt(3), 0);
        calculate(0, 0, 2, -1, Math.sqrt(3), 0);
    }


    /**
     * 计算三阶贝赛尔曲线绘制圆弧的终点坐标以及两个控制棒坐标 只接受 1/4 圆及以内的贝赛尔曲线计算 注意: 整个计算过程使用的坐标系都是计算机直角坐标系
     * @param cx     圆弧对应圆心横坐标
     * @param cy     圆弧对应圆心纵坐标
     * @param r      圆弧对应圆心半径
     * @param startX 圆弧起始横坐标
     * @param startY 圆弧起始纵坐标
     * @param n      n/1 个圆
     * @return 圆弧的终点坐标以及两个控制棒坐标
     */
    public static Result calculate(double cx, double cy, double r, double startX, double startY, int n) {
        // dx = d * cos(θ)
        // dy = d * sin(θ)
        // 计算以圆心为坐标轴正方向开始逆时针旋转到起始线段的角度
        double angle0Begin = getAngleBegin(cx, cy, r, startX, startY);
        log.info("angle0Begin: " + angle0Begin);
        // TODO: 2023/1/7 计算坐标轴正方向开始逆时针旋转到线段 (cx, cy), (endX, endY) 的角度
        // TODO: 2023/1/7 通过计算圆心到终点坐标的 dx 与 dy 来计算终点坐标
        // d(controlBar) = (4/3)tan(π/2n)
        // TODO: 2023/1/7 计算出控制棒长度
        // TODO: 2023/1/7 计算控制棒构建的直角三角形靠圆心侧内角 反三角函数
        // TODO: 2023/1/7 使用三角函数计算出 dx dy，最终计算出控制棒终点坐标
        return null;
    }

    /** 计算以圆心为坐标轴正方向开始逆时针旋转到起始线段的角度 */
    private static double getAngleBegin(double cx, double cy, double r, double startX, double startY) {
        double radian0Begin = Math.acos((startX - cx) / r);
        double angle0Begin = radian0Begin / Math.PI * 180;
        if (startY > cy) {
            angle0Begin = 360 - angle0Begin;
        }
        return angle0Begin;
    }

    class Result {
        int endX;
        int endY;
        int control1X;
        int control1Y;
        int control2X;
        int control2Y;
    }
}

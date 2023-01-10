package math;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

public class BezierCurveCalculator {

    private static final Log log = LogFactory.getLog(BezierCurveCalculator.class);

    public static Result calculate(Point circle, double r, double startAngle, double dAngle) {
        Point start = calPoint(circle.x, circle.y, r, angle2Radian(startAngle));
        // TODO: 2023/1/10 优化: 已知起点或已知起点角度，两种策略作为上层方法，公用下层方法
        return calculate(circle, r, start, dAngle);
    }

    public static Result calculate(Point circle, double r, Point start, double angle) {
        return calculate(circle.x, circle.y, r, start.x, start.y, angle);
    }


    /**
     * 计算三阶贝赛尔曲线绘制圆弧的终点坐标以及两个控制棒坐标 只接受 1/4 圆及以内的贝赛尔曲线计算 注意: 整个计算过程使用的坐标系都是计算机直角坐标系
     * @param cx     圆弧对应圆心横坐标
     * @param cy     圆弧对应圆心纵坐标
     * @param r      圆弧对应圆心半径
     * @param startX 圆弧起始横坐标
     * @param startY 圆弧起始纵坐标
     * @param angle  顶角角度
     * @return 圆弧的终点坐标以及两个控制棒坐标
     */
    public static Result calculate(double cx, double cy, double r, double startX, double startY, double angle) {
        if (angle < 0 || angle > 90) {
            throw new IllegalArgumentException("unsupported angle");
        }
        // dx = d * cos(θ)
        // dy = d * sin(θ)
        // 计算以圆心为坐标轴正方向开始逆时针旋转到起始线段的角度
        double angle0Begin = calAngleBegin(cx, cy, r, startX, startY);
        log.debug("angle0Begin: " + angle0Begin);

        // 计算以圆心为坐标轴正方向开始逆时针旋转到线段 (cx, cy)(endX, endY) 的角度
        double angle0End = angle0Begin + angle;
        log.debug("angle0End: " + angle0End);
        double radian0End = angle2Radian(angle0End);
        Point endPoint = calPoint(cx, cy, r, radian0End);
        log.debug("end point: " + endPoint);

        // 计算出控制棒长度
        double lengthControlBar = calControlBarLength(r, angle);
        log.debug("d(control bar): " + lengthControlBar);
        // 计算控制棒构建的直角三角形靠圆心侧内角 反三角函数
        double radianControlBar = atan(lengthControlBar / r);
        double angleControlBar = radian2Angle(radianControlBar);
        double lengthHypotenuseControlBar = sqrt(pow(lengthControlBar, 2) + pow(r, 2));

        // 使用三角函数计算出 dx dy，最终计算出控制棒终点坐标
        double angle0HypotenuseControlBar1 = angle0Begin + angleControlBar;
        double radian0HypotenuseControlBar1 = angle2Radian(angle0HypotenuseControlBar1);
        Point pointControl1 = calPoint(cx, cy, lengthHypotenuseControlBar, radian0HypotenuseControlBar1);

        double angle0HypotenuseControlBar2 = angle0End - angleControlBar;
        double radian0HypotenuseControlBar2 = angle2Radian(angle0HypotenuseControlBar2);
        Point pointControl2 = calPoint(cx, cy, lengthHypotenuseControlBar, radian0HypotenuseControlBar2);

        int scale = 12;
        Result result = new Result(endPoint, pointControl1, pointControl2);
        result.setStartPoint(new Point(startX, startY));
        result.round(scale);
        return result;
    }

    /**
     * 计算控制棒长度
     * d(controlBar) = (4/3)tan(π/2n), n 表示 1/n 个圆
     * 假如传入 angle = 90，则为 1/4 个圆，得 (4/3)tan(π/8) = 0.552284749831
     * @param angle 圆弧角度值
     */
    private static double calControlBarLength(double r, double angle) {
        double n = angle / 360;
        return 4.0 / 3.0 * tan(PI * n / 2) * r;
    }

    /** 计算以圆心为坐标轴正方向开始逆时针旋转到起始线段的角度 */
    private static double calAngleBegin(double cx, double cy, double r, double startX, double startY) {
        double radian0Begin = acos((startX - cx) / r);
        double angle0Begin = radian2Angle(radian0Begin);
        if (startY > cy) {
            angle0Begin = 360 - angle0Begin;
        }
        return angle0Begin;
    }

    /** 通过计算圆心到终点坐标的 dx 与 dy 来计算终点坐标 */
    private static Point calPoint(double cx, double cy, double hypotenuse, double radian) {
        double dx = cos(radian) * hypotenuse;
        double dy = sin(radian) * hypotenuse;
        double x = cx + dx;
        double y = cy - dy;
        return new Point(x, y);
    }

    /** 弧度值转角度值 */
    private static double radian2Angle(double radian) {
        return radian / PI * 180;
    }

    /** 角度值转弧度值 */
    private static double angle2Radian(double angle) {
        return angle / 180 * PI;
    }

    static class Result {
        Point startPoint;
        Point endPoint;
        Point controlPoint1;
        Point controlPoint2;
        Point dEndPoint;
        Point dControlPoint1;
        Point dControlPoint2;

        public Result(Point endPoint, Point controlPoint1, Point controlPoint2) {
            this.endPoint = endPoint;
            this.controlPoint1 = controlPoint1;
            this.controlPoint2 = controlPoint2;
        }

        public void setStartPoint(Point startPoint) {
            this.startPoint = startPoint;
            dEndPoint = new Point(endPoint.x - startPoint.x, endPoint.y - startPoint.y);
            dControlPoint1 = new Point(controlPoint1.x - startPoint.x, controlPoint1.y - startPoint.y);
            dControlPoint2 = new Point(controlPoint2.x - startPoint.x, controlPoint2.y - startPoint.y);
        }

        @Override
        public String toString() {
            return "Result{" +
                    "endPoint=" + endPoint +
                    ", controlPoint1=" + controlPoint1 +
                    ", controlPoint2=" + controlPoint2 +
                    ", dEndPoint=" + dEndPoint +
                    ", dControlPoint1=" + dControlPoint1 +
                    ", dControlPoint2=" + dControlPoint2 +
                    '}';
        }

        public void round(int scale) {
            endPoint.round(scale);
            controlPoint1.round(scale);
            controlPoint2.round(scale);
            dEndPoint.round(scale);
            dControlPoint1.round(scale);
            dControlPoint2.round(scale);
        }

        public String getPath() {
            return String.format("M %s %s c %s %s %s %s %s %s",
                    startPoint.x, startPoint.y,
                    dControlPoint1.x, dControlPoint1.y,
                    dControlPoint2.x, dControlPoint2.y,
                    dEndPoint.x, dEndPoint.y);
        }
    }

    static class Point {
        double x;
        double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "(x=" + x + ", y=" + y + ")";
        }

        public Point round(int scale) {
            x = BigDecimal.valueOf(x).setScale(scale, RoundingMode.HALF_UP).doubleValue();
            y = BigDecimal.valueOf(y).setScale(scale, RoundingMode.HALF_UP).doubleValue();
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return Double.compare(point.x, x) == 0 && Double.compare(point.y, y) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}

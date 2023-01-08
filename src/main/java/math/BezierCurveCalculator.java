package math;


public class BezierCurveCalculator {
    public static void main(String[] args) {

    }


    /**
     * 计算三阶贝赛尔曲线绘制圆弧的终点坐标以及两个控制棒坐标
     * 只接受 1/4 圆及以内的贝赛尔曲线计算
     * @param cx     圆弧对应圆心横坐标
     * @param cy     圆弧对应圆心纵坐标
     * @param r      圆弧对应圆心半径
     * @param startX 圆弧起始横坐标
     * @param startY 圆弧起始纵坐标
     * @param n      n/1 个圆
     * @return 圆弧的终点坐标以及两个控制棒坐标
     */
    public static Result calculate(int cx, int cy, int r, int startX, int startY, int n) {
        // TODO: 2023/1/7 直接使用计算机直角坐标系进行计算
        // dx = d * cos(θ)
        // dy = d * sin(θ)
        // TODO: 2023/1/7 计算坐标轴正方向开始逆时针旋转到起始线段的角度
        // TODO: 2023/1/7 计算坐标轴正方向开始逆时针旋转到线段 (cx, cy), (endX, endY) 的角度
        // TODO: 2023/1/7 通过计算圆心到终点坐标的 dx 与 dy 来计算终点坐标
        // d(controlBar) = (4/3)tan(π/2n)
        // TODO: 2023/1/7 计算出控制棒长度
        // TODO: 2023/1/7 计算控制棒构建的直角三角形靠圆心侧内角 反三角函数
        // TODO: 2023/1/7 使用三角函数计算出 dx dy，最终计算出控制棒终点坐标
        return null;
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

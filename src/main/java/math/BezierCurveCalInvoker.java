package math;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static math.BezierCurveCalculator.calculate;

public class BezierCurveCalInvoker {
    private static final Log log = LogFactory.getLog(BezierCurveCalInvoker.class);

    public static void main(String[] args) {
        Point circle = new Point(64, 64);
        double r = 47;
        double startAngle = 75;
        double dAngle = 30;
        BezierCurveCalculator.Result result = calculate(circle, r, startAngle, dAngle);
        log.info("startPoint " + result.startPoint);
        log.info("dControlPoint1 " + result.dStartEndControlPoint1);
        log.info("dControlPoint2 " + result.dStartEndControlPoint2);
        log.info("dEndPoint " + result.dStartEndPoint);
        log.info("path: \n" + result.getPath());
        log.info("path revert: \n" + result.getPathRevert());

        //Point point = TrigonometricCoordinateCalculator.calPoint(circle, r - 3, 105);
        //log.info("point: " + point);
        //log.info("point: \n" + point.x + " " + point.y);
    }
}

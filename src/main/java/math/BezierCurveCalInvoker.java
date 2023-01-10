package math;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static math.BezierCurveCalculator.calculate;

public class BezierCurveCalInvoker {
    private static final Log log = LogFactory.getLog(BezierCurveCalInvoker.class);

    public static void main(String[] args) {
        BezierCurveCalculator.Point circle = new BezierCurveCalculator.Point(64, 64);
        double r = 30;
        double startAngle = 300;
        double dAngle = 25;
        BezierCurveCalculator.Result result = calculate(circle, r, startAngle, dAngle);
        log.info("path: " + result.getPath());
        log.info("startPoint " + result.startPoint);
        log.info("dControlPoint1 " + result.dControlPoint1);
        log.info("dControlPoint2 " + result.dControlPoint2);
        log.info("dEndPoint " + result.dEndPoint);
    }
}

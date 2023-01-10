package math;

import math.BezierCurveCalculator.Point;
import math.BezierCurveCalculator.Result;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BezierCurveCalculatorTest {

    private final Log log = LogFactory.getLog(this.getClass());

    private static final String CASE_NAME = "name";
    private static final String CIRCLE = "circle";
    private static final String RADIUS = "r";
    private static final String START_POINT = "start";
    private static final String ANGLE = "angle";
    private static final String EXPECT_D_CONTROL_POINT1 = "expectDControlPoint1";
    private static final String EXPECT_D_CONTROL_POINT2 = "expectDControlPoint2";
    private static final String EXPECT_D_END = "expectDEnd";

    @Test
    void testCalculate() {
        List<Map<String, Object>> testCases = createCases();
        for (Map<String, Object> testCase : testCases) {
            log.info("\ntest case: " + testCase.get(CASE_NAME));
            Result result = BezierCurveCalculator.calculate((Point) testCase.get(CIRCLE),
                    obj2Double(testCase.get(RADIUS)), (Point) testCase.get(START_POINT),
                    obj2Double(testCase.get(ANGLE)));
            int equalScale = 10;
            assertEquals(((Point) testCase.get(EXPECT_D_CONTROL_POINT1)).round(equalScale),
                    result.dControlPoint1.round(equalScale));
            assertEquals(((Point) testCase.get(EXPECT_D_CONTROL_POINT2)).round(equalScale),
                    result.dControlPoint2.round(equalScale));
            assertEquals(((Point) testCase.get(EXPECT_D_END)).round(equalScale),
                    result.dEndPoint.round(equalScale));
        }
    }

    private List<Map<String, Object>> createCases() {
        List<Map<String, Object>> testCases = new ArrayList<>();
        testCases.add(new HashMap<String, Object>() {
            {
                put(CASE_NAME, "case1");
                put(CIRCLE, new Point(64, 64));
                put(RADIUS, 24);
                put(START_POINT, new Point(64 + 24, 64));
                put(ANGLE, 90);
                put(EXPECT_D_CONTROL_POINT1, new Point(0, -13.254833995944));
                put(EXPECT_D_CONTROL_POINT2, new Point(-10.745166004056, -24));
                put(EXPECT_D_END, new Point(-24, -24));
            }
        });
        testCases.add(new HashMap<String, Object>() {
            {
                put(CASE_NAME, "case2");
                put(CIRCLE, new Point(64, 64));
                put(RADIUS, 48);
                put(START_POINT, new Point(64, 64 + 48));
                put(ANGLE, 90);
                put(EXPECT_D_CONTROL_POINT1, new Point(26.509667991888, 0));
                put(EXPECT_D_CONTROL_POINT2, new Point(48, -21.490332008112));
                put(EXPECT_D_END, new Point(48, -48));
            }
        });
        testCases.add(new HashMap<String, Object>() {
            {
                put(CASE_NAME, "case3");
                put(CIRCLE, new Point(64, 64));
                put(RADIUS, 48);
                put(START_POINT, new Point(64 - 48, 64));
                put(ANGLE, 90);
                put(EXPECT_D_CONTROL_POINT1, new Point(0, 26.509667991888));
                put(EXPECT_D_CONTROL_POINT2, new Point(21.490332008112, 48));
                put(EXPECT_D_END, new Point(48, 48));
            }
        });
        testCases.add(new HashMap<String, Object>() {
            {
                put(CASE_NAME, "case4");
                put(CIRCLE, new Point(64, 64));
                put(RADIUS, 48);
                put(START_POINT, new Point(64, 64 - 48));
                put(ANGLE, 90);
                put(EXPECT_D_CONTROL_POINT1, new Point(-26.509667991888, 0));
                put(EXPECT_D_CONTROL_POINT2, new Point(-48, 21.490332008112));
                put(EXPECT_D_END, new Point(-48, 48));
            }
        });
        return testCases;
    }

    private double obj2Double(Object a) {
        if (a instanceof Integer) {
            return (Integer) a + 0.0;
        }
        return (double) a;
    }
}
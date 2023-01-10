package math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import math.BezierCurveCalculator.Point;
import math.BezierCurveCalculator.Result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BezierCurveCalculatorTest {

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
        List<Map<String, Object>> testCases = new ArrayList<>();
        testCases.add(new HashMap<String, Object>() {
            {
                put(CASE_NAME, "case1");
                put(CIRCLE, new Point(64, 64));
                put(RADIUS, 24);
                put(START_POINT, new Point(64 + 24, 64));
                put(ANGLE, 90.0);
                put(EXPECT_D_CONTROL_POINT1, new Point(0, -13.254833995944));
                put(EXPECT_D_CONTROL_POINT2, new Point(-10.745166004056, -24));
                put(EXPECT_D_END, new Point(-24, -24));
            }
        });
        for (Map<String, Object> testCase : testCases) {
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

    private double obj2Double(Object a) {
        if (a instanceof Integer) {
            return (Integer) a + 0.0;
        }
        return (double) a;
    }
}
package math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Point {
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

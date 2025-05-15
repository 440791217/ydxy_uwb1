package org.ydxy.uwb.utils;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


public class UwbInner {

    static class Entity{

    }

    // 表示二维平面上的点
    public static class Point {
        double x;
        double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return String.format("(%.4f, %.4f)", x, y);
        }
    }

    /**
     * 计算两圆的交点坐标
     *
     * @param x1 第一个圆的圆心x坐标
     * @param y1 第一个圆的圆心y坐标
     * @param r1 第一个圆的半径
     * @param x2 第二个圆的圆心x坐标
     * @param y2 第二个圆的圆心y坐标
     * @param r2 第二个圆的半径
     * @return 包含交点的列表，如果没有交点则返回空列表
     */
    public static List<Point> calculateIntersections(double x1, double y1, double r1,
                                                     double x2, double y2, double r2) {
        List<Point> intersections = new ArrayList<>();

        // 计算两圆心之间的距离的平方
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dSquared = dx * dx + dy * dy;
        double d = Math.sqrt(dSquared);

        // 处理特殊情况
        if (dSquared == 0) {
            // 两圆心重合
            if (r1 == r2) {
                // 两圆完全重合，有无穷多个交点，返回空列表表示无法确定
                return intersections;
            } else {
                // 同心圆，半径不同，无交点
                return intersections;
            }
        }

        // 检查两圆是否相离或一个圆完全包含另一个圆
        if (d > r1 + r2 || d < Math.abs(r1 - r2)) {
            return intersections;
        }

        // 计算根轴直线方程的参数 (Ax + By + C = 0)
        double A = 2 * dx;
        double B = 2 * dy;
        double C = r1 * r1 - r2 * r2 - dx * dx - dy * dy;

        if (B == 0) {
            // 直线垂直于x轴，直接解x
            double x = -C / A;
            double discriminant = r1 * r1 - (x - x1) * (x - x1);

            if (discriminant < 0) {
                return intersections; // 无实数解
            }

            double sqrtDiscriminant = Math.sqrt(discriminant);
            double y1Val = y1 + sqrtDiscriminant;
            double y2Val = y1 - sqrtDiscriminant;

            intersections.add(new Point(x, y1Val));
            if (Math.abs(y1Val - y2Val) > 1e-9) { // 检查是否为两个不同的点
                intersections.add(new Point(x, y2Val));
            }
        } else {
            // 直线不垂直于x轴，解 y = mx + b 形式
            double m = -A / B;
            double b = -C / B;

            // 代入第一个圆的方程，求解一元二次方程
            double coeffA = 1 + m * m;
            double coeffB = 2 * (x1 + m * (y1 - b));
            double coeffC = x1 * x1 + (y1 - b) * (y1 - b) - r1 * r1;

            double discriminant = coeffB * coeffB - 4 * coeffA * coeffC;

            if (discriminant < 0) {
                return intersections; // 无实数解
            }

            double sqrtDiscriminant = Math.sqrt(discriminant);
            double x1Val = (-coeffB + sqrtDiscriminant) / (2 * coeffA);
            double x2Val = (-coeffB - sqrtDiscriminant) / (2 * coeffA);
            double y1Val = m * x1Val + b;
            double y2Val = m * x2Val + b;

            intersections.add(new Point(x1Val, y1Val));
            if (Math.abs(x1Val - x2Val) > 1e-9 || Math.abs(y1Val - y2Val) > 1e-9) {
                intersections.add(new Point(x2Val, y2Val));
            }
        }

        return intersections;
    }

    public static void main(String[] args) {
        // 示例：计算两个圆的交点
        double x1 = 10, y1 = 10, r1 = 5;  // 第一个圆的圆心和半径
        double x2 = 20, y2 = 10, r2 = 7;  // 第二个圆的圆心和半径

        List<Point> intersections = calculateIntersections(x1, y1, r1, x2, y2, r2);

        if (intersections.isEmpty()) {
            System.out.println("两圆无交点");
        } else {
            System.out.println("两圆的交点坐标为：");
            for (Point p : intersections) {
                System.out.println(p);
            }
        }
    }
}

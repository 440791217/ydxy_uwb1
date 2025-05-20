package org.ydxy.uwb.utils;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


public class UwbInner {
    /** 浮点数比较容差 */
    private static final double EPSILON = 1e-9;
    static class Entity{

    }

    // 表示二维平面上的点
    public static class Point {
        public double x;
        public double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        @Override
        public String toString() {
            return String.format("(%.4f, %.4f)", x, y);
        }
    }

    /**
     * 求解两个圆方程的交点
     * @param x1 第一个圆心x坐标
     * @param y1 第一个圆心y坐标
     * @param r1 第一个圆半径
     * @param x2 第二个圆心x坐标
     * @param y2 第二个圆心y坐标
     * @param r2 第二个圆半径
     * @return 交点列表，如果没有交点返回空列表
     */
    public static List<Point> solve(double x1, double y1, double r1, double x2, double y2, double r2) {
        List<Point> result = new ArrayList<>();

        // 计算两圆心之间的距离
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dSquared = dx * dx + dy * dy;
        double d = Math.sqrt(dSquared);

        // 处理圆心重合的情况
        if (d < EPSILON) {
            if (Math.abs(r1 - r2) < EPSILON) {
                // 同心圆且半径相等，有无穷多个解
                return null;
            } else {
                // 同心圆但半径不等，没有解
                return result;
            }
        }

        // 检查是否没有交点（相离或包含）
        if (d > r1 + r2 + EPSILON || d < Math.abs(r1 - r2) - EPSILON) {
            return result;
        }

        // 计算辅助变量
        double a = (r1 * r1 - r2 * r2 + dSquared) / (2 * d);
        double hSquared = r1 * r1 - a * a;

        // 如果h²为负，则没有实根（数值误差可能导致微小负数）
        if (hSquared < -EPSILON) {
            return result;
        }

        double h = Math.sqrt(Math.max(0, hSquared));

        // 计算中心线的单位向量
        double cx = (x2 - x1) / d;
        double cy = (y2 - y1) / d;

        // 计算中心线的中点
        double mx = x1 + a * cx;
        double my = y1 + a * cy;

        // 如果h为0，表示相切，只有一个交点
        if (h < EPSILON) {
            result.add(new Point(mx, my));
            return result;
        }

        // 计算两个交点（相交情况）
        double perpCx = -cy;
        double perpCy = cx;

        double x3 = mx + h * perpCx;
        double y3 = my + h * perpCy;
        double x4 = mx - h * perpCx;
        double y4 = my - h * perpCy;

        result.add(new Point(x3, y3));
        result.add(new Point(x4, y4));

        return result;
    }


    public static void main(String[] args) {
        // 示例：计算两个圆的交点
        double x1 = 1000, y1 = 1000, r1 = 100;  // 第一个圆的圆心和半径
        double x2 = 1000, y2 = 1200, r2 = 100;  // 第二个圆的圆心和半径

        List<Point> intersections = solve(x1, y1, r1, x2, y2, r2);

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

package org.ydxy.uwb.utils;

import org.ydxy.uwb.entity.UwbEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UwbToa2D {
    private UwbToa2D() {}

    public static void uwbToaTF2D(UwbEntity[] entities, double results[]) {
        double d1, d2, d3;
        d1 = entities[0].getDist();
        d2 = entities[1].getDist();
        d3 = entities[2].getDist();
        double[] p1 = entities[0].getP();
        double[] p2 = entities[1].getP();
        double[] p3 = entities[2].getP();

        double x1 = p1[0], y1 = p1[1];
        double x2 = p2[0], y2 = p2[1];
        double x3 = p3[0], y3 = p3[1];

        // 构建线性方程组的系数
        double A = 2 * (x2 - x1);
        double B = 2 * (y2 - y1);
        double C = d1 * d1 - d2 * d2 - x1 * x1 + x2 * x2 - y1 * y1 + y2 * y2;

        double D = 2 * (x3 - x1);
        double E = 2 * (y3 - y1);
        double F = d1 * d1 - d3 * d3 - x1 * x1 + x3 * x3 - y1 * y1 + y3 * y3;

        // 求解线性方程组
        double denominator = A * E - B * D;
        results[0] = (C * E - B * F) / denominator;
        results[1] = (A * F - C * D) / denominator;
    }

    public static void main(String[] args) {
        List<UwbEntity> uwbEntityList = new ArrayList<>();
        // 基站1采集
        UwbEntity u1 = new UwbEntity();
        u1.setDevId("YDJZ-25010001");
        u1.setP(new double[]{179, 370});
        u1.setDist(170);
        uwbEntityList.add(u1);
        // 基站2采集
        UwbEntity u2 = new UwbEntity();
        u2.setDevId("YDJZ-25010009");
        u2.setP(new double[]{186, 1256});
        u2.setDist(1061);
        uwbEntityList.add(u2);
        // 基站3采集
        UwbEntity u3 = new UwbEntity();
        u3.setDevId("YDJZ-25010004");
        u3.setP(new double[]{948, 1280});
        u3.setDist(1349);
        uwbEntityList.add(u3);

        // 计算
        double[] results = new double[2];
        UwbToa2D.uwbToaTF2D(uwbEntityList.toArray(new UwbEntity[3]), results);
        System.out.println(Arrays.toString(results));
    }
}

package org.ydxy.uwb.utils;


import org.ydxy.uwb.entity.UwbEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UwbToa3D {
	private UwbToa3D() {}

	public static void uwbToaTF3D(UwbEntity[] entities, double results[]) {
		double d1,d2,d3,d4;
		d1 = entities[0].getDist();
		d2 = entities[1].getDist();
		d3 = entities[2].getDist();
		d4 = entities[3].getDist();
		double[] p1= entities[0].getP();
		double[] p2= entities[1].getP();
		double[] p3= entities[2].getP();
		double[] p4= entities[3].getP();

		//double* p1, double* p2, double* p3, double* p4,
		//    double* x, double* y, double* z
		double x1 = p1[0], y1 = p1[1], z1 = p1[2];
		double x2 = p2[0], y2 = p2[1], z2 = p2[2];
		double x3 = p3[0], y3 = p3[1], z3 = p3[2];
		double x4 = p4[0], y4 = p4[1], z4 = p4[2];

		// 构建线性方程组的系数
		double A = 2 * (x2 - x1);
		double B = 2 * (y2 - y1);
		double C = 2 * (z2 - z1);
		double D = d1 * d1 - d2 * d2 - x1 * x1 + x2 * x2 - y1 * y1 + y2 * y2 - z1 * z1 + z2 * z2;

		double E = 2 * (x3 - x1);
		double F = 2 * (y3 - y1);
		double G = 2 * (z3 - z1);
		double H = d1 * d1 - d3 * d3 - x1 * x1 + x3 * x3 - y1 * y1 + y3 * y3 - z1 * z1 + z3 * z3;

		double I = 2 * (x4 - x1);
		double J = 2 * (y4 - y1);
		double K = 2 * (z4 - z1);
		double L = d1 * d1 - d4 * d4 - x1 * x1 + x4 * x4 - y1 * y1 + y4 * y4 - z1 * z1 + z4 * z4;

		// 求解线性方程组
		double M = A * (F * K - G * J) - B * (E * K - G * I) + C * (E * J - F * I);
		results[0] = (D * (F * K - G * J) - B * (H * K - G * L) + C * (H * J - F * L)) / M;
		results[1] = (A * (H * K - G * L) - D * (E * K - G * I) + C * (E * L - H * I)) / M;
		results[2] = (A * (F * L - H * J) - B * (E * L - H * I) + D * (E * J - F * I)) / M;
	}


	public static void main(String[] args) {
		List<UwbEntity> uwbEntityList =  new ArrayList<>();
		// 基站1采集
		UwbEntity u1 = new UwbEntity();
		u1.setDevId("YDJZ-25010001");
		u1.setP(new double[]{179, 370, 182});
		u1.setDist(170);
		uwbEntityList.add(u1);
		// 基站2采集
		UwbEntity u2 = new UwbEntity();
		u2.setDevId("YDJZ-25010009");
		u2.setP(new double[]{186, 1256, 185});
		u2.setDist(1061);
		uwbEntityList.add(u2);
		// 基站3采集
		UwbEntity u3 = new UwbEntity();
		u3.setDevId("YDJZ-25010004");
		u3.setP(new double[]{948, 1280, 190});
		u3.setDist(1349);
		uwbEntityList.add(u3);
		// 基站4采集
		UwbEntity u4 = new UwbEntity();
		u4.setDevId("YDJZ-25010010");
		u4.setP(new double[]{948, 370, 195});
		u4.setDist(798);
		uwbEntityList.add(u4);
		// 计算
		double[] results = new double[3];
		UwbToa3D.uwbToaTF3D(uwbEntityList.toArray(new UwbEntity[4]), results);
		System.out.println(Arrays.toString(results));
	}
}

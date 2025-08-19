package org.ydxy.uwb.app;

import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.ydxy.uwb.entity.PointEntity;
import org.ydxy.uwb.tool.ExpiringFixedSizeNumberAnalysisQueue;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class TdoaApp {

    @Getter
    @Setter
    static HashMap<SiteQueueKey, LinkedList<ExpiringFixedSizeNumberAnalysisQueue<Double>>> siteQueueMap;
    @Getter
    @Setter
    static int siteQueueSize = 10;

    @Data
    @Builder
    static class SiteQueueKey {
        private String mainDevId;
        private String tagNum;
    }

    public static class Input{
        public List<Double> xList;
        public List<Double> yList;
        public List<Double> zList;
        public List<Double> tdoaData;
        public List<String> anchorNames=new ArrayList<>();
        public long ts=0;
        public long tagId=0;
    }

    public static void init() {
        siteQueueMap = new HashMap<>();
    }


    /**
     * @param anchorPositions 3个基站的坐标，格式为{{x1,y1}, {x2,y2}, {x3,y3}}，单位：厘米(cm)
     * @param tdoaData TDOA测量值，格式为{tdoa_BA, tdoa_CA, ...}，单位：纳秒(ns)
     * @param zHeight 标签高度（单位：cm，默认0）
     * @param c 光速（单位：m/s，默认299792458.0）
     * @return 标签的二维坐标{x, y}，单位：厘米(cm)
     */
    public static double[] chanAlgorithm2d(double[][] anchorPositions, double[] tdoaData, double zHeight, double c) {
        // 输入校验（简化版，实际应用需更严格）
        if (anchorPositions.length != 3 || anchorPositions[0].length != 2) {
            throw new IllegalArgumentException("基站坐标格式错误，需为3个二维坐标");
        }
        if (tdoaData.length < 2) {
            throw new IllegalArgumentException("TDOA数据至少需要2个测量值");
        }

        // 1. 单位转换：厘米(cm) → 米(m)，纳秒(ns) → 秒(s)
        double[][] anchorsM = new double[3][2];
        for (int i = 0; i < 3; i++) {
            anchorsM[i][0] = anchorPositions[i][0] / 100.0;  // x坐标转换
            anchorsM[i][1] = anchorPositions[i][1] / 100.0;  // y坐标转换
        }
        double[] tdoaS = new double[tdoaData.length];
        for (int i = 0; i < tdoaData.length; i++) {
            tdoaS[i] = tdoaData[i] * 1e-9;  // 纳秒转秒
        }

        // 2. 计算距离差（单位：米）
        double dBA = c * tdoaS[0];  // 标签到B与A的距离差
        double dCA = c * tdoaS[1];  // 标签到C与A的距离差

        // 3. 提取基站坐标（以A为参考点）
        double[] A = anchorsM[0];  // 基站A坐标 (xA, yA)
        double[] B = anchorsM[1];  // 基站B坐标 (xB, yB)
        double[] C = anchorsM[2];  // 基站C坐标 (xC, yC)

        // 4. 构建线性方程组 Ax = b（2x2矩阵）
        // 矩阵A：[[2*(xB-xA), 2*(yB-yA)], [2*(xC-xA), 2*(yC-yA)]]
        double a11 = 2 * (B[0] - A[0]);
        double a12 = 2 * (B[1] - A[1]);
        double a21 = 2 * (C[0] - A[0]);
        double a22 = 2 * (C[1] - A[1]);

        // 向量b：[(xB²+yB² - xA²-yA²) + dBA², (xC²+yC² - xA²-yA²) + dCA²]
        double b1 = (B[0]*B[0] + B[1]*B[1] - A[0]*A[0] - A[1]*A[1]) + dBA*dBA;
        double b2 = (C[0]*C[0] + C[1]*C[1] - A[0]*A[0] - A[1]*A[1]) + dCA*dCA;

        // 5. 求解线性方程组（x_m, y_m为米单位的坐标）
        double xM, yM;
        double det = a11 * a22 - a12 * a21;  // 行列式（判断矩阵是否可逆）
        final double EPS = 1e-9;  // 极小值，用于判断行列式是否接近0

        if (Math.abs(det) > EPS) {
            // 矩阵可逆：直接用克莱姆法则求解
            xM = (b1 * a22 - a12 * b2) / det;
            yM = (a11 * b2 - b1 * a21) / det;
        } else {
            // 矩阵接近奇异：用最小二乘法（伪逆求解）
            // 计算A^T * A 和 A^T * b
            double m11 = a11*a11 + a21*a21;
            double m12 = a11*a12 + a21*a22;
            double m21 = m12;
            double m22 = a12*a12 + a22*a22;
            double n1 = a11*b1 + a21*b2;
            double n2 = a12*b1 + a22*b2;

            // 求解(A^T A)x = (A^T b)
            double det2 = m11 * m22 - m12 * m21;
            if (Math.abs(det2) > EPS) {
                xM = (n1 * m22 - m12 * n2) / det2;
                yM = (m11 * n2 - n1 * m21) / det2;
            } else {
                // 极端情况：返回默认值（实际应用需根据场景调整）
                xM = 0;
                yM = 0;
            }
        }

        // 6. 转换为厘米(cm)
        double xCm = xM * 100.0;
        double yCm = yM * 100.0;

        // 7. 约束坐标在合理范围（0~3000cm）
        xCm = Math.max(0, Math.min(xCm, 3000));  // 等价于Python的clip
        yCm = Math.max(0, Math.min(yCm, 3000));

        return new double[]{xCm, yCm};
    }

    // 重载方法：默认高度0，默认光速
    public static double[] chanAlgorithm2d(double[][] anchorPositions, double[] tdoaData) {
        return chanAlgorithm2d(anchorPositions, tdoaData, 0, 299792458.0);
    }


    //返回double[]列表，double数组内容是单次定位的[x,y]，存放顺序也是这样，单位是厘米
    public static List<double[]> uwbTdoa(List<Input> inputs){
        List<double[]> resultList=new ArrayList<>();
        System.out.println(JSONObject.toJSONString(inputs));
        for(Input input:inputs){
            double[][] pos=new double[3][2];
            double[] tds=new double[3];
            for(int i=0;i<3;i++){
                pos[i][0]=input.xList.get(i);
                pos[i][1]=input.yList.get(i);
//                pos[i][2]=input.zList.get(i);
                tds[i]=input.tdoaData.get(i);
            }

            double[] result=chanAlgorithm2d(pos,tds);
            System.out.println(JSONObject.toJSONString(result));
            resultList.add(result);
        }
        System.out.println("*****************************************");
        System.out.println(JSONObject.toJSONString(resultList));
        return resultList;
    }


    public static void main(String[] args){
        System.out.println("TdoaApp launch!");
        init();
        // 示例：3个基站坐标（单位：cm）
        double[][] anchors = {{942, 1113}, {219, 1126}, {212, 2555}};
        // 示例：TDOA测量值（单位：ns）
        double[] tdoa = {6.745150, 39.093700,};  // 假设的时间差

        double[] result = chanAlgorithm2d(anchors, tdoa);
        System.out.println("定位结果（cm）：x=" + result[0] + ", y=" + result[1]);

    }
}

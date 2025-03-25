package org.ydxy.uwb.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Filtering {

    /**
     * 中值滤波函数
     *
     * @param data   输入数据列表
     * @param windowSize 窗口大小，必须是奇数
     * @return 滤波后的数据列表
     */
    public static ArrayList<Double> medianFilter(ArrayList<Double> data, int windowSize) {
        ArrayList<Double> result = new ArrayList<>();
        if (windowSize % 2 == 0) {
            throw new IllegalArgumentException("窗口大小必须是奇数");
        }
        int halfWindow = windowSize / 2;
        for (int i = 0; i < data.size(); i++) {
            int start = Math.max(0, i - halfWindow);
            int end = Math.min(data.size(), i + halfWindow + 1);
            List<Double> window = new ArrayList<>(data.subList(start, end));
            Collections.sort(window);
            int medianIndex = (window.size() - 1) / 2;
            result.add(window.get(medianIndex));
        }
        return result;
    }

    /**
     * 均值滤波函数
     *
     * @param data   输入数据列表
     * @param windowSize 窗口大小
     * @return 滤波后的数据列表
     */
    public static ArrayList<Double> meanFilter(ArrayList<Double> data, int windowSize) {
        ArrayList<Double> result = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            int start = Math.max(0, i - windowSize / 2);
            int end = Math.min(data.size(), i + windowSize / 2 + 1);
            double sum = 0;
            int count = 0;
            for (int j = start; j < end; j++) {
                sum += data.get(j);
                count++;
            }
            result.add(sum / count);
        }
        return result;
    }

    public static void main(String[] args) {
        ArrayList<Double> xl = new ArrayList<>();
        xl.add(1.0);
        xl.add(2.0);
        xl.add(3.0);
        xl.add(4.0);
        xl.add(5.0);

        int windowSize = 3;

        ArrayList<Double> medianFiltered = medianFilter(xl, windowSize);
        ArrayList<Double> meanFiltered = meanFilter(xl, windowSize);

        System.out.println("中值滤波结果: " + medianFiltered);
        System.out.println("均值滤波结果: " + meanFiltered);
    }
}
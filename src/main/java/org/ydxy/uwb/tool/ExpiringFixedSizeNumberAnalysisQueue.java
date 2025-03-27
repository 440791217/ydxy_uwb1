package org.ydxy.uwb.tool;

import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class ExpiringFixedSizeNumberAnalysisQueue<E extends Number> {
    private final Queue<ExpiringValue> queue;
    private final int maxSize;
    /**
     * 过期时间，单位为毫秒，默认为2秒
     */
    private final int EXPIRE_MILLISECONDS = 10000;

    public ExpiringFixedSizeNumberAnalysisQueue(int maxSize) {
        this.maxSize = maxSize;
        this.queue = new LinkedList<>();
    }

    /**
     * 向队列中添加元素，如果队列已满，移除队首元素
     * @param element 要添加的元素
     */
    public void add(E element) {
        // 清理过期元素
        cleanUp();
        if (queue.size() >= maxSize) {
            queue.poll();
        }
        ExpiringValue<Number> wrapElement = ExpiringValue.builder()
                .value(element)
                .expirationTime(System.currentTimeMillis())
                .build();
        queue.add(wrapElement);
    }

    private void cleanUp() {
        long currentTime = System.currentTimeMillis();
        queue.removeIf(entry ->currentTime - entry.getExpirationTime() > EXPIRE_MILLISECONDS);
    }

    /**
     * 获取队列中的元素
     * @return 包含队列元素的数组
     */
    public Object[] toArray() {
        return queue.toArray();
    }

    /**
     * 对队列中的元素进行均值滤波
     * @return 滤波后的均值
     */
    public double meanFilter() {
        if (queue.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (ExpiringValue element : queue) {
            sum += element.getValue().doubleValue();
        }
        return sum / queue.size();
    }

    /**
     * 对队列中的元素进行中值滤波
     * @return 滤波后的中值
     */
    public double medianFilter() {
        if (queue.isEmpty()) {
            return 0;
        }
        double[] array = new double[queue.size()];
        int index = 0;
        for (ExpiringValue element : queue) {
            array[index++] = element.getValue().doubleValue();
        }
        Arrays.sort(array);
        int middle = array.length / 2;
        if (array.length % 2 == 1) {
            return array[middle];
        } else {
            return (array[middle - 1] + array[middle]) / 2.0;
        }
    }

    public static void main(String[] args) {
        ExpiringFixedSizeNumberAnalysisQueue<Double> fixedSizeQueue = new ExpiringFixedSizeNumberAnalysisQueue<>(10);

        // 模拟添加数据
        for (int i = 0; i < 15; i++) {
            fixedSizeQueue.add(i+0.2);
            System.out.println("添加元素 " + i + " 后，队列中的元素: " + Arrays.toString(fixedSizeQueue.toArray()));
            System.out.println("均值滤波结果: " + fixedSizeQueue.meanFilter());
            System.out.println("中值滤波结果: " + fixedSizeQueue.medianFilter());
        }
    }

    public Integer getSize(){
        return queue.size();
    }

    @Data
    @Builder
    static class ExpiringValue<E extends Number> {
        private E value;
        private Long expirationTime;
    }
}
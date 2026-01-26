package org.ydxy.uwb.tool;

import lombok.Builder;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

public class ExpiringFixedSizeQueue<T> {
    private final Queue<ExpiringValue<T>> queue;
    private final int maxSize;
    private final Long expireMillis;
    private boolean isFull = false;

    public ExpiringFixedSizeQueue(int maxSize, Long expireMillis) {
        this.maxSize = maxSize;
        this.expireMillis = expireMillis;
        this.queue = new LinkedList<>();
    }

    /**
     * 向队列中添加元素，如果队列已满，移除队首元素
     *
     * @param element 要添加的元素
     */
    public void add(T element, Long ts) {
        while (queue.size() >= maxSize || (queue.peek() != null && ((ts - queue.peek().ts) > this.expireMillis))) {
            queue.poll();
        }
        ExpiringValue<T> value = ExpiringValue.<T>builder().value(element).ts(ts).build();
        queue.add(value);
    }

    /**
     * 获取队列中的元素
     *
     * @return 包含队列元素的数组
     */
    public Object[] toArray() {
        return queue.toArray();
    }

    public int getSize() {
        return queue.size();
    }

    public List<T> toList() {
        return queue.stream().map(ExpiringValue<T>::getValue).collect(Collectors.toList());
    }

    @Data
    @Builder
    static class ExpiringValue<T> {
        private T value;
        private Long ts;
    }

    public boolean isFull() {
        return isFull;
    }

    public T peek() {
        return queue.peek() == null ? null : queue.peek().getValue();
    }
}
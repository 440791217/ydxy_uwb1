package org.ydxy.uwb.tool;

import java.util.ArrayList;
import java.util.List;

public class UwbComb {
    public List<List<Integer>> combine(int n, int k) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(result, new ArrayList<>(), 1, n, k);
        return result;
    }

    private void backtrack(List<List<Integer>> result, List<Integer> tempList, int start, int n, int k) {
        if (tempList.size() == k) {
            result.add(new ArrayList<>(tempList));
        } else {
            for (int i = start; i <= n; i++) { // 从start开始可以避免重复和提前终止，优化性能
                tempList.add(i);
                backtrack(result, tempList, i + 1, n, k); // 注意这里是从i+1开始，避免重复选取同一个数字
                tempList.remove(tempList.size() - 1); // 回溯
            }
        }
    }

    public static void main(String[] args) {
        UwbComb combinations = new UwbComb();
        int n = 6, k = 4; // 比如从1到4中选2个数字的所有组合方式
        List<List<Integer>> result = combinations.combine(n, k);
        for (List<Integer> list : result) {
            System.out.println(list);
        }
    }
}
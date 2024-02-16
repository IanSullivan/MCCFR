package com.mccfr.Utils;

import java.util.List;
import java.util.ArrayList;

public class Combinations {

    private static void helper(List<int[]> combinations, int data[], int start, int end, int index) {
        if (index == data.length) {
            int[] combination = data.clone();
            combinations.add(combination);
        } else if (start <= end) {
            data[index] = start;
            helper(combinations, data, start + 1, end, index + 1);
            helper(combinations, data, start + 1, end, index);
        }
    }
    public static List<int[]> generate(int n, int r) {
        List<int[]> combinations = new ArrayList<int[]>();
        helper(combinations, new int[r], 0, n-1, 0);
        return combinations;
    }
}
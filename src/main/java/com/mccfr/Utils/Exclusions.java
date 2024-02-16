package com.mccfr.Utils;

import java.util.*;

public class Exclusions {
    public static Map<Integer, List<int[]>> getExclusions(){

        List<int[]> pocketCombos = Combinations.generate(52, 2);
        Map<Integer, List<int[]>> exclusions = new HashMap<>();

        for (int[] pocketCombo : pocketCombos) {
            for (int j : pocketCombo) {
                if (exclusions.containsKey(j)) {
                    exclusions.get(j).add(pocketCombo);
                } else {
                    List<int[]> a = new ArrayList<>();
                    a.add(pocketCombo);
                    exclusions.put(j, a);
                }
            }
        }
        return exclusions;
    }

    public static boolean contains(final int[] arr, int[] arr2) {
        for (int j = 0; j < arr2.length; j++) {
            int key = arr2[j];
            if(Arrays.stream(arr).anyMatch(i -> i == key)){
                System.out.println(key);
                return true;
            }
        }
        return false;
    }


    public static void main(String[] args) {
        int[] a = new int[]{1, 2, 3, 4};
        int[] b = new int[]{4};
        System.out.println(Exclusions.contains(a, b));
//        Map<Integer, List<int[]>> getExclusions = getExclusions();
//        List<int[]> a = getExclusions.get(23);
//        for (int i = 0; i < a.size(); i++) {
//            System.out.println(Arrays.toString(a.get(i)));
//        }

    }
}

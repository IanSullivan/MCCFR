package com.mccfr.PokerUtils;

import org.apache.commons.math3.ml.clustering.DoublePoint;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Arrays;

public final class ArrayUtils {

    public static short[] addAction(short[] arr, short x){
        short[] newarr = new short[arr.length + 1];
        System.arraycopy(arr, 0, newarr, 0, arr.length);
        newarr[arr.length] = x;
        return newarr;
    }

    public static boolean contains(int[] arr, int k){
        return Arrays.stream(arr).anyMatch(i -> i == k);
    }

    public static int[] short2intArr(short[] arr){
        int[] nextArr = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            nextArr[i] = arr[i];
        }
        return nextArr;
    }

    public static String[] convertStringToArray(ArrayList<String> arrayList){
        String[] a = new String[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            a[i] = arrayList.get(i);
        }
        return a;
    }

    public static double[] convertDoubleToList(ArrayList<Double> arrayList){
        double[] a = new double[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            a[i] = arrayList.get(i);
        }
        return a;
    }

    public static ArrayList<Double> convertDoubleToArray(double[] array){
        ArrayList<Double> a = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            a.add(array[i]);
        }
        return a;
    }

    public static double[] doublePointToArray(DoublePoint out){
        double[] a = new double[out.getPoint().length];

        for (int i = 0; i < out.getPoint().length; i++) {
            a[i] = out.getPoint()[i];
        }
        return a;
    }

    public static int roundTo(int betSize, int roundTo){
        return (int) Math.floor((double)betSize/roundTo + 0.5) * roundTo;
    }

    public static float maxUtil(float[] util, float[] strategy){
        float max = Float.MIN_VALUE;
        int idx = 0;
        for (int i = 0; i < util.length; i++) {
            if(util[i] * strategy[i] > max){
                max = util[i] * strategy[i];
            }
        }
        return max;
    }

    public static int getMaxValue(boolean[] activePlayers, short[] playerStacks){
        int maxAmount = 0;
        for (int i = 0; i < activePlayers.length; i++) {
            if(activePlayers[i]){
                maxAmount += playerStacks[i];
            }
        }
        return maxAmount;
    }



    public static boolean allInPlayed(int[] array) {
        for (int num : array) {
            if (num == 0) {
                return false;
            }
        }
        return true;
    }

    public static double getValueOrMax(double value, double maxValue, double threshold) {
        if (maxValue - value <= threshold || value > maxValue) {
            return maxValue;
        }
        else{
            return value;
        }
    }
}

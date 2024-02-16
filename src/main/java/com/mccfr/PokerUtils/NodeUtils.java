package com.mccfr.PokerUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public final class NodeUtils {
    private static final Random random = new Random();

    public static float[] getStrategy(float[] regretSum) {
        float normalizingSum = 0;
        float[] strategy = new float[regretSum.length];
        for (int a = 0; a < regretSum.length; a++) {
//            regretSum[a] = Math.max(regretSum[a], 0);
            strategy[a] = Math.max(regretSum[a], 0);
            normalizingSum += strategy[a];
        }
        for (int a = 0; a < regretSum.length; a++) {
            if (normalizingSum > 0){
                strategy[a] /= normalizingSum;
            }
            else{
                strategy[a] = (float) 1.0 / (float) regretSum.length;
            }
        }
        return strategy;
    }

    public static int getAction(float[] strategy) {
        double r = random.nextDouble();
        int a = 0;
        double cumulativeProbability = 0;
        while (a < strategy.length-1) {
            cumulativeProbability += strategy[a];
            if (r < cumulativeProbability)
                break;
            a++;
        }
        return a;
    }

    public static float[] getAverageStrategy(float[] strategySum) {
        float[] average_strategy = new float[strategySum.length];
        for (int i = 0; i < strategySum.length; i++) {
            average_strategy[i] = strategySum[i];
        }
        float normalizingSum = 0f;
        for (int a = 0; a < strategySum.length; a++){
            normalizingSum += strategySum[a];
        }

        for (int a = 0; a < strategySum.length; a++) {
            if (normalizingSum > 0) {
                average_strategy[a] = strategySum[a] / normalizingSum;
            } else {
                average_strategy[a] = (float) 1.0 / strategySum.length;
            }
        }
        return average_strategy;
    }

    public static byte[] toByteArray(ArrayList<Byte> arrayList){
        byte[] byteArray = new byte[arrayList.size()];
        for (int i = 0; i < arrayList.size() ; i++) {
            byteArray[i] = arrayList.get(i);
        }
        return byteArray;
    }

    public static short[] toShortArray(ArrayList<Short> arrayList){
        short[] byteArray = new short[arrayList.size()];
        for (int i = 0; i < arrayList.size() ; i++) {
            byteArray[i] = arrayList.get(i);
        }
        return byteArray;
    }

    public static void main(String[] args) {
        System.out.println(Integer.MAX_VALUE);
        System.out.println(Math.min(2147483647, 310000));
        float[] strat = {32937.0f, -75090.0f, 41113.0f};
        System.out.println(Arrays.toString(NodeUtils.getStrategy(strat)));
    }
}
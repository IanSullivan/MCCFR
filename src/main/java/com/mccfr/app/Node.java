package com.mccfr.app;

public final class Node {
    public float[] regretSum;
    public float[] strategySum;
//    public int nVisits = 1;
//    float reach_pr_sum;

    public Node(int nActions) {
        regretSum = new float[nActions];
        strategySum = new float[nActions];
//        reach_pr_sum = 0;
    }
}
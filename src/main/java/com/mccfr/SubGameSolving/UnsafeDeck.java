package com.mccfr.SubGameSolving;

import java.util.*;

public final class UnsafeDeck {
    private final HashMap<Float, int[]> map = new HashMap<>();
    public final ArrayList<Float> allProbs = new ArrayList<>();
    private final Random random = new Random();
    private float total = 0;

    public void add(float weight, int[] object) {
        if (weight <= 0) return;
        while (allProbs.contains(weight)){
            weight += 0.000001;
        }
        total += weight;
        allProbs.add(weight);
        map.put(weight, object);
    }

    public int[] next() {
        float r = random.nextFloat() * (total);
        float cumulativeProbability = 0;
        int a = 0;
        while (cumulativeProbability < r) {
            cumulativeProbability += allProbs.get(a);
            if (r < cumulativeProbability)
                break;
            a++;
        }
        return map.get(allProbs.get(a));
    }

    public void clear() {
        allProbs.clear();
        map.clear();
        total = 0;
    }
}

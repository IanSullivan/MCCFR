package com.mccfr.Abstractions;


import java.util.Arrays;
import java.util.HashMap;

public class Lossless {
    public static int getSuit(int cardIdx){
        if (cardIdx < 13)
        return 0; else if (cardIdx < 26)
        return 1; else if (cardIdx < 39)
        return 2; else
        return 3;
    }

    public static int getRank(int cardIdx){
        return (cardIdx % 13) + 2;
    }

    private static void setupCounters(HashMap<Integer, Integer> counter) {
        counter.put(0, 0);
        counter.put(1, 0);
        counter.put(2, 0);
        counter.put(3, 0);
    }

    private static void countSuits(int[] cards, HashMap<Integer, Integer> counter) {
        int suitCount;
        for (int card : cards) {
            suitCount = counter.get(getSuit(card));
            suitCount++;
            counter.put(getSuit(card), suitCount);
        }
    }

    public static String Pocket(int[] pockets){
        int[] losslessPockets = new int[2];
        int suit1 = getSuit(pockets[0]);
        int suit2 = getSuit(pockets[1]);
        boolean suited = suit1 == suit2;
        for (int i = 0; i < pockets.length; i++) {
            if (suited){
                losslessPockets[i] = getRank(pockets[i]) * -1;}
            else{
                losslessPockets[i] = (getRank(pockets[i]));}
        }
        Arrays.sort(losslessPockets);
        return Arrays.toString(losslessPockets);
    }

     public static String Flop(int[] pockets, int[] community){
         HashMap<Integer, Integer> suitCounter = new HashMap<>();
         HashMap<Integer, Integer> pocketSuitCounter = new HashMap<>();
         setupCounters(pocketSuitCounter);
         setupCounters(suitCounter);
         community = Arrays.copyOfRange(community, 0, 3);
         countSuits(pockets, pocketSuitCounter);
         countSuits(community, suitCounter);
         int[] losslessPockets = new int[2];
         int[] losslessCommunity = new int[3];
         int myFlush = -1;
         for (int i = 0; i < pockets.length; i++) {
             if (suitCounter.get(getSuit(pockets[i])) + pocketSuitCounter.get(getSuit(pockets[i])) >= 3) {
                 losslessPockets[i] = getRank(pockets[i]) * -1;
                 myFlush = getSuit(pockets[i]);
             }
             else{
                 losslessPockets[i] = getRank(pockets[i]);
             }
         }
         for (int i = 0; i < 3; i++) {
             if(getSuit(community[i]) == myFlush){
                 losslessCommunity[i] = getRank(community[i])* -1;
             }
             else if(suitCounter.get(getSuit(community[i])) >= 2 && getSuit(community[i]) != myFlush){
                 losslessCommunity[i] = (100 + getRank(community[i])) * -1;
             }
             else{
                 losslessCommunity[i] = getRank(community[i]);
             }
         }
//         for (int i = 0; i < pockets.length; i++) {
//                   // unSuited
//             if (suitCounter.get(getSuit(pockets[i])) + pocketSuitCounter.get(getSuit(pockets[i])) < 4)
//                 losslessPockets[i] = getRank(pockets[i]);
//             else  //  suited
//                 losslessPockets[i] = getRank(pockets[i]) * -1;
//         }
//         for (int i = 0; i < 3; i++) {
//             if (suitCounter.get(getSuit(community[i])) < 2){
//                 losslessCommunity[i] = getRank(community[i]);}
//             else{
//                 losslessCommunity[i] = getRank(community[i]) * -1;}
//         }
         Arrays.sort(losslessPockets);
         Arrays.sort(losslessCommunity);
         return Arrays.toString(losslessPockets) + Arrays.toString(losslessCommunity);
     }
    public static String Turn(int[] pockets, int[] community) {
        HashMap<Integer, Integer> suitCounter = new HashMap<>();
        HashMap<Integer, Integer> pocketSuitCounter = new HashMap<>();
        setupCounters(pocketSuitCounter);
        setupCounters(suitCounter);
        community = Arrays.copyOfRange(community, 0, 4);
        countSuits(pockets, pocketSuitCounter);
        countSuits(community, suitCounter);
        int[] losslessPockets = new int[2];
        int[] losslessCommunity = new int[4];
        int myFlush = -1;
        for (int i = 0; i < pockets.length; i++) {
            if (suitCounter.get(getSuit(pockets[i])) + pocketSuitCounter.get(getSuit(pockets[i])) >= 4) {
                losslessPockets[i] = getRank(pockets[i]) * -1;
                myFlush = getSuit(pockets[i]);
            }
            else{
                losslessPockets[i] = getRank(pockets[i]);
            }
        }
        for (int i = 0; i < 4; i++) {
            if(suitCounter.get(getSuit(community[i])) >= 2 && getSuit(community[i]) == myFlush){
                losslessCommunity[i] = getRank(community[i])* -1;
            }
            else if(suitCounter.get(getSuit(community[i])) >= 2 && getSuit(community[i]) != myFlush){
                losslessCommunity[i] = (100 + getRank(community[i])) * -1;
            }
            else{
                losslessCommunity[i] = getRank(community[i]);
            }
        }
        Arrays.sort(losslessPockets);
        Arrays.sort(losslessCommunity);
        return Arrays.toString(losslessPockets) + Arrays.toString(losslessCommunity);
    }

    public static String River(int[] pockets, int[] community) {
        HashMap<Integer, Integer> suitCounter = new HashMap<>();
        HashMap<Integer, Integer> pocketSuitCounter = new HashMap<>();
        setupCounters(pocketSuitCounter);
        setupCounters(suitCounter);
        countSuits(pockets, pocketSuitCounter);
        countSuits(community, suitCounter);
        int[] losslessPockets = new int[pockets.length];
        int[] losslessCommunity = new int[5];
        for (int i = 0; i < pockets.length; i++) {
            if (suitCounter.get(getSuit(pockets[i])) + pocketSuitCounter.get(getSuit(pockets[i])) < 5)
                losslessPockets[i] = getRank(pockets[i]);
            else
                losslessPockets[i] = getRank(pockets[i]) * -1;
        }
        for (int i = 0; i < community.length; i++) {
            if (suitCounter.get(getSuit(community[i])) < 3)
                losslessCommunity[i] = getRank(community[i]);
            else
                losslessCommunity[i] = getRank(community[i]) * -1;
        }
        Arrays.sort(losslessPockets);
        Arrays.sort(losslessCommunity);
        new StringBuilder(Arrays.toString(losslessPockets) + Arrays.toString(losslessCommunity));
        return Arrays.toString(losslessPockets) + Arrays.toString(losslessCommunity);
    }

    public static void main(String[] args) {
        int[] pockets = new int[]{0, 11};
        int[] community = new int[]{43, 22, 23};
        Lossless lossless = new Lossless();
        String loss = lossless.Flop(pockets, community);
        String p = Lossless.Flop(pockets, community);
        System.out.println(p);
        System.out.println(loss);
    }
}

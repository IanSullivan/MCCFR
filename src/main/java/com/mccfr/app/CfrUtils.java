package com.mccfr.app;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mccfr.PokerUtils.ArrayUtils;
import com.mccfr.PokerUtils.GameUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CfrUtils {
    private static final int NUM_PLAYERS = CONSTANTS.NUM_PLAYERS;
    private static final int STARTING_STACK = CONSTANTS.GAME.STARTING_STACK;

    public static void updateStrategy(Map<String, float[]> regretSum) throws IOException {

        Map<String, float[]> score2;
        ObjectMapper mapper = new ObjectMapper();
        score2 = mapper.readValue(Paths.get("strategySum.txt").toFile(), HashMap.class);

        for (HashMap.Entry me : score2.entrySet()) {
//            float[] currentStrategy = NodeUtils.getStrategy(regretSum.get(me.getKey()));

//                System.out.println("Key: "+me.getKey() + " & Value: " + me.getValue());
            System.out.print(me.getKey());
            System.out.print(me.getValue());
        }
    }

    public static void nextHistory(StringBuilder historyBuilder, char state, boolean[] activePlayers, int pot, int[] playerStacks){
        historyBuilder.setLength(0);
        historyBuilder.append(state);
        historyBuilder.append(" ");
        historyBuilder.append(GameUtils.numberOfPlayersLefts(activePlayers));
        historyBuilder.append(" ");
        if(state == 'f'){
            historyBuilder.append(pot);
//            if(pot < 50){
//                historyBuilder.append(ArrayUtils.roundTo(pot, 10));}
//            else{
//                historyBuilder.append(ArrayUtils.roundTo(pot, 25));}
        }
        else if(state == 't'){
            if(pot < 50){
                historyBuilder.append(ArrayUtils.roundTo(pot, 10));}
            else if(pot < 100){
                historyBuilder.append(ArrayUtils.roundTo(pot, 25));}
            else{
                historyBuilder.append(ArrayUtils.roundTo(pot, 50));}
        }
        else{
            if(pot < 50){
                historyBuilder.append(ArrayUtils.roundTo(pot, 10));}
            else if(pot < 100){
                historyBuilder.append(ArrayUtils.roundTo(pot, 25));}
            else{
                historyBuilder.append(ArrayUtils.roundTo(pot, 50));}
        }
        historyBuilder.append(" ");
    }

    public static float setReward(int[] playerScores, boolean[] activePlayers, int updatePlayer, int[] playerStacks,
                                  int pot){
        ArrayList<Integer> winner = GameUtils.whoWon(playerScores, activePlayers);
        int moneyInPot = STARTING_STACK - playerStacks[updatePlayer];
        if(winner.contains(updatePlayer)){
            return splitPot(winner, playerStacks, pot);}
        else{
            return -1 * moneyInPot;
        }
    }
    public static float setRewardFlop(int[] playerScores, boolean[] activePlayers, int currentPlayer, int[] playerStacks,
                                  int pot){
        ArrayList<Integer> winner = GameUtils.whoWon(playerScores, activePlayers);
        int moneyInPot = STARTING_STACK - playerStacks[currentPlayer];
        if(winner.contains(currentPlayer)){return splitPot(winner, playerStacks, pot);}
        else{return -1 * moneyInPot;}
    }

    public static float splitPot(ArrayList<Integer> winners, int[] playerStacks, int pot){
        for (Integer winner : winners) {
            pot -= (STARTING_STACK - playerStacks[winner]);
        }
        return (float)pot / winners.size();
    }

    public static void giveReward(ArrayList<Integer> winningPlayer, int[] playerStacks, int pot, int[] totalScores, int[] idxs){
        for (int i = 0; i < totalScores.length; i++) {
            int moneyInPot = STARTING_STACK - playerStacks[idxs[i]];
            if(winningPlayer.contains(idxs[i])){
                totalScores[i] += splitPot(winningPlayer, playerStacks, pot);
            }
            else{
                totalScores[i] += -1 * moneyInPot;
            }
        }
    }

    private static float[] convert2Array(ArrayList<Double> a) {
        double [] arr = new double[a.size()];
        float [] float_arr = new float[a.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = a.get(i);
            float_arr[i] = (float) arr[i];
        }
        return float_arr;
    }
}
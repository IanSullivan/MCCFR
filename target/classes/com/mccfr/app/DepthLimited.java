package com.mccfr.app;


import com.mccfr.Games.GameState;
import com.mccfr.Games.Leduc;
import com.mccfr.Games.Kuhn;
import com.mccfr.PokerUtils.GameUtils;
import com.mccfr.PokerUtils.NodeUtils;

import java.util.Arrays;

public class DepthLimited extends Thread {
    private static final int NUM_PLAYERS = CONSTANTS.NUM_PLAYERS;
    private final Leduc game;
    private final CFRThread cfrThread;
    private int updatePlayer = 0;
    int iterations = 100000;
    Rollouts rollouts;

    public DepthLimited(CFRThread cfrThread, Leduc game){
        this.cfrThread = cfrThread;
        this.game = game;
        this.rollouts = new Rollouts(cfrThread, game);
        System.out.println("starting...");
    }

    public void startintState(String history, GameState gameState, float[][] allReaches, int iterations){
        this.iterations = iterations;
//        this.startingHistory = history;
        game.setStartingState(gameState, allReaches);
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        long endTime;
        float[] utils = new float[NUM_PLAYERS];
        GameState gameState;
        float expectedValue = 0;
        for (int i = 0; i < iterations; i++) {
            if (i == iterations/4){
                expectedValue = 0;
//                for (Map.Entry<String, float[]> entry : cfrThread.strategySum.entrySet()){
//                    cfrThread.strategySum.put(entry.getKey(), new float[entry.getValue().length]);
//                }
            }
            if (i % 1000 == 0){
                endTime = System.currentTimeMillis();
                System.out.println(i);
                System.out.println("That took " + (endTime - startTime) + " milliseconds");
                startTime = System.currentTimeMillis();
            }
            for (int j = 0; j < NUM_PLAYERS; j++) {
                gameState = game.resetGame();
                updatePlayer = j;
                float out = walkTree(Leduc.STARTING_HISTORY, gameState);
                expectedValue += updatePlayer == 0 ? out: -1 * out;
            }
        }
        Databases.writeBlueprint(cfrThread);
    }

    private float walkTree(String history, GameState gameState){
        if(game.isTerminal(history, gameState)){
            return game.getReward(gameState, history, updatePlayer);
        }
        else if (game.isChance(history, gameState)){
            GameState newGameState = new GameState(gameState);
            game.applyChance(newGameState, (short)0);
            newGameState.activePlayersRoundStart = newGameState.activePlayers.clone();
            String nextHistory = game.nextChanceHistory(newGameState, history);
            newGameState.currentPlayer = -1;
            return depthUtil(newGameState, nextHistory);
        }
        gameState.currentPlayer = GameUtils.nextPlayer(gameState.activePlayers, gameState.currentPlayer);
        short[] actions = game.legalActions(gameState, history);
        String[] nextHistories = game.nextHistories(gameState, history);
        int nActions = actions.length;
        String key = gameState.cardMap.get(gameState.currentPlayer) + " " + history;
//        cfrThread.actionNames.put(key, actionNames);
        if(cfrThread.regretSum.get(key) == null){
            cfrThread.regretSum.computeIfAbsent(key, k -> new float[nActions]);
        }
        if(!cfrThread.actionNames.containsKey(key)){
            String[] actionNames = new String[actions.length];
            for (int i = 0; i < actions.length; i++) {
                actionNames[i] = Leduc.betAmount2String.get(actions[i]);
            }
            cfrThread.actionNames.put(key, actionNames);
        }
        float[] regretSum = cfrThread.regretSum.get(key);
        float[] strategy = NodeUtils.getStrategy(regretSum);
        float nodeUtil = 0;
        if (updatePlayer == gameState.currentPlayer) {
            float[] actionUtils = new float[actions.length];
            boolean[] explored = new boolean[actions.length];
            for (int a = 0; a < actions.length; a++) {
//                if(regretSum[a] > -150000){
                GameState newGameState = new GameState(gameState);
                game.applyAction(newGameState, actions[a], gameState.currentPlayer);
                actionUtils[a] = walkTree(nextHistories[a], newGameState);
                nodeUtil += (strategy[a] * actionUtils[a]);
                explored[a] = true;
//                }
            }
            for (int i = 0; i < actions.length; i++) {
                if(explored[i]){
                    regretSum[i] += (actionUtils[i] - nodeUtil);
                }
            }
        }
        else {
            if(cfrThread.strategySum.get(key) == null){
                cfrThread.strategySum.computeIfAbsent(key, k -> new float[nActions]);
            }
            float[] strategySum = cfrThread.strategySum.get(key);
            for (int i = 0; i < nActions; i++) {
                strategySum[i] += strategy[i];
            }
            int action_idx = NodeUtils.getAction(strategy);
            GameState newGameState = new GameState(gameState);
            game.applyAction(newGameState, actions[action_idx], gameState.currentPlayer);
            nodeUtil = walkTree(nextHistories[action_idx], newGameState);
        }
        return nodeUtil;
    }
    private float depthUtil(GameState newGameState, String nextHistory){
        rollouts.updatePlayer = updatePlayer;
        String key = newGameState.commuintyCards[0] + nextHistory;
        float[] depth_utils = new float[3];
        float depthUtil = 0;
        if(!cfrThread.depthRegrets.containsKey(key)) {
            cfrThread.depthRegrets.put(key, new float[]{0.33f, 0.33f, 0.33f});
        }
        float[] depth_regrets = cfrThread.depthRegrets.get(key);
        float[] depth_strategy = NodeUtils.getStrategy(depth_regrets);
        for (int i = 0; i < 3; i++) {
            GameState nextGameState = new GameState(newGameState);
            depth_utils[i] = rollouts.walkTree(nextHistory, nextGameState, i);
            depthUtil += (depth_strategy[i] * depth_utils[i]);
        }
        for (int i = 0; i < 3; i++) {
            depth_regrets[i] += (depth_utils[i] - depthUtil);
        }
        return depthUtil;
    }
}

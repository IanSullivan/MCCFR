package com.mccfr.app;


import com.mccfr.Games.GameState;
import com.mccfr.Games.Leduc;
import com.mccfr.Games.Kuhn;
import com.mccfr.PokerUtils.GameUtils;
import com.mccfr.PokerUtils.NodeUtils;

import java.util.Arrays;
import java.util.Map;

public class Rollouts extends Thread {
    private static final int NUM_PLAYERS = CONSTANTS.NUM_PLAYERS;
    protected int updatePlayer = 0;
    int iterations = 100000;
    CFRThread cfrThread;
    Leduc game;

    public Rollouts(CFRThread cfrThread, Leduc game){
        this.cfrThread = cfrThread;
        this.game = game;
//        System.out.println("starting...");
    }

    protected float walkTree(String history, GameState gameState, int bias_index){
        if(game.isTerminal(history, gameState)){
            return game.getReward(gameState, history, updatePlayer);
        }
        else if (game.isChance(history, gameState)){
            GameState newGameState = new GameState(gameState);
            game.applyChance(newGameState, (short)0);
            newGameState.activePlayersRoundStart = newGameState.activePlayers.clone();
            String nextHistory = game.nextChanceHistory(newGameState, history);
            newGameState.currentPlayer = -1;
            return walkTree(nextHistory, newGameState, bias_index);
        }
        gameState.currentPlayer = GameUtils.nextPlayer(gameState.activePlayers, gameState.currentPlayer);
        short[] actions = game.legalActions(gameState, history);
        String[] nextHistories = game.nextHistories(gameState, history);
        String key = gameState.cardMap.get(gameState.currentPlayer) + " " + history;
        float[] strategy = cfrThread.blueprint.get(key);
//        System.out.println(key);
        int action_idx = NodeUtils.getAction(strategy);
//        if (gameState.currentPlayer != updatePlayer){
//            float[] bias_strategy = bias_strategy(strategy.clone(), actions, bias_index);
//            action_idx = NodeUtils.getAction(bias_strategy);
//        }
        GameState newGameState = new GameState(gameState);
        game.applyAction(newGameState, actions[action_idx], gameState.currentPlayer);
        return walkTree(nextHistories[action_idx], newGameState, bias_index);
    }

    private float[] bias_strategy(float[] strategy, short[] actions, int bias_index){
        if (bias_index == 0){
            return strategy;
        }
        else if (bias_index == 1){
            strategy[0] *= 10;
            return NodeUtils.getStrategy(strategy);
        }
        else if (bias_index == 2){
            for (int i = 0; i < strategy.length; i++) {
                if (actions[i] > 0){
                    strategy[i] *= 10;
                }
            }
            return NodeUtils.getStrategy(strategy);
        }
        return strategy;
    }


}

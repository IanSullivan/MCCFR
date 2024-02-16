package com.mccfr.Games;

import java.util.HashMap;
import java.util.List;

public abstract class Game {
    public static final List<int[]> allPockets = null;
    public float[][] allReaches;
    public int STARTING_STACK;
    public String STARTING_HISTORY;
//    public HashMap<Short, String> betAmount2String;

    public abstract void resetGameState();
    public abstract void setStartingState(GameState gameState, float[][] allReaches);
    public abstract boolean isTerminal(String history, GameState gameState);
    public abstract boolean isChance(String history, GameState gameState);
    public abstract float getReward(GameState gameState, String history, int currentPlayer);
    public abstract void applyAction(GameState gameState, int actionValue, int player);
    public abstract void applyChance(GameState gameState, short action);
    public abstract String nextChanceHistory(GameState gameState, String history);
    public abstract String[] nextHistories(GameState gameState, String history);
    public abstract short[] chanceActions(GameState gameState, String history);
    public abstract short[] legalActions(GameState gameState, String history);
    public abstract GameState resetGame();
}

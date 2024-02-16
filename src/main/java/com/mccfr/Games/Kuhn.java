package com.mccfr.Games;

import com.mccfr.PokerUtils.StringUtils;
import com.mccfr.app.CONSTANTS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class Kuhn extends Game{

    public static final int STARTING_STACK = 2;
    public static final String STARTING_HISTORY = "";
    private static HashMap<Short, String> betAmount2String  = new HashMap<Short, String>() {{
        put((short) 0, "f");
        put((short) 1, "c");
    }};

    public float[] getRewardVector(String history, float[] pr2, int traverser){
        char[] historyArr = history.toCharArray();
        boolean checkedOff = historyArr[history.length()-1] == 'f' && historyArr[history.length()-2] == 'f';
        boolean fold = historyArr[history.length()-2] == 'c' && historyArr[history.length()-1] == 'f';
        boolean doubleBet = historyArr[history.length()-1] == 'c' && historyArr[history.length()-2] == 'c';
        float[] rewards = new float[]{0, 0, 0};
        for (int playerCard = 0; playerCard < 3; playerCard++) {
            for (int opponentCard = 0; opponentCard < 3; opponentCard++) {
                if (opponentCard == playerCard){
                    continue;}
                if(checkedOff){
                    float reward = playerCard > opponentCard ? 1 : -1;
                    rewards[playerCard] += reward * pr2[opponentCard];}
                else if(doubleBet){
                    float reward = playerCard > opponentCard ? 2 :-2;
                    rewards[playerCard] += reward * pr2[opponentCard];}
                else if(fold){
                    int foldedPlayer = who_folded(history);
                    if(foldedPlayer == traverser){
                        rewards[playerCard] += -1 * pr2[opponentCard];}
                    else{
                        rewards[playerCard] += pr2[opponentCard];}
                }
                else{
                    System.out.println("er");
                }
            }
        }
        return rewards;
    }

    private int who_folded(String history){
        // pbp
        if (history.length() == 3) {
            return 0;
        }
        // bp
        else if (history.length() == 2){
            return 1;
        }
        else{
            return 0;
        }
    }

    @Override
    public void resetGameState() {

    }

    @Override
    public void setStartingState(GameState gameState, float[][] allReaches) {

    }

    @Override
    public boolean isTerminal(String history, GameState gameState) {
        char[] historyArr = history.toCharArray();
        if (history.length() > 1){
            boolean checkedOff = historyArr[history.length()-1] == 'f' && historyArr[history.length()-2] == 'f';
            boolean fold = historyArr[history.length()-2] == 'c' && historyArr[history.length()-1] == 'f';
            boolean doubleBet = historyArr[history.length()-1] == 'c' && historyArr[history.length()-2] == 'c';
            return checkedOff || fold || doubleBet;
        }
        return false;
    }

    @Override
    public boolean isChance(String history, GameState gameState) {
        return false;
    }

    @Override
    public float getReward(GameState gameState, String history, int currentPlayer) {
        char[] historyArr = history.toCharArray();
        int player_card = gameState.playerCards[currentPlayer][0];
        int opponent_card = gameState.playerCards[(currentPlayer + 1) % 2][0];
        boolean terminal_pass = historyArr[history.length()-1] == 'f' && history.length() > 1;
        boolean double_bet = historyArr[history.length()-1] == 'c' && historyArr[history.length()-2] == 'c';
        if (terminal_pass){
            if(historyArr[history.length()-1] == 'f' && historyArr[history.length()-2] == 'f'){
                return player_card > opponent_card ? 1: -1;
            }
            else{
                return currentPlayer == gameState.currentPlayer ? -1: 1;
            }
        }
        else if (double_bet){
            return player_card > opponent_card ? 2: -2;
        }
        return 0;
    }

    @Override
    public void applyAction(GameState gameState, int actionValue, int player) {
//        int lastPlayer = RoundUtils.lastActionPlayer(gameState.lastActionValues, gameState.activePlayers, player);
//        int betAmount = actionValue + Math.abs(gameState.lastActionValues[lastPlayer] - gameState.lastActionValues[player]);
//        if(betAmount > gameState.playerStacks[player]){
//            betAmount = gameState.playerStacks[player];
//        }
//        if (actionValue != -1){  // fold
//            gameState.pot += betAmount;
//            gameState.playerStacks[player] -= betAmount;
//            gameState.lastActionValues[player] += betAmount;
//        }
//        else{
//            gameState.activePlayers[player] = false;
//        }
    }

    @Override
    public void applyChance(GameState gameState, short action) {

    }

    @Override
    public String nextChanceHistory(GameState gameState, String history) {
        return null;
    }

    @Override
    public String[] nextHistories(GameState gameState, String history) {
        short[] actions = legalActions(gameState, history);
        String[] allHistories = new String[actions.length];
        for (int i = 0; i < actions.length; i++) {
            allHistories[i] = history + betAmount2String.get(actions[i]);
        }
        return allHistories;
    }

    @Override
    public short[] chanceActions(GameState gameState, String history) {
        return new short[0];
    }

    @Override
    public short[] legalActions(GameState gameState, String history) {
        return new short[]{0, 1};

    }

    public GameState resetGame() {
        ArrayList<Integer> deck = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            deck.add(i);
        }
        Collections.shuffle(deck);
        HashMap<Integer, String> cardMap = new HashMap<>();
        int[][] pocketCards = new int[CONSTANTS.NUM_PLAYERS][2];
        for (int i = 0; i < pocketCards.length; i++) {
            pocketCards[i][0] = deck.get(0);
            deck.remove(0);
            cardMap.put(i, String.valueOf(pocketCards[i][0]));
        }
        int[] playerStacks = new int[CONSTANTS.NUM_PLAYERS];
        int[] lastActionValues = new int[CONSTANTS.NUM_PLAYERS];
        boolean[] activePlayers = new boolean[CONSTANTS.NUM_PLAYERS];
        Arrays.fill(activePlayers, true);
        Arrays.fill(playerStacks, 1);
        int pot = 2;
        char betingRound = 'r';
        return new GameState(-1, playerStacks, activePlayers, lastActionValues, pot, betingRound, pocketCards, deck, cardMap);
    }

    public static void main(String[] args) {
        Kuhn kuhn = new Kuhn();
        String history = "fcc";
        float[] pr2 = new float[]{0.f, 0.39445601f, 0.34968377f};
        int travers = 1;
        System.out.println(Arrays.toString(kuhn.getRewardVector(history, pr2, travers)));
//        System.out.println(kuhn.getReward(1, 0, "cf"));
    }
}

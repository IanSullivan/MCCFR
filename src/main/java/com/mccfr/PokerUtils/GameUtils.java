package com.mccfr.PokerUtils;

import com.mccfr.Evaluation.Evaluate;
import com.mccfr.Games.GameState;
import com.mccfr.app.CONSTANTS;

import java.util.ArrayList;

public final class GameUtils {
    /**
     * @param playerRewards the value of each players hand
     * @param activePlayers boolean array of who currently in the game
     * @return the index of the player with the best hand
     */
    public static ArrayList<Integer> whoWon(int[] playerRewards, boolean[] activePlayers) {
        int minValue = Integer.MAX_VALUE;
        ArrayList<Integer> winners = new ArrayList<>();
        for (int i = 0; i < playerRewards.length; i++) {
            if(playerRewards[i] < minValue && activePlayers[i]){
                minValue = playerRewards[i];
                winners.clear();
                winners.add(i);
            }
            else if(playerRewards[i] == minValue && activePlayers[i]){
                winners.add(i);
            }
        }
        return winners;
    }


    public static int[] playerScores(int[][] pocketCards, int[] communityCards){
        int[] scores = new int[pocketCards.length];
        for (int i = 0; i < pocketCards.length; i++) {
            scores[i] = Evaluate.handScore(pocketCards[i], communityCards);
        }
        return scores;
    }
    public static int[] playerScoresFlop(int[][] pocketCards, int[] communityCards){
        int[] scores = new int[pocketCards.length];
        for (int i = 0; i < pocketCards.length; i++) {
            scores[i] = Evaluate.flopScore(pocketCards[i], communityCards);
        }
        return scores;
    }

    public static int numberOfPlayersLefts(boolean[] activePlayers){
        int numPlayersLeft = 0;
        for (boolean activePlayer: activePlayers) {
            if (activePlayer) {
                numPlayersLeft++;
            }
        }
        return numPlayersLeft;
    }

    public static int nextPlayer(boolean[] activePlayers, int startingPlayer){
        int player = startingPlayer;
        player++;
        player = player % CONSTANTS.NUM_PLAYERS;
        while (!activePlayers[player]){
            player++;
            player = player % CONSTANTS.NUM_PLAYERS;
        }
        return player;
    }

    public static int getStartingPlayer(String history, GameState gameState){
        int player = 0;
        String[] actionStrings = StringUtils.getActionStrings(history);
        for (int i = 0; i < actionStrings.length-1; i++) {
            player = GameUtils.nextPlayer(gameState.activePlayers, player);
        }
        return player;
    }
}

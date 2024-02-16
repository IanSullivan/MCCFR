package com.mccfr.PokerUtils;

import com.mccfr.Games.GameState;
import com.mccfr.app.CONSTANTS;

public final class RoundUtils {
    private static final int NUM_PLAYERS = CONSTANTS.NUM_PLAYERS;
    /**
     * Determins if the betting round is done.
     * @param history_split an array of all the actions taken during the round
     * @param activePlayers an array of all players playing at the start of the round, true means they are still in the game
     * Round is done when the betting goes back to last aggressor or the player who raised last
     * fold action removes a player from play
     * @return true if the betting round is done, false if the round should continue
     */

    public static boolean isRoundDone(String[] history_split, boolean[] activePlayers, int player){
        if(history_split.length > 1){
            player = nextPlayer(player, activePlayers);
            int aggressorIndex = player;
            player = lastPlayer(player);
            for (int i = 0; i < history_split.length; i++) {
                String actionTaken = history_split[i];
                player = nextPlayer(player, activePlayers);
                //  find next player playing
                if (actionTaken.charAt(0) == 'r') {
                    aggressorIndex = player;
                }
                else if (actionTaken.charAt(0) == 'f') {
                    activePlayers[player] = false;
                }
            }
            player = nextPlayer(player, activePlayers);
            return aggressorIndex == player;
        }
        return false;
    }


    /**
     * Determines how many players are still playing in the game
     * @param activePlayers array represent who is in the hand; 1 still playing, 0 folded
     * @return count; the number of 1's in the array
     */
    public static int numberOfPlayerLeft(boolean[] activePlayers){
        int count = 0;
        for (boolean activePlayer : activePlayers) {
            if (activePlayer) {
                count++;
            }
        }
        return count;
    }

    /**
     * action value is the size of the chips a player puts into the pot ie; a raise of 10, check 0, call the value
     * of the raise being called
     * @param actionsTaken an array showing the action values previously taken by players
     * @return value of the last action
     */
    public static int lastActionPlayer(int[] actionsTaken, boolean[] activePlayers, int currentPlayer){
        //  get the last player to play
        int player = lastPlayer(currentPlayer);
        while (!activePlayers[player]){
            player = lastPlayer(player);
        }
        return player;
    }

    private static int lastPlayer(int player){
        player--;
        if(player < 0){
            player = NUM_PLAYERS-1;
        }
        return player;
    }

    private static int nextPlayer(int player, boolean[] activePlayers){
        player++;
        player = player % NUM_PLAYERS;
        while (!activePlayers[player]){
            player++;
            player = player % NUM_PLAYERS;
        }
        return player;
    }

    /**
     * Checks all the stacks of players currently playing in the hand
     * Used to check if the game is done, if all active players have 0 stacks no more betting is done
     * @param activePlayers array represent who is in the hand; 1 stil playing, 0 folded
     * @param playerStacks array of ints representing each players stack size
     * @return true if all activa players have empty stack,
     */
    public static boolean allStacksEmpty(boolean[] activePlayers, int[] playerStacks){
        boolean allEmpty = true;
        for (int i = 0; i < activePlayers.length; i++) {
            //  if any of the active players has a stack greater than 0, not all the stacks are empty
            if (activePlayers[i] && playerStacks[i] > 0) {
                allEmpty = false;
                break;
            }
        }
        return allEmpty;
    }



    public static boolean isGameDone(GameState gameState, boolean isRoundDone) {
        if (RoundUtils.numberOfPlayerLeft(gameState.activePlayers) <= 1) {
            return true;
        } else if (gameState.bettingRound == 'r' && isRoundDone) {
            return true;
        } else if(RoundUtils.allStacksEmpty(gameState.activePlayers, gameState.playerStacks)){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * @param state current state of the game ie:flop, turn
     * @return the betting round after the current state
     */
    public static char getNextState(char state) {
        switch (state){
            case 'p':
                return 'f';
            case 'f':
                return 't';
            case 't':
                return 'r';
            default:
                return 0;
        }
    }
}

package com.mccfr.Games;


import com.mccfr.app.CONSTANTS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class GameState{
    public ArrayList<Integer> deck;
    public int currentPlayer;
    public int[] playerStacks;
    public int[] lastActionValues;
    public int[] playerScores = new int[CONSTANTS.NUM_PLAYERS];
    public boolean[] activePlayers;
    public boolean[] activePlayersRoundStart;
    public int pot;
    public char bettingRound;
    public int[][] playerCards;
    public int[] commuintyCards = new int[]{-1, -1, -1, -1, -1};
    public HashMap<Integer, String> cardMap;
    public HashSet<Integer> cardsTaken = new HashSet<>();

    public GameState(int currentPlayer, int[] playerStacks,  boolean[] activePlayers, int[] lastActionValues, int pot, char bettingRound,
    int[][] playerCards, ArrayList<Integer> deck, HashMap<Integer, String> cardMap){
        this.currentPlayer = currentPlayer;
        this.lastActionValues = lastActionValues;
        this.activePlayers = activePlayers;
        this.activePlayersRoundStart = activePlayers;
        this.playerStacks = playerStacks;
        this.pot = pot;
        this.bettingRound = bettingRound;
        this.playerCards = playerCards;
        this.deck = deck;
        this.cardMap = cardMap;
    }

    public GameState(GameState gameState){
        this.currentPlayer = gameState.currentPlayer;
        this.playerStacks = gameState.playerStacks.clone();
        this.lastActionValues = gameState.lastActionValues.clone();
        this.activePlayers = gameState.activePlayers.clone();
        this.activePlayersRoundStart = gameState.activePlayersRoundStart.clone();
        this.playerScores = gameState.playerScores.clone();
        this.playerCards = gameState.playerCards.clone();
        this.commuintyCards = gameState.commuintyCards.clone();
        this.pot = gameState.pot;
        this.bettingRound = gameState.bettingRound;
        this.cardMap = new HashMap<>(gameState.cardMap);
        this.deck = new ArrayList<>(gameState.deck);
        this.cardsTaken = gameState.cardsTaken;
    }

    @Override
    public String toString(){
//        System.out.println(currentPlayer + " current");
        System.out.println(currentPlayer);
        System.out.println(Arrays.toString(playerScores));
        System.out.println(cardMap);
        System.out.println(pot);
        return "";
    }
}

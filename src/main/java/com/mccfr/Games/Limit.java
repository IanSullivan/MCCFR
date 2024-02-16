package com.mccfr.Games;

import com.mccfr.Abstractions.Buckets;
import com.mccfr.Abstractions.Lossless;
import com.mccfr.PokerUtils.*;
import com.mccfr.SubGameSolving.UnsafeDeck;
import com.mccfr.Utils.Combinations;
import com.mccfr.app.CONSTANTS;
import com.mccfr.app.CfrUtils;

import java.util.*;

public class Limit extends Game {
    private static final Random r = new Random();
    private static final int NUM_PlAYERS = CONSTANTS.NUM_PLAYERS;
    public Buckets buckets = new Buckets();
    public static final int STARTING_STACK = 200;
    public static final char STARTING_STATE = 'p';
    public static final String STARTING_HISTORY = STARTING_STATE + " " + NUM_PlAYERS + " 0 r1.r2.";
    private final UnsafeDeck[] allDecks = new UnsafeDeck[NUM_PlAYERS];
    private static final List<int[]> allPockets = Combinations.generate(52, 2);
    public float[][] allReaches = new float[CONSTANTS.NUM_PLAYERS][allPockets.size()];
    public int[] startingCommunityCards = new int[]{-1, -1, -1, -1, -1};
    public char startingBetingRound = 'p';
    int[] startingStacks = new int[CONSTANTS.NUM_PLAYERS];
    int[] lastActionValues = new int[CONSTANTS.NUM_PLAYERS];
    boolean[] activePlayers = new boolean[CONSTANTS.NUM_PLAYERS];

    HashMap<String, String[]> nextHistories = new HashMap<>();
    HashMap<String, String> nextChanceHistory = new HashMap<>();
    HashMap<String, short[]> legalActions = new HashMap<>();
    HashMap<String, Boolean> chanceHistories = new HashMap<>();
    HashMap<String, Boolean> terminalHistories = new HashMap<>();
    public int pot;
    boolean lossless = false;

    public static final HashMap<Short, String> betAmount2String  = new HashMap<Short, String>() {{
        put((short) -1, "f.");
        put((short) 0, "c.");
        put((short) 2, "r2.");
        put((short) 4, "r4.");
        put((short) 8, "r8.");
    }};

    public Limit(){
        resetGameState();
    }

    @Override
    public void resetGameState() {
        Arrays.fill(activePlayers, true);
        Arrays.fill(startingStacks, STARTING_STACK);
        lastActionValues[0] = 1;
        lastActionValues[1] = 2;
        startingStacks[0] -= 1;
        startingStacks[1] -= 2;
        pot = 3;
        for (int i = 0; i < CONSTANTS.NUM_PLAYERS; i++) {
            Arrays.fill(allReaches[i], 1);
        }
        for (int i = 0; i < allDecks.length; i++) {
            allDecks[i] = new UnsafeDeck();
            for (int j = 0; j < allPockets.size(); j++) {
                allDecks[i].add(allReaches[i][j], allPockets.get(j));
            }
        }
    }

    public void setStartingState(GameState gameState, float[][] allReaches) {
        this.allReaches = allReaches;
        lastActionValues = gameState.lastActionValues.clone();
        startingStacks = gameState.playerStacks.clone();
        activePlayers = gameState.activePlayers.clone();
        startingBetingRound = gameState.bettingRound;
        pot = gameState.pot;
        startingCommunityCards = gameState.commuintyCards.clone();
        for (int i = 0; i < allDecks.length; i++) {
            allDecks[i].clear();
            for (int j = 0; j < allPockets.size(); j++) {
                allDecks[i].add(allReaches[i][j], allPockets.get(j));
            }
        }
    }

    @Override
    public boolean isTerminal(String history, GameState gameState) {
        if(!terminalHistories.containsKey(history)){
            boolean isRoundDone = isChance(history, gameState);
            boolean isGameDone;
            isGameDone = RoundUtils.isGameDone(gameState, isRoundDone);
            terminalHistories.put(history, isGameDone);
            return isGameDone;
        }
        else{
            return terminalHistories.get(history);
        }
    }

    @Override
    public boolean isChance(String history, GameState gameState) {
        if(!chanceHistories.containsKey(history)){
            boolean isRoundDone;
            String[] history_split = StringUtils.getActionStrings(history);
            boolean[] newActivePlayers = gameState.activePlayersRoundStart.clone();
            if(gameState.bettingRound == 'p'){
                history_split = StringUtils.getActionStringsPre(history);
                isRoundDone = RoundUtils.isRoundDone(history_split, newActivePlayers, 1);
            }else{
                isRoundDone = RoundUtils.isRoundDone(history_split, newActivePlayers, -1);
            }
            chanceHistories.put(history, isRoundDone);
            return isRoundDone;
        }
        else{
            return chanceHistories.get(history);
        }
    }

    @Override
    public float getReward(GameState gameState, String history, int updatePlayer) {
        return CfrUtils.setReward(gameState.playerScores, gameState.activePlayers, updatePlayer,
            gameState.playerStacks, gameState.pot);
    }

    private int addRandomCard(GameState gameState){
        int p = r.nextInt(gameState.deck.size());
        while (ArrayUtils.contains(gameState.commuintyCards, gameState.deck.get(p))){
            p = r.nextInt(gameState.deck.size());
        }
        return gameState.deck.get(p);
    }

    private void fillCardMap(char bettingRound, int[][] playerCards, int[] commuintyCards,
                             HashMap<Integer, String> cardMap){
        for (int i = 0; i < CONSTANTS.NUM_PLAYERS; i++) {
            if(bettingRound == 'p'){
                cardMap.put(i, Lossless.Pocket(playerCards[i]));}
            else if(bettingRound == 'f'){
                String flopName = Lossless.Flop(playerCards[i], commuintyCards);
                if (lossless){
                    cardMap.put(i, flopName);}
                else{
                    cardMap.put(i, buckets.getFlopBucket(flopName));}
            }
            else if(bettingRound == 't'){
                String turnName = Lossless.Turn(playerCards[i], commuintyCards);
                if (lossless){
                    cardMap.put(i, turnName);}
                else{
                    cardMap.put(i, buckets.getTurnBucket(turnName));}
            }
            else if(bettingRound == 'r'){
                String riverName = Lossless.River(playerCards[i], commuintyCards);
                if (lossless){
                    cardMap.put(i, riverName);}
                else{
                    cardMap.put(i, buckets.getRiverBucket(riverName));}
            }
        }
    }

    @Override
    public void applyAction(GameState gameState, int actionValue, int player) {
        int lastPlayer = RoundUtils.lastActionPlayer(gameState.lastActionValues, gameState.activePlayers, player);
        int betAmount = actionValue + Math.abs(gameState.lastActionValues[lastPlayer] - gameState.lastActionValues[player]);
        if(betAmount > gameState.playerStacks[player]){
            betAmount = gameState.playerStacks[player];
        }
        if (actionValue != -1){  // fold
            gameState.pot += betAmount;
            gameState.playerStacks[player] -= betAmount;
            gameState.lastActionValues[player] += betAmount;
        }
        else{
            gameState.activePlayers[player] = false;
        }
    }

    @Override
    public void applyChance(GameState gameState, short action) {
        gameState.bettingRound = RoundUtils.getNextState(gameState.bettingRound);
        if (gameState.bettingRound == 'f'){
            for (int i = 0; i < 3; i++) {
                int nextCard = addRandomCard(gameState);
                gameState.commuintyCards[i] = nextCard;}
            fillCardMap(gameState.bettingRound, gameState.playerCards, gameState.commuintyCards, gameState.cardMap);
        } else if(gameState.bettingRound == 't'){
            gameState.commuintyCards[3] = addRandomCard(gameState);;
            fillCardMap(gameState.bettingRound, gameState.playerCards, gameState.commuintyCards, gameState.cardMap);
        } else if(gameState.bettingRound == 'r'){
            gameState.commuintyCards[4] = addRandomCard(gameState);
            fillCardMap(gameState.bettingRound, gameState.playerCards, gameState.commuintyCards, gameState.cardMap);
            gameState.playerScores = GameUtils.playerScores(gameState.playerCards, gameState.commuintyCards);
        }
        Arrays.fill(gameState.lastActionValues, 0);
    }

    @Override
    public String nextChanceHistory(GameState gameState, String history) {
        if(!nextHistories.containsKey(history)){
            StringBuilder historyBuilder = new StringBuilder();
            CfrUtils.nextHistory(historyBuilder, gameState.bettingRound, gameState.activePlayers,
                    gameState.pot, gameState.playerStacks);
            nextChanceHistory.put(history, historyBuilder.toString());
            return historyBuilder.toString();
        }
        else{
            return nextChanceHistory.get(history);
        }
    }

    @Override
    public String[] nextHistories(GameState gameState, String history) {
        if(!nextHistories.containsKey(history)) {
            short[] actions = legalActions(gameState, history);
            String[] allHistories = new String[actions.length];
            for (int i = 0; i < actions.length; i++) {
                allHistories[i] = history + betAmount2String.get(actions[i]);
            }
            nextHistories.put(history, allHistories);
            return allHistories;
        }
        else{
            return nextHistories.get(history);
        }
    }

    @Override
    public short[] chanceActions(GameState gameState, String history) {
        return new short[0];
    }

    @Override
    public short[] legalActions(GameState gameState, String history) {
        if(!legalActions.containsKey(history)){
            String[] history_split = StringUtils.getActionStrings(history);
            int numRaises = 0;
            if (!history_split[0].equals("")) {
                numRaises = StringUtils.countChars(history_split, 'r');
            }
            int betSize = gameState.bettingRound == 'p' || gameState.bettingRound == 'f' ? 2 : 4;
            ArrayList<Short> action_dict = new ArrayList<>();
            if (numRaises > 0) {
                action_dict.add((short) -1);
            }
            action_dict.add((short)0);
            // Checked or folded to BB
            if (gameState.bettingRound == 'p' && history_split.length == CONSTANTS.NUM_PLAYERS + 1 && numRaises == 2) {
                action_dict.remove(0);
            }
            if(gameState.bettingRound == 'p'){
                numRaises -= 2;
            }
            if (numRaises < 4){
                action_dict.add((short)betSize);
            }
            short[] actions = NodeUtils.toShortArray(action_dict);
            legalActions.put(history, actions);
            if(history.equals(STARTING_HISTORY)){
                System.out.println(Arrays.toString(actions));
            }
            return actions;
        }
        else{
            return legalActions.get(history);
        }
    }

    @Override
    public GameState resetGame() {
        ArrayList<Integer> deck = CardUtils.buildDeck();
        HashMap<Integer, String> cardMap = new HashMap<>();
        HashSet<Integer> cardsTaken = new HashSet<>();
        for (int startingCommunityCard : startingCommunityCards) {
            cardsTaken.add(startingCommunityCard);
            deck.remove(Integer.valueOf(startingCommunityCard));
        }
        int[][] pocketCards = new int[CONSTANTS.NUM_PLAYERS][2];
        for (int i = 0; i < pocketCards.length; i++) {
            int[] chosenPocket = allDecks[i].next();
            while(cardsTaken.contains(chosenPocket[0]) || cardsTaken.contains(chosenPocket[1])){
                chosenPocket = allDecks[i].next();
            }
            pocketCards[i] = chosenPocket;
            cardsTaken.add(chosenPocket[0]);
            cardsTaken.add(chosenPocket[1]);
            deck.remove(Integer.valueOf(chosenPocket[0]));
            deck.remove(Integer.valueOf(chosenPocket[1]));
        }
        lastActionValues[0] = 1;
        lastActionValues[1] = 2;
        fillCardMap(startingBetingRound, pocketCards, startingCommunityCards, cardMap);
        return new GameState(1, startingStacks, activePlayers, lastActionValues,
                pot, startingBetingRound, pocketCards, deck, cardMap);
    }

    public static void main(String[] args){
        float[] allReaches = new float[allPockets.size()];
        Arrays.fill(allReaches, 1);
        allReaches[0] = 1;
        allReaches[143] = 1;
        Limit game = new Limit();
        GameState gameState = game.resetGame();
        String history = STARTING_HISTORY + "c.r5.c.";
        System.out.println(history);
        System.out.println(game.isChance(history, gameState));
        game.resetGame();
    }
}

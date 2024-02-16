package com.mccfr.Games;

import com.mccfr.Abstractions.Buckets;
import com.mccfr.Abstractions.Lossless;
import com.mccfr.PokerUtils.*;
import com.mccfr.SubGameSolving.UnsafeDeck;
import com.mccfr.Utils.Combinations;
import com.mccfr.app.CONSTANTS;
import com.mccfr.app.CfrUtils;

import java.util.*;


public class NoLimit extends Game {
    private static final Random r = new Random();
    private static final int NUM_PLAYERS = 6;
    public static final char STARTING_STATE = 'p';
    HashMap<String, short[]> legalActions = new HashMap<>();
    public static final int STARTING_STACK = 200;
    public Buckets buckets = new Buckets();
    private static final List<int[]> allPockets = Combinations.generate(52, 2);
    public float[][] allReaches = new float[CONSTANTS.NUM_PLAYERS][allPockets.size()];
    private final UnsafeDeck[] allDecks = new UnsafeDeck[CONSTANTS.NUM_PLAYERS];
    int[] startingStacks = new int[CONSTANTS.NUM_PLAYERS];
    int[] lastActionValues = new int[CONSTANTS.NUM_PLAYERS];
    boolean[] activePlayers = new boolean[CONSTANTS.NUM_PLAYERS];
    int pot = 3;
    char startingBettingRound = 'p';
    public int[] startingCommunityCards = new int[]{-1, -1, -1, -1, -1};
    boolean lossless = false;

    public NoLimit(){
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

    @Override
    public void setStartingState(GameState gameState, float[][] allReaches) {
        this.allReaches = allReaches;
        lastActionValues = gameState.lastActionValues.clone();
        startingStacks = gameState.playerStacks.clone();
        activePlayers = gameState.activePlayers.clone();
        startingBettingRound = gameState.bettingRound;
        startingCommunityCards = gameState.commuintyCards.clone();
        pot = gameState.pot;
//        for (int i = 0; i < allDecks.length; i++) {
//            allDecks[i].clear();
//            for (int j = 0; j < allPockets.size(); j++) {
//                allDecks[i].add(this.allReaches[i][j], allPockets.get(j));}
//        }
    }

    public static String betAmountToString(GameState gameState, double betSize) {
        if (betSize == -1){
            return "f.";
        }else if(betSize == 0){
            return "c.";
        }
        else if(betSize == 6){
            return "r6.";
        }
        else if(betSize == 10){
            return "r10.";
        }
        else if(betSize == 16){
            return "r16.";
        }
        else if(betSize == 30){
            return "r30.";
        }
        else{
            double potSize = gameState.pot;
            double halfPot =  potSize / 2;
//            double doublePot = potSize * 2;
            double allStack = gameState.playerStacks[gameState.currentPlayer];

            Map<String, Double> differences = new HashMap<>();
            differences.put("r1/2p.", Math.abs(betSize - halfPot));
            differences.put("rp.", Math.abs(betSize - potSize));
//            differences.put("r2p.", Math.abs(betSize - doublePot));
            differences.put("rall.", Math.abs(betSize - allStack));

            String closestOption = "";
            double minDifference = Double.MAX_VALUE;
            for (Map.Entry<String, Double> entry : differences.entrySet()) {
                if (entry.getValue() < minDifference) {
                    minDifference = entry.getValue();
                    closestOption = entry.getKey();
                }
            }
            return closestOption;
        }
    }


    @Override
    public boolean isTerminal(String history, GameState gameState) {
        boolean isRoundDone = isChance(history, gameState);
        return RoundUtils.isGameDone(gameState, isRoundDone);
    }

    @Override
    public float getReward(GameState gameState, String history, int currentPlayer) {
        return CfrUtils.setReward(gameState.playerScores, gameState.activePlayers, currentPlayer,
                gameState.playerStacks, gameState.pot);
    }

    @Override
    public boolean isChance(String history, GameState gameState) {
        boolean isRoundDone;
        String[] history_split = StringUtils.getActionStrings(history);
        boolean[] newActivePlayers = gameState.activePlayersRoundStart.clone();
        if(gameState.bettingRound == STARTING_STATE){
            history_split = StringUtils.getActionStringsPre(history);
            isRoundDone = RoundUtils.isRoundDone(history_split, newActivePlayers, 1);
        }
        else{
            isRoundDone = RoundUtils.isRoundDone(history_split, newActivePlayers, -1);
        }
        return isRoundDone;
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
        if (gameState.bettingRound == 'f') {
            for (int i = 0; i < 3; i++) {
                int nextCard = CardUtils.addRandomCard(gameState);
                gameState.commuintyCards[i] = nextCard;
            }
            fillCardMap(gameState.bettingRound, gameState.playerCards, gameState.commuintyCards, gameState.cardMap);
        }
        else if(gameState.bettingRound == 't'){
            int nextCard = CardUtils.addRandomCard(gameState);
            gameState.commuintyCards[3] = nextCard;
            fillCardMap(gameState.bettingRound, gameState.playerCards, gameState.commuintyCards, gameState.cardMap);
        }
        else if(gameState.bettingRound == 'r'){
            int nextCard = CardUtils.addRandomCard(gameState);
            gameState.commuintyCards[4] = nextCard;
            fillCardMap(gameState.bettingRound, gameState.playerCards, gameState.commuintyCards, gameState.cardMap);
            gameState.playerScores = GameUtils.playerScores(gameState.playerCards, gameState.commuintyCards);
        }
        Arrays.fill(gameState.lastActionValues, 0);
    }

    @Override
    public String nextChanceHistory(GameState gameState, String history) {
        StringBuilder historyBuilder = new StringBuilder();
        CfrUtils.nextHistory(historyBuilder, gameState.bettingRound, gameState.activePlayers,
                gameState.pot, gameState.playerStacks);
        return historyBuilder.toString();
    }

    @Override
    public String[] nextHistories(GameState gameState, String history) {
        short[] actions = legalActions(gameState, history);
        String[] allHistories = new String[actions.length];
        for (int i = 0; i < actions.length; i++) {
            allHistories[i] = history + betAmountToString(gameState, actions[i]);
        }
        return allHistories;
    }

    @Override
    public short[] chanceActions(GameState gameState, String history) {
        return new short[0];
    }

    @Override
    public short[] legalActions(GameState gameState, String history) {
        if(!legalActions.containsKey(history)) {
            String[] history_split = StringUtils.getActionStrings(history);
            int numRaises = 0;
            if (!history_split[0].equals("")) {
                numRaises = StringUtils.countChars(history_split, 'r');
            }
            ArrayList<Short> action_dict = new ArrayList<>();
            if (numRaises > 0) {
                action_dict.add((short) -1);
            }
            action_dict.add((short) 0);
            if (gameState.bettingRound == 'p' && history_split.length == NUM_PLAYERS + 1 && numRaises == 2) {
                action_dict.remove(0);
            }
            if (gameState.bettingRound == 'p') {
                if (numRaises < 5){
                    pocketActions(action_dict, numRaises);
                }

            } else if (gameState.bettingRound == 'f') {
                if (numRaises < 2 && ArrayUtils.allInPlayed(gameState.playerStacks)) {
                    addBetSize(gameState, action_dict, (double) gameState.pot * .5);
                }
                else{
                    addBetSize(gameState, action_dict, gameState.pot);
                }
            } else {
                addBetSize(gameState, action_dict, gameState.pot);
            }
            return NodeUtils.toShortArray(action_dict);
        }
        else{
            return legalActions.get(history);
        }
    }

    private void addBetSize(GameState gameState, ArrayList<Short> action_dict, double betValue){
        short betSize = (short) ArrayUtils.getValueOrMax(betValue,
                gameState.playerStacks[gameState.currentPlayer],
                gameState.playerStacks[gameState.currentPlayer] * .8);
            action_dict.add(betSize);
    }

    private static void pocketActions(ArrayList<Short> action_dict, int numRaises){
        if(numRaises == 2) {
            action_dict.add((short)6);
        }
        else if(numRaises == 3) {
            action_dict.add((short)16);
        }
        else if(numRaises == 4) {
            action_dict.add((short)30);
        }
    }

    private void fillCardMap(char bettingRound, int[][] playerCards, int[] commuintyCards,
                             HashMap<Integer, String> cardMap){
        for (int i = 0; i < CONSTANTS.NUM_PLAYERS; i++) {
            if(bettingRound == 'p'){
                cardMap.put(i, Lossless.Pocket(playerCards[i]));
            }
            else if(bettingRound == 'f'){
                String flopName = Lossless.Flop(playerCards[i], commuintyCards);
                if (lossless){cardMap.put(i, flopName);}
                else{
                    cardMap.put(i, buckets.getFlopBucket(flopName));}
            }
            else if(bettingRound == 't'){
                String turnName = Lossless.Turn(playerCards[i], commuintyCards);
                if (lossless){ cardMap.put(i, turnName);}
                else{
                    cardMap.put(i, buckets.getTurnBucket(turnName));}
            }
            else if(bettingRound == 'r'){
                String riverName = Lossless.River(playerCards[i], commuintyCards);
                if (lossless){cardMap.put(i, riverName);}
                else{cardMap.put(i, buckets.getRiverBucket(riverName));}
            }
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
        int[][] pocketCards = new int[NUM_PLAYERS][2];
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
        fillCardMap(startingBettingRound, pocketCards, startingCommunityCards, cardMap);
        return new GameState(1, startingStacks, activePlayers, lastActionValues,
                pot, startingBettingRound, pocketCards, deck, cardMap);
    }
}

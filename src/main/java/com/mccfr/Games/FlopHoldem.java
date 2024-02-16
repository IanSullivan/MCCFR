package com.mccfr.Games;



import com.mccfr.Abstractions.Buckets;
import com.mccfr.Abstractions.Lossless;
import com.mccfr.PokerUtils.*;
import com.mccfr.SubGameSolving.UnsafeDeck;
import com.mccfr.Utils.Combinations;
import com.mccfr.app.CfrUtils;

import java.util.*;

public class FlopHoldem extends Game {
    private static final Random r = new Random();
    public static final int NUM_PLAYERS = 2;
    public Buckets buckets;
    public static final int STARTING_STACK = 50;
    public static final char STARTING_STATE = 'p';
    public static final String STARTING_HISTORY = STARTING_STATE + " " + NUM_PLAYERS + " 0 r1.r2.";
    private final UnsafeDeck[] allDecks = new UnsafeDeck[NUM_PLAYERS];
    private static final List<int[]> allPockets = Combinations.generate(52, 2);
    public float[][] allReaches = new float[NUM_PLAYERS][allPockets.size()];
    public int[] startingCommunityCards = new int[]{-1, -1, -1, -1, -1};
    public char betingRound = 'p';
    int[] startingStacks = new int[NUM_PLAYERS];
    int[] lastActionValues = new int[NUM_PLAYERS];
    boolean[] activePlayers = new boolean[NUM_PLAYERS];

    public HashMap<String, String[]> nextHistoriesMap = new HashMap<>();
    HashMap<String, String> nextChanceHistory = new HashMap<>();
//    HashMap<String, short[]> legalActions = new HashMap<>();
    HashMap<String, Boolean> chanceHistories = new HashMap<>();
    HashMap<String, Boolean> terminalHistories = new HashMap<>();
    public int pot;
    boolean lossless = false;
    public HashMap<String, short[]> legalActionsMap = new HashMap<String, short[]>() {{
        put(STARTING_HISTORY, new short[]{-1, 0, 4, 50});
        put(STARTING_HISTORY + "c.", new short[]{0, 7, 50});
        put(STARTING_HISTORY + "r4.", new short[]{-1, 0, 7, 50});
        put(STARTING_HISTORY + "rAll.", new short[]{-1, 0});
        put(STARTING_HISTORY + "c.rAll.", new short[]{-1, 0});
        put(STARTING_HISTORY + "r4.rAll.", new short[]{-1, 0});
        put(STARTING_HISTORY + "r4.r7.", new short[]{-1, 0, 50});
        put(STARTING_HISTORY + "c.r7.", new short[]{-1, 0, 50});
    }};

    public HashMap<Short, String> betAmount2String  = new HashMap<Short, String>() {{
        put((short) -1, "f.");
        put((short) 0, "c.");
        put((short) 2, "r2.");
        put((short) 4, "r4.");
        put((short) 7, "r7.");
        put((short) 50, "rAll.");
    }};

    public FlopHoldem(){
        buckets = new Buckets();
        resetGameState();
    }

    // setups the blinds and pots for the next hand
    @Override
    public void resetGameState() {
        Arrays.fill(activePlayers, true);
        Arrays.fill(startingStacks, STARTING_STACK);
        lastActionValues[0] = 1;
        lastActionValues[1] = 2;
        startingStacks[0] -= 1;
        startingStacks[1] -= 2;
        pot = 3;
        for (int i = 0; i < NUM_PLAYERS; i++) {
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
        betingRound = gameState.bettingRound;
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
            boolean isGameDone = isGameDone(gameState, isRoundDone);
            terminalHistories.put(history, isGameDone);
            return isGameDone;
        }
        else{
            return terminalHistories.get(history);
        }
    }

    public static boolean isGameDone(GameState gameState, boolean isRoundDone) {
        if (RoundUtils.numberOfPlayerLeft(gameState.activePlayers) <= 1) {
            return true;
        } else if (gameState.bettingRound == 'f' && isRoundDone) {
            return true;
        } else if(RoundUtils.allStacksEmpty(gameState.activePlayers, gameState.playerStacks)){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public boolean isChance(String history, GameState gameState) {
        if(!chanceHistories.containsKey(history)){
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
            chanceHistories.put(history, isRoundDone);
            return isRoundDone;
        }
        else{
            return chanceHistories.get(history);
        }
    }

    @Override
    public float getReward(GameState gameState, String history, int updatePlayer) {
//        All in preflop, randomly draw the rest of the hand, compute rewards
        if (gameState.bettingRound == 'p'){
            applyChance(gameState, (short)0);
        }
        return CfrUtils.setRewardFlop(gameState.playerScores, gameState.activePlayers, updatePlayer,
                gameState.playerStacks, gameState.pot);
    }

    private void fillCardMap(char bettingRound, int[][] playerCards, int[] commuintyCards,
                             HashMap<Integer, String> cardMap){
        for (int i = 0; i < NUM_PLAYERS; i++) {
            if(bettingRound == 'p'){
                cardMap.put(i, Lossless.Pocket(playerCards[i]));}
            else if(bettingRound == 'f'){
                String flopName = Lossless.Flop(playerCards[i], commuintyCards);
                if (lossless){
                    cardMap.put(i, flopName);}
                else{
                    cardMap.put(i, buckets.getFlopBucket(flopName));
                }
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


    private int addRandomCard(GameState gameState){
        int p = r.nextInt(gameState.deck.size());
        while (ArrayUtils.contains(gameState.commuintyCards, gameState.deck.get(p))){
            p = r.nextInt(gameState.deck.size());
        }
        int cardValue = gameState.deck.get(p);
        gameState.deck.remove(Integer.valueOf(cardValue));
        return cardValue;
    }

    @Override
    public void applyChance(GameState gameState, short action) {
        gameState.bettingRound = RoundUtils.getNextState(gameState.bettingRound);
        if (gameState.bettingRound == 'f') {
            for (int i = 0; i < 3; i++) {
                int nextCard = addRandomCard(gameState);
                gameState.commuintyCards[i] = nextCard;
            }
            fillCardMap(gameState.bettingRound, gameState.playerCards, gameState.commuintyCards, gameState.cardMap);
            gameState.playerScores = GameUtils.playerScoresFlop(gameState.playerCards, gameState.commuintyCards);
        }
        Arrays.fill(gameState.lastActionValues, 0);
    }

    @Override
    public String nextChanceHistory(GameState gameState, String history) {
        if(!nextChanceHistory.containsKey(history)){
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
        if(!nextHistoriesMap.containsKey(history)) {
            short[] actions = legalActions(gameState, history);
            String[] allHistories = new String[actions.length];
            for (int i = 0; i < actions.length; i++) {
                if (!betAmount2String.containsKey(actions[i])){
                    betAmount2String.put(actions[i], "r" + actions[i] + ".");
                }
                allHistories[i] = history + betAmount2String.get(actions[i]);
            }
            nextHistoriesMap.put(history, allHistories);
            return allHistories;
        }
        else{
            return nextHistoriesMap.get(history);
        }
    }

    @Override
    public short[] chanceActions(GameState gameState, String history) {
        return new short[0];
    }

    @Override
    public short[] legalActions(GameState gameState, String history) {
        if(!legalActionsMap.containsKey(history)){
            String[] history_split = StringUtils.getActionStrings(history);
            int numRaises = 0;
            if (!history_split[0].equals("")) {
                numRaises = StringUtils.countChars(history_split, 'r');
            }
            ArrayList<Short> action_dict = new ArrayList<>();
            if (numRaises > 0) {
                action_dict.add((short) -1);
            }
            action_dict.add((short)0);
            if (numRaises == 0){
                action_dict.add((short) ((short)gameState.pot / 3));
                action_dict.add((short) ((short)gameState.pot * 0.66));
                action_dict.add((short) ((short) gameState.pot * 1.25));
                action_dict.add((short) 50);
            }
            else{
                String lastBet = history_split[history_split.length - 1];
                if(!lastBet.equals("rAll")){
                    short actionValue = StringUtils.extractShortValue(lastBet);
                    if (actionValue * 3 < gameState.playerStacks[gameState.currentPlayer]){
                        action_dict.add((short) (actionValue * 3));
                    }
                    action_dict.add((short) 50);
                }
            }
            short[] actions = NodeUtils.toShortArray(action_dict);
            legalActionsMap.put(history, actions);
            return actions;
        }
        else{
            return legalActionsMap.get(history);
        }
    }

        private static void flopActions(ArrayList<Short> action_dict, int minBet, int numRaises,
                                    int stack, int nRemainingPlayers){
        if(numRaises == 0) {
            set_action(action_dict, stack, 6, minBet);
            set_action(action_dict, stack, 10, minBet);
        }
        else if(numRaises == 1){
            set_action(action_dict, stack, 16, minBet);
            set_action(action_dict, stack, 20, minBet);
//            set_action(action_dict, stack, 30, minBet);
        }
        else if(numRaises == 2 && nRemainingPlayers <= 4){
//            set_action(action_dict, stack, 30, minBet);
            set_action(action_dict, stack, 50, minBet);
        }
        else if(numRaises == 3 && nRemainingPlayers <= 4){
//            set_action(action_dict, stack, 100, minBet);
//            action_dict.add((short)(int)StringUtils.betAmount2int.get("rAll."));
        }
    }
    private static void set_action(ArrayList<Short> action_dict, int maxBetSize, int bet_size, int min_bet){
        if(bet_size <= maxBetSize && bet_size >= min_bet &&
                !action_dict.contains((short)(int)StringUtils.betAmount2int.get("r" + bet_size + "."))){
            action_dict.add((short)(int)StringUtils.betAmount2int.get("r" + bet_size + "."));
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
        fillCardMap(betingRound, pocketCards, startingCommunityCards, cardMap);
        return new GameState(1, startingStacks, activePlayers, lastActionValues,
                pot, betingRound, pocketCards, deck, cardMap);
    }

    public static void main(String[] args){
//        float[] allReaches = new float[allPockets.size()];
//        Arrays.fill(allReaches, 1);
//        allReaches[0] = 1;
//        allReaches[143] = 1;
//        FlopHoldem game = new FlopHoldem();
//        GameState gameState = game.resetGame();
//        game.addRandomCard(gameState);
//        System.out.println(gameState.deck);
//        String history = STARTING_HISTORY + "c.r5.c.";
//        System.out.println(history);
//        System.out.println(game.isChance(history, gameState));
//        game.resetGame();
    }
}

package com.mccfr.Games;

import com.mccfr.PokerUtils.NodeUtils;
import com.mccfr.PokerUtils.RoundUtils;
import com.mccfr.PokerUtils.StringUtils;
import com.mccfr.app.CONSTANTS;

import java.util.*;

public class Leduc extends Game {
    private static final Random r = new Random();
    public static final int STARTING_STACK = 20;
    public static String STARTING_HISTORY =  "1 ";

    public static final HashMap<Short, String> betAmount2String  = new HashMap<Short, String>() {{
        put((short) -1, "f.");
        put((short) 0, "c.");
        put((short) 2, "r2.");
        put((short) 4, "r4.");
    }};

    public static final HashMap<Integer, String> idx2Name  = new HashMap<Integer, String>() {{
        put(1 , "J");
        put(2 , "Q");
        put(0 , "K");
    }};

    @Override
    public void resetGameState() {

    }

    @Override
    public void setStartingState(GameState gameState, float[][] allReaches) {

    }

    public float getReward(GameState gameState, String history, int updatePlayer) {
        String[] historySplit = getActionStrings(gameState, history);
        int oppPlayer = (updatePlayer + 1) % CONSTANTS.NUM_PLAYERS;
        if (historySplit[historySplit.length-1].equals("f")){
            if(updatePlayer == gameState.currentPlayer){
                return -1 * (STARTING_STACK - gameState.playerStacks[updatePlayer]);
            }
            else{
                return (STARTING_STACK - gameState.playerStacks[oppPlayer]);
            }
        }
        else{
            return getShowdown(gameState, updatePlayer);
        }
    }
    public float getShowdown(GameState gameState, int updatePlayer) {
        int oppPlayer = (updatePlayer + 1) % CONSTANTS.NUM_PLAYERS;
        int currentPlayerCard = gameState.playerCards[updatePlayer][0] % 3;
        int opponentPlayerCard = gameState.playerCards[oppPlayer][0] % 3;
        int communityCard = gameState.commuintyCards[0] % 3;
        if (currentPlayerCard == communityCard){
            return (float)gameState.pot / 2;
        }
        else if (opponentPlayerCard == communityCard){
            return -1 * ((float)gameState.pot / 2) ;
        }
        else if (opponentPlayerCard == currentPlayerCard){
            return 0;
        }
        else{
            currentPlayerCard = currentPlayerCard == 0 ? 3: currentPlayerCard;
            opponentPlayerCard = opponentPlayerCard == 0 ? 3: opponentPlayerCard;
            return (currentPlayerCard > opponentPlayerCard ? 1: -1) * ((float)gameState.pot / 2);
        }
    }

    @Override
    public boolean isTerminal(String history, GameState gameState) {
        String[] historySplit = getActionStrings(gameState, history);
        if(historySplit.length > 0){
            if(historySplit[historySplit.length-1].equals("f")){
                return true;
            }
        }
        return isChance(history, gameState) && gameState.bettingRound == '2';
    }

    @Override
    public boolean isChance(String history, GameState gameState) {
        String[] historySplit = getActionStrings(gameState, history);
        if(historySplit.length >= 2){
            if (historySplit[historySplit.length-1].equals("f")){
                return true;}
            else if(historySplit[historySplit.length - 1].equals("c") && historySplit[historySplit.length - 2].startsWith("r")){
                return true;}
            else if (historySplit[historySplit.length - 1].equals("c") && historySplit[historySplit.length - 2].startsWith("c")){
                return true;}
            else{
                return false;}
        }
        else{
            return false;}
    }

    private String[] getActionStrings(GameState gameState, String history) {
        String[] split = history.split(String.valueOf(gameState.bettingRound));
        return split[split.length-1].replaceAll(" ", "").split("[.]");
    }

    @Override
    public void applyChance(GameState gameState, short selection) {
        gameState.bettingRound = '2';
        gameState.commuintyCards[0] = gameState.deck.get(r.nextInt(gameState.deck.size()));
        gameState.deck.remove(Integer.valueOf(selection));
        for (int i = 0; i < 2; i++) {
            gameState.cardMap.put(i, idx2Name.get(gameState.playerCards[i][0] % 3) + " " +
                    idx2Name.get(gameState.commuintyCards[0] % 3));
        }
        Arrays.fill(gameState.lastActionValues, 0);
    }

    public String nextChanceHistory(GameState gameState, String history){
        return history + " " + gameState.bettingRound + " ";
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
    public String[] nextHistories(GameState gameState, String history) {
        short[] actions = legalActions(gameState, history);
        String[] allHistories = new String[actions.length];
        for (int i = 0; i < actions.length; i++) {
            allHistories[i] = history + betAmount2String.get(actions[i]);
        }
        return allHistories;
    }

    public short[] chanceActions(GameState gameState, String history) {
        short[] chanceActions = new short[gameState.deck.size()];
        for (int i = 0; i < gameState.deck.size(); i++) {
            int a = gameState.deck.get(i);
            chanceActions[i] = (short)a;
        }
        return chanceActions;
    }

    @Override
    public short[] legalActions(GameState gameState, String history) {
        int numRaises = 0;
        String[] history_split = getActionStrings(gameState, history);
        if (!history_split[0].equals("")) {
            numRaises = StringUtils.countChars(history_split, 'r');
        }
        ArrayList<Short> action_dict = new ArrayList<>();
        if (numRaises > 0) {
            action_dict.add((short) -1);
        }
        action_dict.add((short)0);

        if (numRaises < 2) {
            if (gameState.bettingRound == '1') {
                action_dict.add((short) 2);
            }
            else{
                action_dict.add((short) 4);
            }
        }
        return NodeUtils.toShortArray(action_dict);
    }

    @Override
    public GameState resetGame() {
        ArrayList<Integer> deck = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            deck.add(i);
        }
        Collections.shuffle(deck, r);
        HashMap<Integer, String> cardMap = new HashMap<>();
        int[][] pocketCards = new int[CONSTANTS.NUM_PLAYERS][1];
        for (int i = 0; i < pocketCards.length; i++) {
            for (int j = 0; j < pocketCards[i].length; j++) {
                pocketCards[i][j] = deck.get(0);
                deck.remove(0);
                Collections.shuffle(deck, r);
            }
            cardMap.put(i, idx2Name.get(pocketCards[i][0] % 3));
        }

        int[] playerStacks = new int[CONSTANTS.NUM_PLAYERS];
        int[] lastActionValues = new int[CONSTANTS.NUM_PLAYERS];
        boolean[] activePlayers = new boolean[CONSTANTS.NUM_PLAYERS];
        Arrays.fill(activePlayers, true);
        Arrays.fill(playerStacks, STARTING_STACK);
        int pot = 2;
        playerStacks[0] -= 1;
        playerStacks[1] -= 1;
        return new GameState(-1, playerStacks, activePlayers, lastActionValues, pot, '1', pocketCards, deck, cardMap);
    }

    public static void main(String[] args) {
        Leduc game = new Leduc();
        GameState newGameState = game.resetGame();
        newGameState.bettingRound = '1';
        HashMap<Integer, String> idx2Name  = new HashMap<Integer, String>() {{
            put(1 , "J");
            put(2 , "Q");
            put(0 , "K");
        }};
        for (int i = 1; i <= 6; i++) {
            System.out.println(i);
            System.out.println(idx2Name.get(i % 3));
            System.out.println();
        }
//        boolean done = game.isTerminal("p 2 2 c.c.", newGameState);
//        short[] actions = game.legalActions(newGameState, "p 2 2 c.c.");
//        System.out.println(done);
        System.out.println(Arrays.toString(game.getActionStrings(newGameState, "1 c.")));
//        ArrayList<Integer> deck = CardUtils.buildDeck();
//        int[] lastActionValues = new int[]{0, 0, 0, 0};
//        boolean[] activePlayers = new boolean[]{true, true, true, true};
//        int[] playerStacks = new int[]{10, 10, 10, 10};
//        int[][] pocketCards = new int[CONSTANTS.NUM_PLAYERS][1];
//        HashMap<Integer, String> cardMap = new HashMap<>();
//        int pot = 3;
//        char bettingRound = 'p';
////        int currentPlayer = 1;
////        int oppPlayer = (currentPlayer + 1) % CONSTANTS.NUM_PLAYERS;
////        System.out.println(oppPlayer);
//        Leduc game = new Leduc();
////        GameState gameState = new GameState(playerStacks, activePlayers, lastActionValues, pot,
////                bettingRound, pocketCards, deck, cardMap);
//        GameState gameState  = game.resetGame();
//        game.applyAction(gameState, 0, 0);
//        game.applyAction(gameState, 3, 1);
//        System.out.println(gameState);
//        game.isChance(gameState);
//        System.out.println(gameState);
//        System.out.println(Arrays.toString(gameState.commuintyCards));

//        String[] history_split = StringUtils.getActionStrings("p 2 0 r1.r2.c.");
//        game.getAvailableActionsCoarse(2, 'p', history_split, )
//        pocketCards[0][0] = 6;
//        pocketCards[1][0] = 5;
//        gameState.commuintyCards[0] = 4;
//        game.isTerminal(gameState,  1);
    }
}

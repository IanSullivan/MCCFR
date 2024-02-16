package com.mccfr.PokerUtils;

import com.mccfr.Games.GameState;
import com.mccfr.app.CONSTANTS;

import java.util.ArrayList;
import java.util.Random;

public class CardUtils {
    private static final int NUM_PLAYERS = CONSTANTS.NUM_PLAYERS;
    private static final Random r = new Random(43);

    public static int getSuit(int cardIdx){
        if (cardIdx < 9)
            return 0; else if (cardIdx < 18)
            return 1; else if (cardIdx < 27)
            return 2; else
            return 3;
    }

    public static int getRank(int cardIdx){
        return (cardIdx % 9) + 6;
    }


    public static ArrayList<Integer> buildDeck() {
        ArrayList<Integer> deck = new ArrayList<>();
        for (int i = 0; i < 52; i++) {
            deck.add(i);
        }
        return deck;
    }

    public static void setCardsFromDeck(int[][] pocketCards, int[] communityCards, ArrayList<Integer> deck){
        int deckIdx = 0;
        for (int i = 0; i < pocketCards.length; i++) {
            for (int j = 0; j < pocketCards[i].length; j++) {
                pocketCards[i][j] = deck.get(deckIdx);
                deckIdx++;
            }
        }
        Object[] a = deck.subList(deckIdx, deckIdx+5).toArray();
        for (int i = 0; i < a.length; i++) {
            communityCards[i] = (int)a[i];
        }
    }

    public static int addRandomCard(GameState gameState){
        int p = r.nextInt(gameState.deck.size());
        while (ArrayUtils.contains(gameState.commuintyCards, gameState.deck.get(p))){
            p = r.nextInt(gameState.deck.size());
        }
        int cardValue = gameState.deck.get(p);
        gameState.deck.remove(Integer.valueOf(cardValue));
        return cardValue;
    }
}

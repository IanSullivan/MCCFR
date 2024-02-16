package com.mccfr.Evaluation;



import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

public class Deck {
    private final ArrayList<Card> cards;
    public HashMap<String, Integer> card2idx = new HashMap<String, Integer>();
    public HashMap<Integer, Card> idx2card = new HashMap<Integer, Card>();
    // private static final String RANKS = "23456789TJQKA";
    private static final int[] SUITS = { 0x8000, 0x4000, 0x2000, 0x1000 };
    Random rand = new Random();

    public Deck() {
        cards = new ArrayList<>();
        resetDeck();
    }

    public void resetDeck() {
        int idx = 0;
        cards.clear();
        for (int a = 0; a < 4; a++) {
//            for (int b = 4; b < 13; b++) {
            for (int b = 0; b < 13; b++) {
                Card card = new Card(b, SUITS[a]);
                cards.add(card);
                idx2card.put(idx, card);
                card2idx.put(card.toString(), idx);
                idx++;
            }
        }
    }

    public int decksize() {
        return cards.size();
    }

    public void printDeck(){
        for (Card i : cards) {
            System.out.println(i);
        }
    }

    public Card drawOne(int index) {
        return cards.get(index);
    }

    public String idx2String(int i) {
        return idx2card.get(i).toString();
    }

    public void remove(int idx) {
        cards.remove(idx);
    }

    public void removeCard(Card card_to_remove) {
        // Card cardToRemove = null;
        for (Card card : cards) {
            if (card.toString().equals(card_to_remove.toString())) {
                cards.remove(card);
                break;
            }
        }
    }

    public void removeCardFromInt(int card_to_remove_idx) {
        // Card cardToRemove = null;
        Card card_to_remove = cards.get(card_to_remove_idx);
        for (Card card : cards) {
            if (card.toString().equals(card_to_remove.toString())) {
                cards.remove(card);
                break;
            }
        }
    }

    public Card drawFromDeck(int idx) {
        return cards.get(idx);
    }

    public void returnCard(Card card) {
        cards.add(card);
    }
}

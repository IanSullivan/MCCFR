package com.mccfr.Evaluation;



public class Evaluate {
    
    public static Deck deck = new Deck();
    // All the possible combination of hands at index
    private static final int[][] HAND_COMBOS = {{ 0, 1, 2, 3, 4 }, { 0, 1, 2, 3, 5 }, { 0, 1, 2, 3, 6 }, { 0, 1, 2, 4, 5 },
            { 0, 1, 2, 5, 6 }, { 0, 1, 3, 4, 5 }, { 0, 1, 3, 4, 6 }, { 0, 1, 3, 5, 6 }, { 0, 1, 4, 5, 6 },
            { 0, 2, 3, 4, 5 }, { 0, 2, 3, 4, 6 }, { 0, 2, 3, 5, 6 }, { 0, 2, 4, 5, 6 }, { 0, 3, 4, 5, 6 },
            { 1, 2, 3, 4, 5 }, { 1, 2, 3, 4, 6 }, { 1, 2, 3, 5, 6 }, { 1, 2, 4, 5, 6 }, { 1, 3, 4, 5, 6 },
            { 2, 3, 4, 5, 6 }};

    public static Card[] findTopHand(Card[] allCards) {
        Card[] hand = new Card[5];

        int topScore = Integer.MAX_VALUE;
        int topScoreidx = 0;

        for (int i = 0; i < HAND_COMBOS.length - 1; i++) {
            for (int j = 0; j < HAND_COMBOS[i].length; j++) {
                // Populate the hand array with the possible 5 cards for the seven available
                // cards cominations
                hand[j] = allCards[HAND_COMBOS[i][j]];
            }
            int value = Hand.evaluate(hand);
            if (value < topScore) {
                topScore = value;
                topScoreidx = i;
            }
        }
        for (int j = 0; j < HAND_COMBOS[topScoreidx].length; j++) {
            // Populate the hand array with the possible 5 cards for the seven available
            // cards cominations
            hand[j] = allCards[HAND_COMBOS[topScoreidx][j]];
        }
        return hand;
    }
    public static int handScore(int[] pocketCardsIdx, int[] communityCardsIdx){
        Card[] pocketCards = { deck.idx2card.get(pocketCardsIdx[0]), deck.idx2card.get(pocketCardsIdx[1])};
        Card[] commintyCards = { deck.idx2card.get(communityCardsIdx[0]), deck.idx2card.get(communityCardsIdx[1]), deck.idx2card.get(communityCardsIdx[2]), 
            deck.idx2card.get(communityCardsIdx[3]), deck.idx2card.get(communityCardsIdx[4])};

        Card[] fullHand = new Card[pocketCards.length + commintyCards.length];
        System.arraycopy(pocketCards, 0, fullHand, 0, pocketCards.length);
        System.arraycopy(commintyCards, 0, fullHand, pocketCards.length, commintyCards.length);
        Card[] bestHand = findTopHand(fullHand);
        return Hand.evaluate(bestHand);
    }

    public static int score(int[] hand){
        Card[] finalHand = new Card[hand.length];
        for(int i=0;i<hand.length;i++){
            finalHand[i] = deck.idx2card.get(hand[i]);
        }
        return Hand.evaluate(finalHand);
    }

    public String getCardName(int i){
        return deck.drawOne(i).toString();
    }

    public void printDeck(){
        for (int i=0;i<deck.decksize();i++){
            System.out.println(deck.drawFromDeck(i));
        }
    }

    public static int flopScore(int[] pocketCardsIdx, int[] communityCardsIdx){
        Card[] pocketCards = { deck.idx2card.get(pocketCardsIdx[0]), deck.idx2card.get(pocketCardsIdx[1])};
        Card[] commintyCards = { deck.idx2card.get(communityCardsIdx[0]), deck.idx2card.get(communityCardsIdx[1]), deck.idx2card.get(communityCardsIdx[2])};

        Card[] fullHand = new Card[pocketCards.length + commintyCards.length];
        System.arraycopy(pocketCards, 0, fullHand, 0, pocketCards.length);
        System.arraycopy(commintyCards, 0, fullHand, pocketCards.length, commintyCards.length);

        return Hand.evaluate(fullHand);
    }

}

package com.mccfr.app;


import com.mccfr.Games.Leduc;

public class CONSTANTS {
    public static final int NUM_PLAYERS = 2;
    public static final int POCKET_CARDS = 2;
    public static boolean LOAD_BUCKETS = false;
    public static boolean PRINT = false;
    public static final String BPNAME = "Leduc_depth";
    public static final String ACTION_TREE = "NoLimit2PlayerActions.json";
    public static final String AVG_STRATS = "vec2Flop.txt";
//    public static final int N_THREADS =  Runtime.getRuntime().availableProcessors();
    public static final int N_THREADS = 1;
    public static Leduc GAME = null;
}

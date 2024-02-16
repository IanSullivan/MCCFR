
package com.mccfr.app;

import com.mccfr.Games.Kuhn;
import com.mccfr.Games.Leduc;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class CFRThread {

    public Map<String, Node> p1_nodeMap = new ConcurrentHashMap<>();
    public Map<String, float[]> regretSum = new ConcurrentHashMap<>();
    public Map<String, float[]> strategySum = new ConcurrentHashMap<>();
    public final Map<String, String[]> nextHistories = new ConcurrentHashMap<>();
    public final Map<String, short[]> allActions = new ConcurrentHashMap<>();
    public Map<String, Short> roundDoneHistories = new ConcurrentHashMap<>();
    public Map<String, Short> finalPot = new ConcurrentHashMap<>();
    public Map<String, String[]> actionNames = new ConcurrentHashMap<>();
    public HashMap<String, float[]> blueprint;
    Map<Character, HashMap<String, double[]>> runningScore = new ConcurrentHashMap<>();

    public CFRThread() throws IOException {
        runningScore.put('p', new HashMap<>());
        runningScore.put('f', new HashMap<>());
        runningScore.put('t', new HashMap<>());
        runningScore.put('r', new HashMap<>());
        System.out.println("loaded");
    }

    public void run_sims(String history, int iterations){
//        int n_threads = CONSTANTS.N_THREADS;
        int n_threads = 1;
        Leduc game = new Leduc();
        System.out.println(history);
        System.out.println("sim histoy");
        ExecutorService service = Executors.newFixedThreadPool(n_threads);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < n_threads; i++) {
            System.out.println("starting here");
            MCCFR vevCfr = new MCCFR(this, game);
//            vevCfr.startintState(history, gameState, allReaches, 50000, depthLimited);
            service.execute(vevCfr);
        }
        service.shutdownNow();
    }

    public static void main(String[] args) throws InterruptedException, IOException, ParseException {
        CFRThread cfr = new CFRThread();

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        System.out.println("Max Memory: " + maxMemory / (1024 * 1024) + " MB");
        System.out.println("Free Memory: " + freeMemory / (1024 * 1024) + " MB");
        System.out.println("Total Memory: " + totalMemory / (1024 * 1024) + " MB");
        long startTime = System.currentTimeMillis();
        float[][] reachProbs = new float[CONSTANTS.NUM_PLAYERS][1326];  // all pocketSize
        for (float[] reachProb : reachProbs) {
            Arrays.fill(reachProb, 1);
        }
//        cfr.run_sims(FlopHoldem.STARTING_HISTORY, gameState, reachProbs, 50000, false);
        cfr.run_sims(Leduc.STARTING_HISTORY, 50000);
        long endTime = System.currentTimeMillis();
        System.out.println("That took " + (endTime - startTime) + " milliseconds");
    }
}

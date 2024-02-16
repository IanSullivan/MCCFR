package com.mccfr.Abstractions;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mccfr.Evaluation.Deck;
import com.mccfr.Utils.Combinations;

import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Buckets {
    public  ConcurrentHashMap<String, String> riverBuckets;
    public ConcurrentHashMap <String, String> turnBuckets;
    public ConcurrentHashMap <String, String> flopBuckets;
    Lossless lossless = new Lossless();
    Reader reader;
    public Deck deck = new Deck();

    public Buckets() {
        try {
            System.out.println("started card abstraction");
            Gson gson = new Gson();

            Type map_type = new TypeToken<HashMap<String, String>>(){}.getType();

            reader = Files.newBufferedReader(Paths.get("flopHoldem3Buckets.json"));
            HashMap<String, String> mapFlopInt = gson.fromJson(reader, map_type);
            flopBuckets = new ConcurrentHashMap<>(mapFlopInt);

//            reader = Files.newBufferedReader(Paths.get("data_holdem/HoldemBuckets/turn100.json"));
//            HashMap<String, String> mapInt = gson.fromJson(reader, map_type);
//            intTurnBuckets = new ConcurrentHashMap<>(mapInt);
////
////
//            reader = Files.newBufferedReader(Paths.get("data_holdem/HoldemBuckets/river100.json"));
//            HashMap<String, String> mapRiverInt = gson.fromJson(reader, map_type);
//            intRiverBuckets = new ConcurrentHashMap<>(mapRiverInt);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public String getFlopBucket(String hand_name) {
        return flopBuckets.get(hand_name);

    }

    public String getRiverBucket(String hand_name) {
        return riverBuckets.get(hand_name);
    }

    public String getTurnBucket(String hand_name) {
        return turnBuckets.get(hand_name);
    }
}
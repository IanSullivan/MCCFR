package com.mccfr.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mccfr.PokerUtils.NodeUtils;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Databases {

    private static final DecimalFormat df = new DecimalFormat("0.00");
    public static void writeBlueprint(CFRThread cfrThread) {
        TreeMap<String, float[]> writeMap = new TreeMap<>();
//        TreeMap<String, JSONObject> writeMap = new TreeMap<>();
        try {
            System.out.println("printing...");
            ObjectMapper mapper = new ObjectMapper();
            for (String k : cfrThread.strategySum.keySet()) {
                float[] stratSum = cfrThread.strategySum.get(k);
                stratSum = trim(NodeUtils.getAverageStrategy(stratSum));
                writeMap.put(k, stratSum);
            }
            // convert book map to JSON file
            mapper.writeValue(Paths.get(CONSTANTS.BPNAME + ".json").toFile(), writeMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static HashMap<String, float[]> loadBlueprint(String filePath){
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, float[]>>(){}.getType();
        HashMap<String, float[]> map = null;
        try (FileReader reader = new FileReader(filePath)) {
            map = gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    private static float[] trim(float[] array){
        float[] finalArr = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            if(array[i] < 0.05){
                array[i] = 0;
            }
        }
        // Renormalize
        float normalizingSum = 0;
        for (float v : array) {
            normalizingSum += v;
        }
        for (int i = 0; i < array.length; i++) {
            finalArr[i] = Float.parseFloat(df.format(array[i] / normalizingSum));
        }
        return finalArr;
    }
}

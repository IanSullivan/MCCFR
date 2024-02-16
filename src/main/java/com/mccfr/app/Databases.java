package com.mccfr.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mccfr.PokerUtils.NodeUtils;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

public class Databases {
    public static void writeBlueprint(CFRThread cfrThread) {
        TreeMap<String, float[]> writeMap = new TreeMap<>();
//        TreeMap<String, JSONObject> writeMap = new TreeMap<>();
        try {
            System.out.println("printing...");
            ObjectMapper mapper = new ObjectMapper();
            for (String k : cfrThread.strategySum.keySet()) {
                float[] stratSum = cfrThread.strategySum.get(k);
                stratSum = NodeUtils.getAverageStrategy(stratSum);
                writeMap.put(k, stratSum);
            }
            // convert book map to JSON file
            mapper.writeValue(Paths.get(CONSTANTS.BPNAME + ".json").toFile(), writeMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Map<String, float[]> loadBlueprint(String filePath){
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, float[]>>(){}.getType();
        Map<String, float[]> map = null;
        try (FileReader reader = new FileReader(filePath)) {
            map = gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (map != null) {
            for (Map.Entry<String, float[]> entry : map.entrySet()) {
                System.out.println("Key = " + entry.getKey());
                System.out.print("Values = ");
                for (float value : entry.getValue()) {
                    System.out.print(value + " ");
                }
                System.out.println();
            }
        }
        return map;
    }
}

package com.mccfr.PokerUtils;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public final class StringUtils {
    private static final char[] roundNames = new char[]{'p', 'f', 't', 'r'};
    static DecimalFormat df = new DecimalFormat("#.###");
    private static final int MAX_BET_SIZE = 200;
    public static HashMap<Integer, String> betAmount2String  = new HashMap<Integer, String>() {{
        put(-1, "f.");
        put(0, "c.");
        for (int i = 1; i < MAX_BET_SIZE; i++) {
            put(i, String.format("r%s.", i));
        }
        put(MAX_BET_SIZE, "rAll.");
    }};

    public static HashMap<String, Integer> betAmount2int  = new HashMap<String, Integer>() {{
        put("f.",  -1);
        put("c.",  0);
        for (int i = 1; i < MAX_BET_SIZE; i++) {
            put(String.format("r%s.", i),  i);
        }
        put("rAll.",  MAX_BET_SIZE);
    }};

    public static int countChars(String[] fullString, char searchChar) {
        int count = 0;
        for (String s : fullString) {
            if (s.charAt(0) == searchChar) {
                count++;
            }
        }
        return count;
    }

    public int getBetAmount(String action){
        return betAmount2int.get(action);
    }

    public String getString(int idx){
        return betAmount2String.get(idx);
    }

    public static float[] string2Array(String a){

        a = a.replace(",", " ");
        a = a.replace("[", " ");
        a = a.replace("]", " ");
        String[] out = a.split(" ");
        ArrayList<Float> allPoints = new ArrayList<>();
        for (int i = 0; i < out.length-1; i++) {
            if(!out[i].equals("")){
                allPoints.add(Float.valueOf(out[i]));
            }
        }
        float[] finalOut = new float[allPoints.size()];
        for (int i = 0; i < allPoints.size(); i++) {
            finalOut[i] = allPoints.get(i);
        }
//        SubGameUtils.reNormalize(finalOut);
        for (int i = 0; i < finalOut.length; i++) {
            finalOut[i] = Float.parseFloat(df.format(finalOut[i]));
        }
        return finalOut;
    }

    public static short extractShortValue(String input) {
        // Define a regular expression to match a number following "r" (e.g., "r15.")
        String regex = "r(\\d+)";

        // Attempt to match the regular expression in the input string
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            try {
                // Extract the matched number and parse it as a short
                String numberStr = matcher.group(1);
                short extractedValue = Short.parseShort(numberStr);
                return extractedValue;
            } catch (NumberFormatException e) {
                // Handle parsing errors if the number is out of the range for a short, etc.
                throw new IllegalArgumentException("Invalid short value in input: " + input);
            }
        } else {
            throw new IllegalArgumentException("Input does not match the expected pattern: " + input);
        }
    }

    public static String[] getActionStrings(String history) {
        String[] split = history.split(" ");
        try{
            return split[3].replaceAll(" ", "").split("[.]");
        }catch (ArrayIndexOutOfBoundsException e){
            return new String[]{""};
        }
    }

    public static String[] getActionStringsPre(String history) {
        String[] split = history.split(" ");
        try{
            String[] out = split[3].replaceAll(" ", "").split("[.]");
            return Arrays.copyOfRange(out, 2, out.length);
        }catch (ArrayIndexOutOfBoundsException e){
            return new String[]{""};
        }
    }

    public static String[] getActionsTaken(String startingHistory, String finalHistory){
        String[] finalStrings = StringUtils.getActionStrings(finalHistory);
        String[] startingStrings = StringUtils.getActionStrings(startingHistory);
        String[] actionsTaken = new String[finalStrings.length - startingStrings.length];
        int startingIdx = startingStrings.length;
        if (actionsTaken.length >= 0)
            System.arraycopy(finalStrings, startingIdx, actionsTaken, 0, actionsTaken.length);
        return actionsTaken;
    }

    public static String removeLastAction(String history, String action){
        String[] historySplit = history.split("\\.");
        String[] last = Arrays.copyOf(historySplit, historySplit.length-1);
        return String.join( ".", last) + '.';
    }
}

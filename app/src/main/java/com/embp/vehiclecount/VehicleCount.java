package com.embp.vehiclecount;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VehicleCount {
    private static final String TAG = "VehicleCounter";
    private static final String PREF_NAME = "VehicleCountPrefs";
    public static void incrementCount(Context context, String location, String vehicleType){
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        int currentCount = preferences.getInt(getKey(location, vehicleType), 0);
        currentCount++;
        preferences.edit().putInt(getKey(location, vehicleType), currentCount).apply();
        Log.d(TAG,"Increased Count for "+location+","+vehicleType+" : "+currentCount);
    }
    public static int getCount(Context context, String location, String vehicleType){
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        //Log.d(TAG, "Count for " + location + ", " + vehicleType + ": " + preferences.getInt(getKey(location, vehicleType), 0));
        return preferences.getInt(getKey(location, vehicleType), 0);
    }
    public static void resetCount(Context context, String location, String vehicleType){
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putInt(getKey(location,vehicleType), 0).apply();
        Log.d(TAG, "Reset count for " + location + ", " + vehicleType + " to 0");
    }
    public static void matchCounter(Context context, String location, String vehicleType, int setCounter){
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putInt(getKey(location,vehicleType), setCounter).apply();
        Log.d(TAG, "Match count for " + location + ", " + vehicleType + " to entered Number");
    }
    public static  Map<String, Map<String, Integer>> getAllCounts(Context context){
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Map<String, Map<String, Integer>> allCounts = new HashMap<>();

        Map<String, ?> allEntries = preferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()){
            String key = entry.getKey();
            int count = (int) entry.getValue();

            String[] parts = key.split("_");
            if (parts.length == 2){
                String location = parts[0];
                String vehicleType = parts[1];

                //Log.d(TAG, "Count for " + location + ", " + vehicleType + ": " + count);

                if (!allCounts.containsKey(location)){
                    allCounts.put(location, new HashMap<>());
                }
                Objects.requireNonNull(allCounts.get(location)).put(vehicleType, count);
            }
        }
        return allCounts;
    }
    private static  String getKey(String location, String vehicleType){
        return location+"_"+vehicleType;
    }
}

package com.example.androidproject;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

class Scratch {
    public static HashMap<String, TreeSet<String>> createMap() {
        HashMap<String, TreeSet<String>> map = new HashMap<>();
        map.put("a", new TreeSet<>());
        map.get("a").add("1");
        map.get("a").add("3");

        map.put("b", new TreeSet<>());
        map.get("b").add("2");

        map.put("c", new TreeSet<>());
        map.get("c").add("2");
        map.get("c").add("9");

        map.put("d", new TreeSet<>());
        map.get("d").add("1");
        map.get("d").add("5");

        return map;
    }


    public static HashMap<String, TreeSet<String>> mapFirstStopToSameDestinations(HashMap<String, TreeSet<String>> map) {
        HashMap<String, TreeSet<String>> firstStopToSameDestinations = new HashMap<>();

        // Iterating every set of entry in the HashMap
        for (String firstKey : map.keySet()) {

            for (String nextKey : map.keySet()) {

                if (!firstKey.equals(nextKey)) {
                    String firstKeyFirstValue = map.get(firstKey).first();
                    String nextKeyFirstValue = map.get(nextKey).first();

                    if (!firstStopToSameDestinations.containsKey(firstKeyFirstValue)) {
                        firstStopToSameDestinations.put(firstKeyFirstValue, new TreeSet<String>() {{
                            add(firstKey);
                        }});
                    }

                    if (firstKeyFirstValue.equals(nextKeyFirstValue)) {

                        try {
                            firstStopToSameDestinations.get(map.get(firstKey).first()).add(firstKey);
                            firstStopToSameDestinations.get(map.get(firstKey).first()).add(nextKey);
                        }

                        catch (NullPointerException e) {
                            firstStopToSameDestinations.put(map.get(firstKey).first(), new TreeSet<>());
                            firstStopToSameDestinations.get(map.get(firstKey).first()).add(firstKey);
                            firstStopToSameDestinations.get(map.get(firstKey).first()).add(nextKey);
                        }
                    }
                }
            }
        }

        return firstStopToSameDestinations;
    }

    public static HashMap<String, TreeSet<String>> mergeDestinations(HashMap<String, TreeSet<String>> notMergedMap, HashMap<String, TreeSet<String>> mergeMap) {
        HashMap<String, TreeSet<String>> mergedMap = new HashMap<>();
        for (TreeSet<String> destinations : mergeMap.values()) {
            String destinationsString = String.join(" / ", destinations);
            TreeSet<String> mergedStops = new TreeSet<>();
            for (String destination : destinations) {
                mergedStops.addAll(notMergedMap.get(destination));
            }
            mergedMap.put(destinationsString, mergedStops);
        }
        return mergedMap;
    }

    public static void main(String[] args) {
        HashMap<String, TreeSet<String>> map = createMap();
        HashMap<String, TreeSet<String>> mergeMap = mapFirstStopToSameDestinations(map);
        map = mergeDestinations(map, mergeMap);

        for (Map.Entry<String, TreeSet<String>> entry: map.entrySet()) {
            System.out.println(entry);
        }
    }
}

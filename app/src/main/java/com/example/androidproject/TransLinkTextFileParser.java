package com.example.androidproject;

import android.app.Activity;
import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Responsible for all TransLink text parsing.
 */
public class TransLinkTextFileParser {
    Activity activity;
    Context context;

    TransLinkTextFileParser(Activity activity) {
        this.activity = activity;
        this.context = this.activity.getBaseContext();
    }

    /**
     * Splits each line of text file into an array of Strings
     * @param fileName a String
     * @return ArrayList of array of strings of tokenized file lines
     */
    public ArrayList<String[]> tokenizeFileLines(String fileName) {
        ArrayList<String[]> tokenizedLines = new ArrayList<>();

        try {
            //Read the file
            InputStream inputStream = context
                    .getResources()
                    .openRawResource(context
                            .getResources()
                            .getIdentifier(fileName,"raw", context
                                    .getPackageName()));

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            bufferedReader.readLine();
            String line = bufferedReader.readLine();

            while (line != null) {
                //Use string.split to load a string array with the values from the line of
                //the file, using a comma as the delimiter
                String[] tokenize = line.split(",");
                tokenizedLines.add(tokenize);
                line = bufferedReader.readLine();
            }

            //Close reader, catch errors
            bufferedReader.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return tokenizedLines;
    }

    /**
     * Map stop ids to their trip ids.
     * @param tripIds a TreeSet
     * @return a Hashmap of stop ids mapped to trip ids
     */
    public HashMap<String, String> mapStopIdsToTripIds(TreeSet<String> tripIds) {
        HashMap<String, String> stopTripMap = new HashMap<>();
        ArrayList<String[]> tokenizedLines = tokenizeFileLines("stop_times");

        for (String[] tokenize : tokenizedLines) {
            String tripId = tokenize[0];
            String stopId = tokenize[3];

            if (tripIds.contains(tripId)) {
                stopTripMap.put(stopId, tripId);
            }
        }

        return stopTripMap;
    }

    /**
     * Map stop strings (stop num + stop name) to the corresponding stop id.
     * @param stopIds a Set
     * @return a Set of stop names
     */
    public HashMap<String, String> mapStopStringsToStopIds(Set<String> stopIds) {
        HashMap<String, String> stopStringStopIdMap = new HashMap<>();
        ArrayList<String[]> tokenizedLines = tokenizeFileLines("stops");

        for (String[] tokenize : tokenizedLines) {
            String stopId = tokenize[0];
            String stopCode = tokenize[1];
            String stopName = tokenize[2];

            if (stopIds.contains(stopId)) {
                String stopString;

                try {
                    String[] stopNameSplit = stopName.split("bound "); // Remove direction
                    stopString = String.format("%s: %s", stopCode, stopNameSplit[1]);
                }

                catch (ArrayIndexOutOfBoundsException e) {
                    stopString = String.format("%s: %s", stopCode, stopName);
                }

                stopStringStopIdMap.put(stopString, stopId);
            }
        }

        return stopStringStopIdMap;
    }

    /**
     * Map route strings (route num + route name) to their route ids.
     * @return HashMap of route strings mapped to route ids
     */
    public HashMap<String, String> mapRouteStringsToRouteIds() {
        HashMap<String, String> routes = new HashMap<>();
        ArrayList<String[]> tokenizedLines = tokenizeFileLines("routes");

        for (String[] tokenize : tokenizedLines) {
            String routeId = tokenize[0];
            String busNum = tokenize[2];
            String routeName = tokenize[3];

            // Trains have no bus number, do not add these to routes
            String routeString;
            if (!busNum.isEmpty()) {
                routeString = String.format("%s: %s", busNum, routeName);
                routes.put(routeString, routeId);
            }
        }

        return routes;
    }

    /**
     * Map route destination string to corresponding trip id
     * @param selectedRouteId a String
     * @param routeNum a String
     * @return Hashmap where destination strings are mapped to trip id
     */
    public HashMap<String, TreeSet<String>> mapRouteDestinationsToTripIds(String selectedRouteId, String routeNum) {
        HashMap<String, TreeSet<String>> destinations = new HashMap<>();
        ArrayList<String[]> tokenizedLines = tokenizeFileLines("trips");

        for (String[] tokenize : tokenizedLines) {
            String fileRouteId = tokenize[0];
            String tripId = tokenize[2];
            String destination = tokenize[3];

            if (Objects.equals(selectedRouteId, fileRouteId)) {
                destination = formatDestination(destination, routeNum);

                if (!destinations.containsKey(destination)) {
                    destinations.put(destination, new TreeSet<>());
                }

                Objects.requireNonNull(destinations.get(destination)).add(tripId);
            }
        }

        return destinations;
    }

    /**
     * Format destination strings to follow 'To destination' format
     * @param destination a String
     * @return formatted destination String
     */
    public static String formatDestination(String destination, String routeNum) {
        String[] destinationSplit = destination.split(" ");
        List<String> destinationArrayList = new ArrayList<>(Arrays.asList(destinationSplit));

        // Remove initial route number from string
        if (routeNum.contains(destinationSplit[0])) {
            destinationArrayList.remove(0);
        }

        // Remove NightBus from NightBus routes
        if (routeNum.contains("N")) {
            destinationArrayList.remove("NightBus");
        }

        // Remove any lone 'to's
        destinationArrayList.remove("To");
        destinationArrayList.remove("to");

        // Special case for WCE
        if (Objects.equals(routeNum, "WCE")) {
            destinationArrayList.remove("West");
            destinationArrayList.remove("Coast");
            destinationArrayList.remove("Express");
            destinationArrayList.remove("Train");
        }

        // Add initial To
        destinationArrayList.add(0, "To");

        destinationSplit = destinationArrayList.toArray(new String[0]);
        return String.join(" ", destinationSplit);
    }
}

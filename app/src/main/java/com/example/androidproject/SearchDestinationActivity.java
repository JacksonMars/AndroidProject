package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class SearchDestinationActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> arrayAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_destination);

        final LoadingDialog loadingDialog = new LoadingDialog(SearchDestinationActivity.this);

        // Get the intent
        Intent intent = getIntent();

        // Option 2: Extract data from bundle
        Bundle bundle = intent.getBundleExtra("bundle");
        HashMap<String, TreeSet<String>> destinationsToTripIds =
                (HashMap<String, TreeSet<String>>) bundle.getSerializable("destinationsToTripIds");

        // Put stop strings in ArrayList, sort
        Set<String> destinations = destinationsToTripIds.keySet();
        ArrayList<String> destinationStrings = new ArrayList<>(destinations);
        destinationStrings.sort(Comparator.naturalOrder());

        //Create adapter for routes listview
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, destinationStrings);
        listView = findViewById(R.id.destination_search_results);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener((adapterView, view, position, l) -> {

            loadingDialog.startLoadingDialog();

            // using handler class to set time delay methods
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                // Get values for selected list item
                String selectedDestination = listView.getItemAtPosition(position).toString(); // Stop num + name
                TreeSet<String> tripIds = destinationsToTripIds.get(selectedDestination);
                HashMap<String, String> stopIdTripIdMap = mapStopIdsToTripIds(tripIds); // Map stop ids for route's trips
                HashMap<String, String> stops = mapStopStringsToStopIds(stopIdTripIdMap.keySet()); // Map stop strings to ids

                // Create an intent to pass data
                Intent newIntent = new Intent(view.getContext(), SearchStopActivity.class);

                // Create a bundle to store data
                bundle.putSerializable("stopIdTripIdMap", stopIdTripIdMap);
                bundle.putSerializable("stops", stops);
                newIntent.putExtra("bundle", bundle);

                // Close loading dialog
                loadingDialog.dismissDialog();

                // Go to stop selection
                startActivity(newIntent);
            }, 0);
        });
    }

    /**
     * Map stop ids to their trip ids.
     * @param tripIds a TreeSet
     * @return a Hashmap of stop ids mapped to trip ids
     */
    public HashMap<String, String> mapStopIdsToTripIds(TreeSet<String> tripIds) {
        HashMap<String, String> stopTripMap = new HashMap<>();
        try {
            //Read the file
            InputStream inputStream = getBaseContext().getResources().openRawResource(R.raw.stop_times);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            bufferedReader.readLine();
            String line = bufferedReader.readLine();

            while (line != null) {
                //Use string.split to load a string array with the values from the line of
                //the file, using a comma as the delimiter
                String[] tokenize = line.split(",");
                String tripId = tokenize[0];
                String stopId = tokenize[3];

                if (tripIds.contains(tripId)) {
                    stopTripMap.put(stopId, tripId);
                }

                line = bufferedReader.readLine();
            }
            //Close reader, catch errors
            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopTripMap;
    }

    /**
     * Return a TreeSet of stop names from a set of stop ids.
     * @param stopIds a TreeSet
     * @return a TreeSet of stop names
     */
    public HashMap<String, String> mapStopStringsToStopIds(Set<String> stopIds) {
        HashMap<String, String> stopStringStopIdMap = new HashMap<>();

        try {
            //Read the file
            InputStream inputStream = getBaseContext().getResources().openRawResource(R.raw.stops);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            bufferedReader.readLine();
            String line = bufferedReader.readLine();

            while (line != null) {
                //Use string.split to load a string array with the values from the line of
                //the file, using a comma as the delimiter
                String[] tokenize = line.split(",");
                String stopId = tokenize[0];
                String stopCode = tokenize[1];
                String stopName = tokenize[2];

                if (stopIds.contains(stopId)) {
                    String[] stopNameSplit = stopName.split("@ ");
                    String stopString = String.format("%s: %s", stopCode, stopNameSplit[1]);
                    stopStringStopIdMap.put(stopString, stopId);
                }
                line = bufferedReader.readLine();
            }
            //Close reader, catch errors
            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stopStringStopIdMap;
    }
}

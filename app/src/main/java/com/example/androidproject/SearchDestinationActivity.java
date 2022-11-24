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
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class SearchDestinationActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> arrayAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_destination);

        final LoadingDialog loadingDialog = new LoadingDialog(SearchDestinationActivity.this);
        TransLinkTextFileParsing transLinkTextFileParsing = new TransLinkTextFileParsing(SearchDestinationActivity.this);

        // Get the intent
        Intent intent = getIntent();

        // Option 2: Extract data from bundle
        Bundle bundle = intent.getBundleExtra("bundle");
        String selectedRoute = bundle.getString("route");
        HashMap<String, TreeSet<String>> destinationsToTripIds =
                (HashMap<String, TreeSet<String>>) bundle.getSerializable("destinationsToTripIds");

        // Set Action Bar to show selected route
        Objects.requireNonNull(getSupportActionBar()).setTitle(selectedRoute);

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
                HashMap<String, String> stopIdTripIdMap = transLinkTextFileParsing.mapStopIdsToTripIds(tripIds); // Map stop ids for route's trips
                HashMap<String, String> stops = transLinkTextFileParsing.mapStopStringsToStopIds(stopIdTripIdMap.keySet()); // Map stop strings to ids

                // Create an intent to pass data
                Intent newIntent = new Intent(view.getContext(), SearchStopActivity.class);

                // Create a bundle to store data
                bundle.putString("selectedDestination", selectedDestination);
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
}

package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

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
        TransLinkTextFileParser transLinkTextFileParser = new TransLinkTextFileParser(SearchDestinationActivity.this);

        // Get the intent
        Intent intent = getIntent();

        // Extract data from bundle
        Bundle bundle = intent.getBundleExtra("bundle");
        String routeString = bundle.getString("routeString");
        String routeNum = bundle.getString("routeNum");
        ArrayList<String> tripIdsArrayList = bundle.getStringArrayList("tripIdsArrayList");
        HashMap<String, TreeSet<String>> destinationsToTripIds =
                (HashMap<String, TreeSet<String>>) bundle.getSerializable("destinationsToTripIds");

        // Set Action Bar to show selected route
        Objects.requireNonNull(getSupportActionBar()).setTitle(routeString);

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
                TreeSet<String> tripIdsSet = destinationsToTripIds.get(selectedDestination);
                HashMap<String, String> stopIdTripIdMap = transLinkTextFileParser.mapStopIdsToTripIds(tripIdsSet); // Map stop ids for route's trips
                HashMap<String, String> stops = transLinkTextFileParser.mapStopStringsToStopIds(stopIdTripIdMap.keySet()); // Map stop strings to ids

                // Create an intent to pass data
                Intent newIntent = new Intent(view.getContext(), SearchStopActivity.class);

                // Create a bundle to store data
                Bundle newBundle = new Bundle();
                newBundle.putString("destinationString", selectedDestination);
                newBundle.putString("routeString", routeString);
                newBundle.putString("routeNum", routeNum);
                newBundle.putStringArrayList("tripIdsArrayList", tripIdsArrayList);
                newBundle.putSerializable("stopIdTripIdMap", stopIdTripIdMap);
                newBundle.putSerializable("stopStringStopIdMap", stops);
                newIntent.putExtra("bundle", newBundle);

                // Close loading dialog
                loadingDialog.dismissDialog();

                // Go to stop selection
                startActivity(newIntent);
            }, 0);
        });
    }
}

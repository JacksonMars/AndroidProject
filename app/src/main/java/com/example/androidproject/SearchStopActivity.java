package com.example.androidproject;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

public class SearchStopActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> arrayAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_stop);

        final LoadingDialog loadingDialog = new LoadingDialog(SearchStopActivity.this);

        // Get the intent
        Intent intent = getIntent();

        // Option 2: Extract data from bundle
        Bundle bundle = intent.getBundleExtra("bundle");
        String routeNum = bundle.getString("routeNum");
        HashMap<String, String> stopIdTripIdMap = (HashMap<String, String>) bundle.getSerializable("stopIdTripIdMap");
        HashMap<String, String> stops = (HashMap<String, String>) bundle.getSerializable("stops");

        // Put stop strings in ArrayList, sort
        Set<String> stopsKeySet = stops.keySet();
        ArrayList<String> stopStrings = new ArrayList<>(stopsKeySet);
        stopStrings.sort(Comparator.naturalOrder());

        //Create adapter for routes listview
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stopStrings);
        listView = findViewById(R.id.stop_search_results);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener((adapterView, view, position, l) -> {

            loadingDialog.startLoadingDialog();

            // using handler class to set time delay methods
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                // Get values for selected list item
                String selectedStop = listView.getItemAtPosition(position).toString(); // Stop num + name
                String[] selectedStopComponents = selectedStop.split(": ");
                String selectedStopNum = selectedStopComponents[0]; // Stop num
                String selectedStopName = selectedStopComponents[1]; // Stop name
                String stopId = stops.get(selectedStop); // Stop id
                String tripId = stopIdTripIdMap.get(stopId); // Trip id

                RealTimeBusInfoService realTimeBusInfoService = new RealTimeBusInfoService(SearchStopActivity.this);
                realTimeBusInfoService.getActiveBusses(selectedStopNum, routeNum, new RealTimeBusInfoService.VolleyResponseListener() {
                    @Override
                    public void onError(String message) {
                        Toast.makeText(SearchStopActivity.this, "Error", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(ArrayList<HashMap<String, String>> activeBusses) {
                        Toast.makeText(SearchStopActivity.this, activeBusses.toString(), Toast.LENGTH_LONG).show();
                        bundle.putSerializable("activeBusses", activeBusses);
                        // Create an intent to pass data
                        Intent newIntent = new Intent(view.getContext(), MainActivity.class);

                        // Put new info in bundle
                        bundle.putString("tripId", tripId);

                        // Create a bundle to store data
                        newIntent.putExtra("bundle", bundle);

                        // Close loading dialog
                        loadingDialog.dismissDialog();

                        // Go to Map
                        startActivity(newIntent);
                    }
                });
            }, 0);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) SearchStopActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (menuItem != null) {
            searchView = (SearchView) menuItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(SearchStopActivity.this.getComponentName()));
            searchView.setQueryHint("Enter a stop number");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    arrayAdapter.getFilter().filter(newText);
                    return false;
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }
}

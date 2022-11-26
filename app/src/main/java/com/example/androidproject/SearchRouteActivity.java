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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class SearchRouteActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> arrayAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_route);

        final LoadingDialog loadingDialog = new LoadingDialog(SearchRouteActivity.this);
        TransLinkTextFileParser transLinkTextFileParser = new TransLinkTextFileParser(SearchRouteActivity.this);

        // Set action bar text
        Objects.requireNonNull(getSupportActionBar()).setTitle("Please select a route");

        //Create adapter for route strings in listview
        HashMap<String, String> routeStringsToRouteIds = transLinkTextFileParser.mapRouteStringsToRouteIds();
        ArrayList<String> routeStrings = new ArrayList<>(routeStringsToRouteIds.keySet());
        routeStrings.sort(Comparator.naturalOrder());

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, routeStrings);
        listView = findViewById(R.id.route_search_results);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener((adapterView, view, position, l) -> {

            loadingDialog.startLoadingDialog();

            // using handler class to set time delay methods
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                // Get values for selected list item
                String selectedRoute = listView.getItemAtPosition(position).toString(); // Route num + name
                String[] selectedRouteComponents = selectedRoute.split(": ");
                String routeNum = selectedRouteComponents[0];
                String routeId = routeStringsToRouteIds.get(selectedRoute); // Route id
                HashMap<String, TreeSet<String>> destinationsToTripIds =
                        transLinkTextFileParser.mapRouteDestinationsToTripIds(routeId, routeNum); // Trip ids for this route
                TreeSet<String> tripIds = transLinkTextFileParser.getTripIds(routeId); // Trip ids for this route
                ArrayList<String> tripIdsArrayList = new ArrayList<>(tripIds);

                // Create an intent to pass data
                Intent intent = new Intent(view.getContext(), SearchDestinationActivity.class);

                // Create a bundle to store data
                Bundle bundle = new Bundle();
                bundle.putString("route", selectedRoute);
                bundle.putString("routeNum", routeNum);
                bundle.putSerializable("destinationsToTripIds", destinationsToTripIds);
                bundle.putStringArrayList("tripIds", tripIdsArrayList);
                intent.putExtra("bundle", bundle);

                // Close loading dialog
                loadingDialog.dismissDialog();

                // Go to stop selection
                startActivity(intent);
            }, 0);
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Set menu to search menu
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) SearchRouteActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (menuItem != null) {
            searchView = (SearchView) menuItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(SearchRouteActivity.this.getComponentName()));
            searchView.setQueryHint("Enter a bus number");
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

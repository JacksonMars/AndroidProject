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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.TreeSet;

public class SearchActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> arrayAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ArrayList<String> routes = getRoutes();
        final LoadingDialog loadingDialog = new LoadingDialog(SearchActivity.this);

        //Create adapter for routes listview
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, routes);
        listView = findViewById(R.id.route_search_results);

        //TODO CVERMA -- Make the terminus stations the subtitle of the list items
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener((adapterView, view, position, l) -> {
            loadingDialog.startLoadingDialog();

            // using handler class to set time delay methods
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Get value for selected list item
                    String selectedRoute = listView.getItemAtPosition(position).toString();
                    ArrayList<String> stops = getStopsForRoute(selectedRoute);

                    // Create an intent to pass data
                    Intent intent = new Intent(view.getContext(), SearchStopActivity.class);

                    // Create a bundle to store data
                    Bundle bundle = new Bundle();
                    bundle.putString("route", selectedRoute);
                    bundle.putStringArrayList("stops", stops);
                    intent.putExtra("bundle", bundle);

                    // Close loading dialog
                    loadingDialog.dismissDialog();

                    // Go to Map
                    startActivity(intent);
                }
            }, 0);
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) SearchActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (menuItem != null) {
            searchView = (SearchView) menuItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(SearchActivity.this.getComponentName()));
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

    /**
     * Return an ArrayList containing all TransLink routes.
     * @return an ArrayList containing all TransLink routes
     */
    public ArrayList<String> getRoutes() {
        //Create ArrayList with routes from .txt file
        ArrayList<String> routes = new ArrayList<>();
        try {
            //Read the file
            InputStream inputStream = getBaseContext().getResources().openRawResource(R.raw.routes);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            bufferedReader.readLine();
            String line = bufferedReader.readLine();

            while (line != null) {
                //Use string.split to load a string array with the values from the line of
                //the file, using a comma as the delimiter
                String[] tokenize = line.split(",");
                String busNum = tokenize[2];
                String routeName = tokenize[3];

                //Some bus routes have no bus number
                String searchOption;
                if (busNum.isEmpty()) {
                    searchOption = String.format("%s", routeName);
                } else {
                    searchOption = String.format("%s: %s", busNum, routeName);
                }
                routes.add(searchOption);

                line = bufferedReader.readLine();
            }
            //Close reader, catch errors
            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Sort routes
        routes.sort(Comparator.naturalOrder());
        return routes;
    }

    /**
     * Extracts route id from a given route name.
     * @param route a String
     * @return route id as a String
     */
    public String getRouteId(String route) {
        String selectedRouteName;
        if (route.contains(":")) {
            selectedRouteName = route.split(": ")[1];
        } else {
            selectedRouteName = route;
        }

        try {
            //Read the file
            InputStream inputStream = getBaseContext().getResources().openRawResource(R.raw.routes);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            bufferedReader.readLine();
            String line = bufferedReader.readLine();

            while (line != null) {
                //Use string.split to load a string array with the values from the line of
                //the file, using a comma as the delimiter
                String[] tokenize = line.split(",");
                String routeId = tokenize[0];
                String routeName = tokenize[3];

                if (Objects.equals(selectedRouteName, routeName)) {
                    Toast.makeText(this.getBaseContext(), routeId, Toast.LENGTH_LONG).show();
                    return routeId;
                }

                line = bufferedReader.readLine();
            }
            //Close reader, catch errors
            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this.getBaseContext(), "No id obtained", Toast.LENGTH_LONG).show();
        return "";
    }

    /**
     * Returns a TreeSet of trip ids for a given route.
     * @param routeId a String
     * @return TreeSet of trip ids
     */
    public TreeSet<String> getTripIds(String routeId) {
        TreeSet<String> tripIds = new TreeSet<>();
        try {
            //Read the file
            InputStream inputStream = getBaseContext().getResources().openRawResource(R.raw.trips);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            bufferedReader.readLine();
            String line = bufferedReader.readLine();

            while (line != null) {
                //Use string.split to load a string array with the values from the line of
                //the file, using a comma as the delimiter
                String[] tokenize = line.split(",");
                String fileRouteId = tokenize[0];
                String tripId = tokenize[2];

                if (Objects.equals(routeId, fileRouteId)) {
                    tripIds.add(tripId);
                }

                line = bufferedReader.readLine();
            }
            //Close reader, catch errors
            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tripIds;
    }

    /**
     * Get stop ids corresponding to given trip ids.
     * @param tripIds a TreeSet
     * @return a TreeSet containing stop ids
     */
    public TreeSet<String> getStopIds(TreeSet<String> tripIds) {
        TreeSet<String> stopIds = new TreeSet<>();
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
                    stopIds.add(stopId);
                }

                line = bufferedReader.readLine();
            }
            //Close reader, catch errors
            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopIds;
    }

    /**
     * Return a TreeSet of stop names from a set of stop ids.
     * @param stopIds a TreeSet
     * @return a TreeSet of stop names
     */
    public ArrayList<String> getStops(TreeSet<String> stopIds) {
        //Create ArrayList with routes from .txt file
        ArrayList<String> stops = new ArrayList<>();
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
                    String stop = String.format("%s: %s", stopCode, stopName);
                    stops.add(stop);
                }
                line = bufferedReader.readLine();
            }
            //Close reader, catch errors
            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Sort routes
        stops.sort(Comparator.naturalOrder());
        return stops;
    }

    public ArrayList<String> getStopsForRoute (String route) {
        String routeId = getRouteId(route);
        TreeSet<String> tripIds = getTripIds(routeId);
        TreeSet<String> stopIds = getStopIds(tripIds);
        return getStops(stopIds);
    }
}

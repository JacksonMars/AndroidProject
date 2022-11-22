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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class SearchRouteActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> arrayAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_route);

        HashMap<String, String> routes = mapRouteStringsToRouteIds();
        Set<String> routesKeySet = routes.keySet();
        ArrayList<String> routeStrings = new ArrayList<>(routesKeySet);
        routeStrings.sort(Comparator.naturalOrder());
        final LoadingDialog loadingDialog = new LoadingDialog(SearchRouteActivity.this);

        //Create adapter for routes listview
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
                String routeId = routes.get(selectedRoute); // Route id
                HashMap<String, TreeSet<String>> destinationsToTripIds = mapRouteDestinationsToTripIds(routeId, routeNum); // Trip ids for this route

                // Create an intent to pass data
                Intent intent = new Intent(view.getContext(), SearchDestinationActivity.class);

                // Create a bundle to store data
                Bundle bundle = new Bundle();
                bundle.putString("route", selectedRoute);
                bundle.putString("routeNum", routeNum);
                bundle.putSerializable("destinationsToTripIds", destinationsToTripIds);
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

    /**
     * Map route strings (route number + route name) to their route ids.
     * @return HashMap of route strings mapped to route ids
     */
    public HashMap<String, String> mapRouteStringsToRouteIds() {
        HashMap<String, String> routes = new HashMap<>();

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
                String busNum = tokenize[2];
                String routeName = tokenize[3];

                //Trains have no bus number, do not add these to routes
                String routeString;
                if (!busNum.isEmpty()) {
                    routeString = String.format("%s: %s", busNum, routeName);
                    routes.put(routeString, routeId);
                }

                line = bufferedReader.readLine();
            }
            //Close reader, catch errors
            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return routes;
    }

    /**
     * Return true if strNum is numerical, else false.
     * @param str a String
     * @return true if strNum is numerical, else false
     */
    public static boolean isNumeric(String str) {
        try {
            double parseDouble = Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Reformat destination strings to follow 'To destination' format
     * @param destination a String
     * @return formatted destination String
     */
    public static String formatDestination(String destination, String routeNum) {
        String[] destinationSplit = destination.split(" ");
        List<String> destinationArrayList = new ArrayList<>(Arrays.asList(destinationSplit));
        if (isNumeric(destinationSplit[0])) {
            destinationArrayList.remove(0);
        }
        destinationArrayList.remove("To");
        destinationArrayList.remove("to");
        destinationArrayList.remove(routeNum);
        destinationArrayList.add(0, "To");
        destinationSplit = destinationArrayList.toArray(new String[0]);

        return String.join(" ", destinationSplit);
    }

    public HashMap<String, TreeSet<String>> mapRouteDestinationsToTripIds(String selectedRouteId, String routeNum) {
        HashMap<String, TreeSet<String>> destinations = new HashMap<>();

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
                String destination = tokenize[3];

                if (Objects.equals(selectedRouteId, fileRouteId)) {
                    destination = formatDestination(destination, routeNum);
                    if (!destinations.containsKey(destination)) {
                        destinations.put(destination, new TreeSet<>());
                    }
                    Objects.requireNonNull(destinations.get(destination)).add(tripId);
                }

                line = bufferedReader.readLine();
            }
            //Close reader, catch errors
            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return destinations;
    }
}

package com.example.androidproject;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;

public class SearchPage extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> arrayAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ArrayList<String> routes = getRoutes();

        //Create adapter for routes listview
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, routes);
        listView = findViewById(R.id.search_results);

        //TODO CVERMA -- Make the terminus stations the subtitle of the list items
        listView.setAdapter(arrayAdapter);

        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // Override the onItemSelected method defined in AdapterView.OnItemSelectedListener
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the String representation of the selected item
                String textCountry = parent.getItemAtPosition(position).toString();
                // Show Toast message when a country is selected
                Toast.makeText(view.getContext(), textCountry + " is selected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) SearchPage.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (menuItem != null) {
            searchView = (SearchView) menuItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(SearchPage.this.getComponentName()));
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
}

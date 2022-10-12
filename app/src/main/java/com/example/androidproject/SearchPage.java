package com.example.androidproject;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SearchPage extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Create ArrayList with routes from .txt file
        ArrayList<String> routes = new ArrayList<>();
        try {
            InputStream inputStream = getBaseContext().getResources().openRawResource(R.raw.routes);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = bufferedReader.readLine();
            routes.add(line);
            while (line != null) {
                // make use of the line read
                line = bufferedReader.readLine();
                routes.add(line);
            }
            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Create adapter for country spinner
        ArrayAdapter<String> data = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, routes);
        // Set the layout style for the drop down menu
        data.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Attach the ArrayAdapter to spinner
        Spinner spinnerCountries = findViewById(R.id.spinnerCountries);
        spinnerCountries.setAdapter(data);
        // Create a listener for selecting an item in the spinner
        spinnerCountries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // Override the onItemSelected method defined in AdapterView.OnItemSelectedListener
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the String representation of the selected item
                String textCountry = parent.getItemAtPosition(position).toString();
                // Show Toast message when a country is selected
                Toast.makeText(view.getContext(), textCountry + " is selected", Toast.LENGTH_SHORT).show();
            }

            // Override the onNothingSelected method defined in AdapterView.OnItemSelectedListener
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // unused
            }
        });
    }
}

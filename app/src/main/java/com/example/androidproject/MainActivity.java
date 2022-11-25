package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);

        loadingDialog.startLoadingDialog();

        // using handler class to set time delay methods
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            // Get information from search bundle
            Intent intent = getIntent();
            Bundle bundle = intent.getBundleExtra("bundle");
            String route = bundle.getString("route");
            String tripId = bundle.getString("tripId");
            ArrayList<String> tripIdsArrayList = bundle.getStringArrayList("tripIds");
            TextView textView = findViewById(R.id.route);
            textView.setText(route);

            // Set bottom navbar
            BottomNavigationView bottomNavigationView= findViewById(R.id.bottom_navigation);

            ArrayList<String> stopIds = getStopIds(tripId);
            ArrayList<String> allCoordinates = allStopCoordinates(stopIds);

            // Set map fragment as default in frame layout
            bundle.putStringArrayList("coordinates", allCoordinates);
            Bundle newBundle = new Bundle();
            newBundle.putStringArrayList("coordinates", allCoordinates);
            newBundle.putString("stopName", bundle.getString("stopName"));

            Fragment mapFragment = new MapFragment();
            mapFragment.setArguments(bundle);
            getSupportFragmentManager()
                    .beginTransaction().replace(R.id.fragment_container, mapFragment)
                    .commit();

            // Close loading dialog
            loadingDialog.dismissDialog();

            // Switch statements for navbar
            bottomNavigationView.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.map:
                        selectedFragment = mapFragment;
                        break;
                    case R.id.schedule:
                        selectedFragment = new ScheduleFragment();
                        Bundle scheduleBundle = new Bundle();

                        String chosenStopNumber = ((MapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container)).getCurrentStopNumber();
                        String chosenStopName = ((MapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container)).getCurrentStopName();
                        scheduleBundle.putString("stopNumber", chosenStopNumber);
                        scheduleBundle.putString("stopName", chosenStopName);
                        scheduleBundle.putStringArrayList("tripIds", tripIdsArrayList);
                        selectedFragment.setArguments(scheduleBundle);
                        break;
                    case R.id.info:
                        selectedFragment = new BusInfoFragment();
                        break;
                    default:
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                }
                return false;
            });
        }, 0);
    }

    public ArrayList<String> getStopIds(String intendedTripId) {
        ArrayList<String> allStops = new ArrayList<>();
        try {
            InputStream inputStream = getBaseContext().getResources().openRawResource(R.raw.stop_times);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            bufferedReader.readLine();
            String line = bufferedReader.readLine();

            while(line != null) {
                String[] tokenize = line.split(",");
                String routeId = tokenize[0];
                if(routeId.equals(intendedTripId)) {
                    allStops.add(tokenize[1] + "," + tokenize[3]);
                }
                line = bufferedReader.readLine();
            }

            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        allStops.sort(Comparator.naturalOrder());

        return allStops;
    }

    public String getStopCoordinates(String intendedStopId) {
        String coordinates = null;
        try {
            InputStream inputStream = getBaseContext().getResources().openRawResource(R.raw.stops);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            bufferedReader.readLine();
            String line = bufferedReader.readLine();

            while(line != null && coordinates == null) {
                String[] tokenize = line.split(",");
                String stopId = tokenize[0];
                if(stopId.equals(intendedStopId)) {
                    coordinates = tokenize[1] + ": " + tokenize[2] + "/" + tokenize[4] + "," + tokenize[5];
                }
                line = bufferedReader.readLine();
            }

            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return coordinates;
    }

    public ArrayList<String> allStopCoordinates(ArrayList<String> stopIds) {
        ArrayList<String> allCoordinates = new ArrayList<>();
        for(int i = 0; i < stopIds.size(); i++) {
            allCoordinates.add(getStopCoordinates(stopIds.get(i).split(",")[1]));
        }
        return allCoordinates;
    }
}
package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);
        TransLinkTextFileParser transLinkTextFileParser = new TransLinkTextFileParser(MainActivity.this);

        loadingDialog.startLoadingDialog();

        // using handler class to set time delay methods
        Handler handler = new Handler();
        handler.postDelayed(() -> {

            // Get information from search bundle
            Intent intent = getIntent();
            Bundle bundle = intent.getBundleExtra("bundle");
            String routeString = bundle.getString("routeString");
            String tripId = bundle.getString("tripId");
            ArrayList<String> tripIdsArrayList = bundle.getStringArrayList("tripIdsArrayList");

            // Set map header to route name
            TextView textView = findViewById(R.id.route);
            textView.setText(routeString);

            // Set bottom navbar
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

            ArrayList<String> stopIds = transLinkTextFileParser.getStopIds(tripId);
            ArrayList<String> allCoordinates = transLinkTextFileParser.allStopCoordinates(stopIds);

            bundle.putStringArrayList("coordinates", allCoordinates);
            Bundle mapBundle = new Bundle();
            mapBundle.putStringArrayList("coordinates", allCoordinates);
            mapBundle.putString("stopName", bundle.getString("stopName"));

            // Set map fragment as default in frame layout
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

                        String chosenStopNumber = ((MapFragment) Objects.requireNonNull(getSupportFragmentManager()
                                .findFragmentById(R.id.fragment_container)))
                                .getCurrentStopNumber();
                        String chosenStopName = ((MapFragment) Objects.requireNonNull(getSupportFragmentManager()
                                .findFragmentById(R.id.fragment_container)))
                                .getCurrentStopName();
                        scheduleBundle.putString("stopNumber", chosenStopNumber);
                        scheduleBundle.putString("stopName", chosenStopName);
                        scheduleBundle.putStringArrayList("tripIdsArrayList", tripIdsArrayList);
                        selectedFragment.setArguments(scheduleBundle);
                        break;
                    default:
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
                return false;
            });
        }, 0);
    }
}
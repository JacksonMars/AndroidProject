package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get information from search bundle
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        String route = bundle.getString("route");
        TextView textView = findViewById(R.id.route);
        textView.setText(route);

        // Set bottom navbar
        BottomNavigationView bottomNavigationView= findViewById(R.id.bottom_navigation);

        // Set map fragment as default in frame layout
        Fragment mapFragment = new MapFragment();
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.fragment_container, mapFragment)
                .commit();

        // Switch statements for navbar
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.map:
                    selectedFragment = mapFragment;
                    break;
                case R.id.schedule:
                    selectedFragment = new ScheduleFragment();
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
    }
}
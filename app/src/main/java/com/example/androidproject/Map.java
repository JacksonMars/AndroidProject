package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class Map extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.map_layout);

        // Get the intent
        Intent intentPrev = getIntent();

        // Extract data from intent
        Bundle bundlePrev = intentPrev.getBundleExtra("bundle");
        String route = bundlePrev.getString("route");

        //Set screen header as selected route
        TextView textView = findViewById(R.id.textView);
        textView.setText(route);

        Fragment mapFragment = new MapFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainerView, mapFragment);
        transaction.commit();
    }
}

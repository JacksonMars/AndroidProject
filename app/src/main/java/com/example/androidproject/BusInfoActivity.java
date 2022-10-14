package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class BusInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_info_activity);

        // Get the intent
        Intent intentPrev = getIntent();

        // Extract data from intent
        Bundle bundlePrev = intentPrev.getBundleExtra("bundle");
        String route = bundlePrev.getString("route");

        //Set screen header as selected route
        TextView textView = findViewById(R.id.title);
        textView.setText(route);

        Fragment busInfoFragment = new BusInfoFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.busInfoContainer, busInfoFragment);
//        fragmentTransaction.commit();


        Button btnToInfo = findViewById(R.id.btnToInfo);
        btnToInfo.setOnClickListener(view -> {
            FragmentTransaction fragmentTransaction1 = fragmentManager.beginTransaction();
            fragmentTransaction1.replace(R.id.busInfoContainer, busInfoFragment);
            fragmentTransaction1.commit();
        });

    }

}
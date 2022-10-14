package com.example.androidproject;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class BusInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_info_activity);

        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment busInfoFragment = new BusInfoFragment();
        Fragment scheduleFragment = new ScheduleFragment();

        Fragment mapFragment = new MapFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.busInfoContainer, mapFragment);
        transaction.commit();

//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.busInfoContainer, busInfoFragment);
//        fragmentTransaction.commit();

        Button btnToInfo = findViewById(R.id.btnToInfo);
        btnToInfo.setOnClickListener(view -> {
            FragmentTransaction fragmentTransaction1 = fragmentManager.beginTransaction();
            fragmentTransaction1.replace(R.id.busInfoContainer, busInfoFragment);
            fragmentTransaction1.commit();
        });

        Button btnMap = findViewById(R.id.btnToMap);
        btnMap.setOnClickListener(view -> {
            FragmentTransaction fragmentTransaction1 = fragmentManager.beginTransaction();
            fragmentTransaction1.replace(R.id.busInfoContainer, mapFragment);
            fragmentTransaction1.commit();
        });

        Button btnSchedule = findViewById(R.id.btnToSchedule);
        btnSchedule.setOnClickListener(view -> {
            FragmentTransaction fragmentTransaction1 = fragmentManager.beginTransaction();
            fragmentTransaction1.replace(R.id.busInfoContainer, scheduleFragment);
            fragmentTransaction1.commit();
        });
    }

}
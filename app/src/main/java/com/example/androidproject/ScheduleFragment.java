package com.example.androidproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class ScheduleFragment extends Fragment {
    public ScheduleFragment() {
        super(R.layout.schedule_fragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // This gets the stop number from the MainActivity. You can probably use it to get details
        Bundle bundle = this.getArguments();
        String stopNumber = bundle.getString("stopNumber");

        View view = inflater.inflate(R.layout.schedule_fragment, container, false);
        return view;
    }
}

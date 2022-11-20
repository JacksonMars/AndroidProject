package com.example.androidproject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ScheduleFragment extends Fragment {
    public ScheduleFragment() {
        super(R.layout.schedule_fragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // This gets the stop number from the MainActivity. You can probably use it to get details
        Bundle bundle = this.getArguments();
        String stopCode = bundle.getString("stopNumber");
        String stopId = getStopId(stopCode);

        View view = inflater.inflate(R.layout.schedule_fragment, container, false);
        return view;
    }

    public String getStopId(String stopCode) {
        String coordinates = null;
        try {
            InputStream inputStream = getActivity().getBaseContext().getResources().openRawResource(R.raw.stops);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            bufferedReader.readLine();
            String line = bufferedReader.readLine();

            while(line != null && coordinates == null) {
                String[] tokenize = line.split(",");
                String nextStopCode = tokenize[1];
                if(nextStopCode.equals(stopCode)) {
                    bufferedReader.close();
                    inputStream.close();
                    return tokenize[0];
                }
                line = bufferedReader.readLine();
            }

            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

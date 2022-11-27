package com.example.androidproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

        String stopName = bundle.getString("stopName");
        ArrayList<String> tripIdsArrayList = bundle.getStringArrayList("tripIdsArrayList");

        // Build View for Fragment
        View view = inflater.inflate(R.layout.schedule_fragment, container, false);

        // Edit Values in Fragment
        TextView stopNumberTextView = (TextView) view.findViewById(R.id.scheduleFragStopNumber);
        TextView stopNameTextView = (TextView) view.findViewById(R.id.scheduleFragStopName);
        stopNameTextView.setText(stopName);
//        stopNumberTextView.setText("StopNumber: " + stopCode);

        HashMap<String, String> stops = getScheduleForStopId(stopId, tripIdsArrayList);
        ArrayAdapter<String> arrayAdapter;

        Set<String> stopsKeySet = stops.keySet();
        ArrayList<String> stopsStrings = new ArrayList<>(stopsKeySet);
        stopsStrings.sort(Comparator.naturalOrder());

        // DateTime Implementation
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String currentTime = dtf.format(now);

        Collections.sort(stopsStrings, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.compareTo(currentTime);
            }
        });


        String nextBusTime = stopsStrings.get(0);

        long eta = findEta(currentTime, nextBusTime);
        String etaString = String.valueOf(eta);


        TextView etaValue = (TextView) view.findViewById(R.id.scheduleFragNextBusEtaValue);
        etaValue.setText(etaString);

        // Create Adapter for Schedule Time in ListView
        arrayAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1 , stopsStrings);
        ListView listView = (ListView) view.findViewById(R.id.scheduleFragStopTimes);
        listView.setAdapter(arrayAdapter);


        return view;
    }

    // Function to print difference in
    // time start_date and end_date
    static long findEta(String start_date,
                        String end_date)
    {
        // SimpleDateFormat converts the
        // string format to date object
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        // Try Class
        try {

            // parse method is used to parse
            // the text from a string to
            // produce the date
            Date d1 = sdf.parse(start_date);
            Date d2 = sdf.parse(end_date);

            // Calculate time difference
            // in milliseconds
            long difference_In_Time
                    = d2.getTime() - d1.getTime();

            // Calucalte time difference in seconds,
            // minutes, hours, years, and days
            long difference_In_Seconds
                    = TimeUnit.MILLISECONDS
                    .toSeconds(difference_In_Time)
                    % 60;

            long difference_In_Minutes
                    = TimeUnit
                    .MILLISECONDS
                    .toMinutes(difference_In_Time)
                    % 60;

            long difference_In_Hours
                    = TimeUnit
                    .MILLISECONDS
                    .toHours(difference_In_Time)
                    % 24;

            long difference_In_Days
                    = TimeUnit
                    .MILLISECONDS
                    .toDays(difference_In_Time)
                    % 365;

            long difference_In_Years
                    = TimeUnit
                    .MILLISECONDS
                    .toDays(difference_In_Time)
                    / 365l;

            // Print the date difference in
            // years, in days, in hours, in
            // minutes, and in seconds
            System.out.print(
                    "Difference"
                            + " between two dates is: ");

            // Print result
            System.out.println(
                    difference_In_Years
                            + " years, "
                            + difference_In_Days
                            + " days, "
                            + difference_In_Hours
                            + " hours, "
                            + difference_In_Minutes
                            + " minutes, "
                            + difference_In_Seconds
                            + " seconds");

            return difference_In_Minutes;
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * Map route strings (route number + route name) to their route ids.
     * @return HashMap of route strings mapped to route ids
     */
    public HashMap<String, String> getScheduleForStopId(String stopIdString,
                                                        ArrayList<String> tripIdsArrayList) {

        HashMap<String, String> routes = new HashMap<>();

        try {
            //Read the file
            InputStream inputStream = getActivity().getBaseContext().getResources().openRawResource(R.raw.stop_times);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            bufferedReader.readLine();
            String line = bufferedReader.readLine();

            while (line != null) {
                //Use string.split to load a string array with the values from the line of
                //the file, using a comma as the delimiter
                String[] tokenize = line.split(",");
                String stopId = tokenize[3];
                String arrivalTime = tokenize[1];
                String tripId = tokenize[0];

                // The issue is that because this is parsing from stop_times, it also contains
                // northbound/southbound info. For example it will return 6:24:39 -> 13339465,
                // which is not in the stop_id given.
                // Potentially use set, rather than ArrayList?

                // Service_id is the attribute for weekday/weekend/holiday
                if (stopIdString.equals(stopId)) {
                    if (tripIdsArrayList.contains(tripId)) {
                        String routeString;
                        routeString = String.format("Arrival Time: %s", arrivalTime);
                        routes.put(arrivalTime, tripId);
                    }
                }

                line = bufferedReader.readLine();
            }
            //Close reader, catch errors
            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return routes;
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

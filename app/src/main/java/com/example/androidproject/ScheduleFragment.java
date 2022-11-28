package com.example.androidproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
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
        TextView stopNameTextView = view.findViewById(R.id.scheduleFragStopName);
        stopNameTextView.setText(stopName);

        HashMap<String, String> stops = getScheduleForStopId(stopId, tripIdsArrayList);
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

        ArrayList<String> finalSchedule = formatSchedule(stopsStrings, currentTime);

        String nextBusTime = stopsStrings.get(0);
        long eta = findEta(currentTime, nextBusTime);
        String etaString = String.valueOf(eta);

        TextView etaValue = view.findViewById(R.id.scheduleFragNextBusEtaValue);
        etaValue.setText(etaString);

        // Create Adapter for Schedule Time in ListView
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1 , finalSchedule);
        ListView listView = view.findViewById(R.id.scheduleFragStopTimes);
        listView.setAdapter(arrayAdapter);

        return view;
    }

    /*
    Source: https://www.geeksforgeeks.org/find-the-duration-of-difference-between-two-dates-in-java/
    */
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

            long difference_In_Minutes
                    = TimeUnit
                    .MILLISECONDS
                    .toMinutes(difference_In_Time)
                    % 60;

            return difference_In_Minutes;
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Function to format and order schedule for display
    public ArrayList<String> formatSchedule(ArrayList<String> originalSchedule, String currentTime) {

        ArrayList<String> newSchedule = new ArrayList<>();
        String[] timeToken = currentTime.split(":");
        String currentHourString = timeToken[0];
        String currentMinuteString = timeToken[1];

        int currentHour = Integer.parseInt(currentHourString);
        int currentMinute = Integer.parseInt(currentMinuteString);

        // Modulo Time
        for (String s : originalSchedule) {
            String[] tokenize = s.split(":");
            String hour = tokenize[0];
            String firstHourCharacter = Character.toString(hour.charAt(0));
            String minute = tokenize[1];

            if (firstHourCharacter.equals(" ")) {
                hour = hour.replace(" ", "0");
            }

            int hourNumber = Integer.parseInt(hour);
            int moduloHour = hourNumber % 24;

            String moduloHourString = Integer.toString(moduloHour);
            String newHour;

            if (moduloHourString.length() == 1) {
                newHour = "0" + moduloHour + ":" + minute;
            } else {
                newHour = moduloHour + ":" + minute;
            }

            newSchedule.add(newHour);
        }

        ArrayList<String> pastSchedule = new ArrayList<>();
        ArrayList<String> upcomingSchedule = new ArrayList<>();

        // Split past schedule and upcoming schedule
        for (String time : newSchedule) {
            String[] timeSplit = time.split(":");
            String hourSplitString = timeSplit[0];
            String minuteSplitString = timeSplit[1];
            int hourSplit = Integer.parseInt(hourSplitString);
            int minuteSplit = Integer.parseInt(minuteSplitString);

            if (hourSplit < currentHour ) {
                if (hourSplit > 3) {
                    pastSchedule.add(time);
                } else {
                    upcomingSchedule.add(time);
                }
            } else if (hourSplit == currentHour) {
                if (minuteSplit < currentMinute) {
                    pastSchedule.add(time);
                } else {
                    upcomingSchedule.add(time);
                }
            } else {
                upcomingSchedule.add(time);
            }
        }

        Collections.sort(pastSchedule);

        ArrayList<String> finalSchedule = new ArrayList<>();
        finalSchedule.addAll(upcomingSchedule);
        finalSchedule.addAll(pastSchedule);

        return finalSchedule;
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
                String[] tokenize = line.split(",");
                String stopId = tokenize[3];
                String arrivalTime = tokenize[1];
                String tripId = tokenize[0];

                if (stopIdString.equals(stopId)) {
                    if (tripIdsArrayList.contains(tripId)) {
                        String routeString;
                        routeString = String.format("Arrival Time: %s", arrivalTime);
                        routes.put(arrivalTime, tripId);
                    }
                }

                line = bufferedReader.readLine();
            }
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

package com.example.androidproject;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;

import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RealTimeBusInfoService {
    public static final String API_KEY = "AdKqzDnSKgbtZdyDroOt";

    Context context;
    ArrayList<HashMap<String, String>> activeBussesList;

    public RealTimeBusInfoService(Context context) {
        this.context = context;
    }

    public interface VolleyResponseListener {
        void onError(String message);

        void onResponse(ArrayList<HashMap<String, String>> activeBusses);
    }

    public void getActiveBusses(String stopNum, String routeNum, VolleyResponseListener volleyResponseListener) {
        // Instantiate the RequestQueue.
        String url = MessageFormat.format(
                "https://api.translink.ca/rttiapi/v1/buses?apikey={0}&stopNo={1}&routeNo={2}",
                        API_KEY,
                        stopNum,
                        routeNum);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
            response -> {
                activeBussesList = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    HashMap<String, String> activeBusMap = new HashMap<>();
                    try {
                        JSONObject activeBusJSON = response.getJSONObject(i);

                        String busNum = activeBusJSON.getString("VehicleNo");
                        String latitude = String.valueOf(activeBusJSON.getDouble("Latitude"));
                        String longitude = String.valueOf(activeBusJSON.getDouble("Longitude"));

                        activeBusMap.put("busNum", busNum);
                        activeBusMap.put("latitude", latitude);
                        activeBusMap.put("longitude", longitude);
                        activeBussesList.add(activeBusMap);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                volleyResponseListener.onResponse(activeBussesList);
            },
            error -> {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                volleyResponseListener.onError("Error");
            }) {
                /**
                 * Ensures the API returns a JSON array instead of XML.
                 */
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("accept", "application/json");
                    headers.put("content-type", "application/json; charset=utf-8");
                    return headers;
                }
            };

        // Add the request to the RequestQueue.
        RequestSingleton.getInstance(context).addToRequestQueue(request);
    }

}

package com.example.androidproject;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Fragment housing the map and related functions
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private ArrayList<String> stopCoordinates;
    private ArrayList<HashMap<String, String>> activeBusses;
    private String currentStop = null;

    /**
     * Code to be executed when the MapFragment is created. Gets data from Bundle and prepares a map
     * @param inflater inflater for the layout
     * @param container container for views
     * @param savedInstanceState a Bundle
     * @return view of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        assert bundle != null;
        stopCoordinates = bundle.getStringArrayList("coordinates");
        activeBusses = (ArrayList<HashMap<String, String>>) bundle.getSerializable("activeBusses");

        if(currentStop == null) {
            currentStop = bundle.getString("stopName");
        }

        // Initialize view
        View view=inflater.inflate(R.layout.map_fragment, container, false);

        // Initialize map fragment
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapAPI);

        // Async map
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // Return view
        return view;
    }

    /**
     * Code to be executed when the Map object is ready. Parses data, draws the route, and places
     * pins in current bus locations if the information is available
     * @param googleMap a GoogleMap object
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        String[] startInfo = stopCoordinates.get(0).split("/");
        String[] startCoordinates = startInfo[1].split(",");
        LatLng start = new LatLng(Double.parseDouble(startCoordinates[0]), Double.parseDouble(startCoordinates[1]));
        List<LatLng> path = new ArrayList<>();

        // Draw route
        for(int i = 0; i < stopCoordinates.size(); i+=9) {
            if(i <= stopCoordinates.size() - 10) {
                addToRoute(stopCoordinates.subList(i, i + 10), path, googleMap);
            } else {
                addToRoute(stopCoordinates.subList(i, stopCoordinates.size()), path, googleMap);
            }
        }

        if(path.size() > 0) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
            mMap.addPolyline(opts);
        }
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                currentStop = marker.getTitle();
                return false;
            }
        });

        markActiveBusses(googleMap);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 13));
    }

    /**
     * Add segments to a route. Up to nine stops can be traversed at once
     * @param stopsInfo a List of Strings containing info about each stop, such as latitude,
     *                  longitude, stop number, and stop name
     * @param path a List of LatLng objects. If a non-null path is passed, this method will add
     *             to the path that is already there
     * @param googleMap a GoogleMap object
     */
    private void addToRoute(List<String> stopsInfo, List<LatLng> path, GoogleMap googleMap) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyA7B8cf57CuID9r8WHJCkmWa4P-cCGQNMo")
                .build();

        ArrayList<String> allCoordinates = getStopCoordinates(stopsInfo);
        ArrayList<String> allNames = getStopNames(stopsInfo);

        List<String> waypoints = allCoordinates.subList(1, allCoordinates.size());
        DirectionsApiRequest req = DirectionsApi.getDirections(context, allCoordinates.get(0), allCoordinates.get(allCoordinates.size() - 1)).waypoints(waypoints.toArray(new String[0]));

        for(int i = 0; i < allCoordinates.size(); i++) {
            String[] stopCoordinatesString = allCoordinates.get(i).split(",");
            LatLng stopCoordinates = new LatLng(Double.parseDouble(stopCoordinatesString[0]), Double.parseDouble(stopCoordinatesString[1]));

            BitmapDrawable bitmapDraw = (BitmapDrawable) getResources().getDrawable(R.drawable.translink_logo);

            Bitmap b = bitmapDraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, 40, 40, false);
            MarkerOptions newMarker = new MarkerOptions().position(stopCoordinates).title(allNames.get(i)).icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            googleMap.addMarker(newMarker);

            if(Objects.equals(allNames.get(i), currentStop)) {
                currentStop = newMarker.getTitle();
            }
        }

        try {
            DirectionsResult res = req.await();

            if(res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if(route.legs != null) {
                    for(int i = 0; i < route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if(leg.steps != null) {
                            for(int j = 0; j < leg.steps.length; j++) {
                                DirectionsStep step = leg.steps[j];

                                if(step.steps != null && step.steps.length > 0) {
                                    for(int k = 0; k < step.steps.length; k++) {
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;

                                        if(points1 != null) {
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for(com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if(points != null) {
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for(com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch(Exception e) {
            Log.i("ERROR", e.getLocalizedMessage());
        }
    }

    /**
     * Mark active busses on the map using a pin. Buses are marked based on their current latitude
     * and longitude
     * @param googleMap a GoogleMap object
     */
    public void markActiveBusses(@NonNull GoogleMap googleMap) {
        if (!(activeBusses == null)) {
            // Mark active busses
            for (int i = 0; i < activeBusses.size(); i++) {
                HashMap<String, String> activeBus = activeBusses.get(i);
                String busNum = activeBus.get("busNum");
                double latitude = Double.parseDouble(Objects.requireNonNull(activeBus.get("latitude")));
                double longitude = Double.parseDouble(Objects.requireNonNull(activeBus.get("longitude")));

                LatLng busCoordinates = new LatLng(latitude, longitude);
                googleMap.addMarker(new MarkerOptions()
                        .position(busCoordinates)
                        .title("Bus No. " + busNum)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_clipart)));
            }
        }
    }

    /**
     * Get all stop names from a List containing stop info
     * @param stopsInfo a List of Strings containing stop info
     * @return an ArrayList of all stop names
     */
    private ArrayList<String> getStopNames(List<String> stopsInfo) {
        ArrayList<String> allNames = new ArrayList<>();
        for(int i = 0; i < stopsInfo.size(); i++) {
            String stopInfo = stopsInfo.get(i);
            String stopName = stopInfo.split("/")[0];
            allNames.add(stopName);
        }
        return allNames;
    }

    /**
     * Get coordinates for a List of stop info
     * @param stopsInfo a List containing Strings of stop info
     * @return latitude and longitude coordinates for each stop
     */
    private ArrayList<String> getStopCoordinates(List<String> stopsInfo) {
        ArrayList<String> allCoordinates = new ArrayList<>();
        for(int i = 0; i < stopsInfo.size(); i++) {
            String stopInfo = stopsInfo.get(i);
            String stopCoords = stopInfo.split("/")[1];
            allCoordinates.add(stopCoords);
        }
        return allCoordinates;
    }

    /**
     * Get the stop number of the user's currently selected stop
     * @return current stop number
     */
    public String getCurrentStopNumber() {
        return currentStop.split("/")[0].split(":")[0];
    }

    /**
     * Get the stop name of the user's currently selected stop
     * @return current stop name
     */
    public String getCurrentStopName() {
        return currentStop.split("/")[0].split(":")[1];
    }
}
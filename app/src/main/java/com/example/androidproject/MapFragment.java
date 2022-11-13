package com.example.androidproject;

import android.graphics.Color;
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
import com.google.android.gms.maps.model.LatLng;
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
import com.google.maps.model.TravelMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private ArrayList<String> stopCoordinates;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        stopCoordinates = bundle.getStringArrayList("coordinates");

        // Initialize view
        View view=inflater.inflate(R.layout.map_fragment, container, false);

        // Initialize map fragment
        mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapAPI));

        // Async map
        assert mapFragment != null;
        // Async map
        mapFragment.getMapAsync(this);

        // Return view
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng start = null;
        List<LatLng> path = new ArrayList<>();

        for(int i = 0; i < stopCoordinates.size() - 1; i++) {
            String startCoords = stopCoordinates.get(i);
            String endCoords = stopCoordinates.get(i + 1);
            if(start == null) {
                String[] tokenized = startCoords.split(",");
                start = new LatLng(Float.parseFloat(tokenized[0]), Float.parseFloat(tokenized[1]));
            }
            addToRoute(startCoords, endCoords, path);
        }

        if(path.size() > 0) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
            mMap.addPolyline(opts);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 13));
    }

    private void addToRoute(String location1, String location2, List<LatLng> path) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyA7B8cf57CuID9r8WHJCkmWa4P-cCGQNMo")
                .build();
        DirectionsApiRequest req = DirectionsApi.getDirections(context, location1, location2).mode(TravelMode.TRANSIT);

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
}
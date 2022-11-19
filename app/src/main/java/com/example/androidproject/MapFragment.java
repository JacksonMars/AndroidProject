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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private ArrayList<String> stopCoordinates;
    private String chosenStop;
    private String currentStop = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        stopCoordinates = bundle.getStringArrayList("coordinates");
        chosenStop = bundle.getString("stopName");

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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        String[] startInfo = stopCoordinates.get(0).split(":");
        String[] startCoordinates = startInfo[1].split(",");
        LatLng start = new LatLng(Double.parseDouble(startCoordinates[0]), Double.parseDouble(startCoordinates[1]));
        List<LatLng> path = new ArrayList<>();

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
//                BitmapDrawable bitmapDraw = (BitmapDrawable) getResources().getDrawable(R.drawable.green_square);
//                Bitmap b = bitmapDraw.getBitmap();
//                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 25, 25, false);
//                marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                currentStop = marker.getTitle();
                return true;
            }
        });

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 13));
    }

    private void addToRoute(List<String> stopsInfo, List<LatLng> path, GoogleMap googleMap) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("API")
                .build();

        ArrayList<String> allCoordinates = getStopCoordinates(stopsInfo);
        ArrayList<String> allNames = getStopNames(stopsInfo);

        List<String> waypoints = allCoordinates.subList(1, allCoordinates.size());
        DirectionsApiRequest req = DirectionsApi.getDirections(context, allCoordinates.get(0), allCoordinates.get(allCoordinates.size() - 1)).waypoints(waypoints.toArray(new String[0]));

        for(int i = 0; i < allCoordinates.size(); i++) {
            String[] stopCoordinatesString = allCoordinates.get(i).split(",");
            LatLng stopCoordinates = new LatLng(Double.parseDouble(stopCoordinatesString[0]), Double.parseDouble(stopCoordinatesString[1]));

            BitmapDrawable bitmapDraw = null;
            if(Objects.equals(allNames.get(i), chosenStop)) {
                bitmapDraw = (BitmapDrawable) getResources().getDrawable(R.drawable.green_square);
            } else {
                bitmapDraw = (BitmapDrawable) getResources().getDrawable(R.drawable.red_square);
            }

            Bitmap b = bitmapDraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, 25, 25, false);
            MarkerOptions newMarker = new MarkerOptions().position(stopCoordinates).title(allNames.get(i)).icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            googleMap.addMarker(newMarker);

            if(Objects.equals(allNames.get(i), chosenStop)) {
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

    private ArrayList<String> getStopNames(List<String> stopsInfo) {
        ArrayList<String> allNames = new ArrayList<>();
        for(int i = 0; i < stopsInfo.size(); i++) {
            String stopInfo = stopsInfo.get(i);
            String stopName = stopInfo.split(":")[0];
            allNames.add(stopName);
        }
        return allNames;
    }

    private ArrayList<String> getStopCoordinates(List<String> stopsInfo) {
        ArrayList<String> allCoordinates = new ArrayList<>();
        for(int i = 0; i < stopsInfo.size(); i++) {
            String stopInfo = stopsInfo.get(i);
            String stopCoords = stopInfo.split(":")[1];
            allCoordinates.add(stopCoords);
        }
        return allCoordinates;
    }
}
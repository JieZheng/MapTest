package com.jalen.maptest;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<PlaceLikelihoodBuffer> {
    private final static String TAG = MapsActivity.class.getName();
    public final static int PERMISSIONS_REQUEST_LOCATION = 1;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    List<LatLng> mPositions = new ArrayList<LatLng>();
    boolean mIsRouteFind = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (permissions.length == 1 &&
                    permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                // Permission was denied. Display an error message.
                Utils.showOkSnackbar(getWindow().getDecorView().getRootView(), R.string.no_permission_location);
            }
        }
    }

    void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
            {
                //Explain to the user why we need to read location permission
                Utils.showOkSnackbar(getWindow().getDecorView().getRootView(), R.string.location_permission_rationale);
            } else {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        requestLocationPermission();
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    void updateMap() {
        try {
            Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //move camera to current location
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));

            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
//                    Log.d(TAG, "PlaceDetectionApi onResult:" + likelyPlaces);
        Marker marker = null;
        for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
            if(mPositions.size() >= 20) {
                return;
            }
            LatLng latLng = placeLikelihood.getPlace().getLatLng();
            Log.d(TAG, "PlaceDetectionApi place:" + latLng);
            if (Math.abs(latLng.latitude) < 0.1 && Math.abs(latLng.longitude) < 0.1 ) {
//                            Log.d(TAG, "PlaceDetectionApi place:" + placeLikelihood.getPlace());
                continue;
            }
            mPositions.add(latLng);
            marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(placeLikelihood.getPlace().getName().toString())
                    .snippet(placeLikelihood.getPlace().getAddress().toString()));

            Log.d(TAG, String.format("PlaceDetectionApi Place '%s' has likelihood: %g",
                    placeLikelihood.getPlace().getName(),
                    placeLikelihood.getLikelihood()));
            // Set a listener for marker click.
            mMap.setOnMarkerClickListener(MapsActivity.this);
        }
        if (marker != null){
            marker.showInfoWindow();
        }
        placeLikelihoods.release();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(mIsRouteFind) {
           return false;
        } else{
            mIsRouteFind = true;
        }
        RouteFinder routeFinder = new RouteFinder(mPositions);
        Stack<LatLng> route = routeFinder.searchRoute(marker.getPosition());

        // Instantiates a new Polyline object and adds points to define a polyline
        PolylineOptions polylineOptions = new PolylineOptions();
        while (route.size() > 0) {
            LatLng position = route.pop();
            polylineOptions.add(position);
        }

        // draw polyline
        Polyline polyline = mMap.addPolyline(polylineOptions);
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Map onConnected");
        updateMap();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Map onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Map onConnectionFailed");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

}

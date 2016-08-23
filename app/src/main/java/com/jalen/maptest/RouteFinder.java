package com.jalen.maptest;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by zj on 22/08/2016.
 * Search for the route with given start position
 */
public class RouteFinder {
    private final static String TAG = RouteFinder.class.getName();
    Stack<LatLng> mRoute = new Stack<LatLng>();
    List<LatLng> mPositions = new ArrayList<LatLng>();
    LatLng mStartPosition;

    public RouteFinder(List<LatLng> latLngs) {
        mPositions.addAll(latLngs);
//        Log.d(TAG, "mPositions"+ mPositions);
    }

    /**
     * Search the route by given start position
     * @param startPosition
     * @return
     */
    public Stack<LatLng> searchRoute(LatLng startPosition) {
        mStartPosition = startPosition;

        //check validation of startposition
        if(!mPositions.remove(startPosition)) {
            return null;
        }
        mRoute.push(startPosition);

        LatLng currentPosition = startPosition;
        while(mPositions.size() > 0) {
            LatLng closestPosition = searchClosestPosition(currentPosition);
            mPositions.remove(closestPosition);
            mRoute.push(closestPosition);
            currentPosition = closestPosition;
        }
        mRoute.push(startPosition);

        return mRoute;
    }

    /**
     * Search for the closest position with given position
     * @param currentPosition
     * @return
     */
    LatLng searchClosestPosition(LatLng currentPosition) {
        LatLng closestPosition = null;
        float closestDistance = -1;

        for(LatLng position : mPositions) {
            float distance = getDistanceBetween(currentPosition, position);
            if (closestPosition == null || distance < closestDistance) {
                closestPosition = position;
                closestDistance = distance;
            }
        }

        return closestPosition;
    }

    /**
     * Calculate the distance between two LatLngs, a and b
     * @param a
     * @param b
     * @return
     */
    float getDistanceBetween(LatLng a, LatLng b) {
        float disdance = 0;
        Location locationA = new Location("a");
        locationA.setLatitude(a.latitude);
        locationA.setLongitude(a.longitude);
        Location locationB = new Location("b");
        locationB.setLatitude(b.latitude);
        locationB.setLongitude(b.longitude);

        return locationA.distanceTo(locationB);
    }

}

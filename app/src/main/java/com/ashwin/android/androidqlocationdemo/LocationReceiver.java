package com.ashwin.android.androidqlocationdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import java.util.List;

public class LocationReceiver extends BroadcastReceiver {
    private static final String TAG = "LUBroadcastReceiver";

    static final String ACTION_PROCESS_UPDATES = "com.ashwin.locationdemo.action.PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    LocationResultHelper locationResultHelper = new LocationResultHelper(context, locations);

                    // Save the location data to SharedPreferences.
                    locationResultHelper.saveResults();

                    // Show notification with the location data.
                    locationResultHelper.showNotification();

                    Log.i(TAG, LocationResultHelper.getSavedLocationResult(context));
                } else {
                    Log.e(TAG, "Location result is null");
                }
            }
        }
    }
}

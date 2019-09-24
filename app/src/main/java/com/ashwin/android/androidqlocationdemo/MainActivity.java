package com.ashwin.android.androidqlocationdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Q-Location";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private Button coarseButton, fineButton, backgroundButton, startFgButton, startBgButton, stopFgButton, stopBgButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.d(TAG, "location: (lat: " + location.getLatitude() + ", lng: " + location.getLongitude() + ")");
                }
            }
        };

        coarseButton = (Button) findViewById(R.id.coarse_button);
        fineButton = (Button) findViewById(R.id.fine_button);
        backgroundButton = (Button) findViewById(R.id.background_button);
        startFgButton = (Button) findViewById(R.id.start_fg_button);
        startBgButton = (Button) findViewById(R.id.start_bg_button);
        stopFgButton = (Button) findViewById(R.id.stop_fg_button);
        stopBgButton = (Button) findViewById(R.id.stop_bg_button);

        coarseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPermissionRequest(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
        });

        fineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        backgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startPermissionRequest(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION});
                } else {
                    Toast.makeText(MainActivity.this, "Android version is lower than 29 (Q)", Toast.LENGTH_LONG).show();
                }
            }
        });

        startFgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startFgLocationUpdates();
            }
        });

        startBgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBgLocationUpdates();
            }
        });

        stopFgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopFgLocationUpdates();
            }
        });

        stopBgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopBgLocationUpdates();
            }
        });
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, LocationReceiver.class);
        intent.setAction(LocationReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private boolean checkPermission(String permission) {
        int permissionState = ActivityCompat.checkSelfPermission(this, permission);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startPermissionRequest(String permission) {
        startPermissionRequest(new String[]{permission});
    }

    private void startPermissionRequest(String[] permissions) {
        ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
                Toast.makeText(MainActivity.this, "Permission cancelled", Toast.LENGTH_LONG).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted");
                Toast.makeText(MainActivity.this, "Permission granted", Toast.LENGTH_LONG).show();
            } else {
                Log.i(TAG, "Permission denied");
                Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10_000);
        locationRequest.setFastestInterval(5_000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void startFgLocationUpdates() {
        Log.i(TAG, "Starting fg location updates");
        fusedLocationClient.requestLocationUpdates(getLocationRequest(), locationCallback, Looper.getMainLooper());
    }

    private void startBgLocationUpdates() {
        Log.i(TAG, "Starting bg location updates");
        fusedLocationClient.requestLocationUpdates(getLocationRequest(), getPendingIntent());
    }

    private void stopFgLocationUpdates() {
        Log.i(TAG, "Stopping fg location updates");
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void stopBgLocationUpdates() {
        Log.i(TAG, "Stopping bg location updates");
        fusedLocationClient.removeLocationUpdates(getPendingIntent());
    }
}

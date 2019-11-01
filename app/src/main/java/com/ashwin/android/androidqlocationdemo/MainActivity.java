package com.ashwin.android.androidqlocationdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Q-Location";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final long LOCATION_INTERVAL = 10_000L;
    private static final long FASTEST_LOCATION_INTERVAL = LOCATION_INTERVAL / 2;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private Button coarseButton, fineButton, backgroundButton, updateNowButton, startFgButton, startBgButton, stopFgButton, stopBgButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.e(TAG, "Location result is null");
                    return;
                }

                List<Location> locations = locationResult.getLocations();
                LocationReceiver.showNotification(getApplicationContext(), LocationReceiver.getLocationsTitle("Fg"), LocationReceiver.getLocationsMessage(locations));
            }
        };

        coarseButton = (Button) findViewById(R.id.coarse_button);
        fineButton = (Button) findViewById(R.id.fine_button);
        backgroundButton = (Button) findViewById(R.id.background_button);
        updateNowButton = (Button) findViewById(R.id.update_now_button);
        startFgButton = (Button) findViewById(R.id.start_fg_button);
        startBgButton = (Button) findViewById(R.id.start_bg_button);
        stopFgButton = (Button) findViewById(R.id.stop_fg_button);
        stopBgButton = (Button) findViewById(R.id.stop_bg_button);

        coarseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPermissionRequest(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION});
            }
        });

        fineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPermissionRequest(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
            }
        });

        backgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startPermissionRequest(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION});
                } else {
                    Toast.makeText(MainActivity.this, "Android version is lower than 29 (Q)", Toast.LENGTH_LONG).show();
                }
            }
        });

        updateNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLocation(getApplicationContext());
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

    private void updateLocation(final Context context) {
        FusedLocationProviderClient fusedLocationProvider = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationProvider.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Log.d(TAG, "Updated location: latitude: " + latitude + ", longitude: " + longitude);

                    List<Location> locations = new ArrayList<>();
                    locations.add(location);

                    LocationReceiver.showNotification(context, LocationReceiver.getLocationsTitle("Manual"), LocationReceiver.getLocationsMessage(locations));
                } else {
                    Log.e(TAG, "Location is NULL");
                }
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

    private void startPermissionRequest(String[] permissions) {
        ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
                Toast.makeText(MainActivity.this, "Permission cancelled", Toast.LENGTH_LONG).show();
            } else {
                boolean allPermissionsGranted = false;

                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = true;
                    } else {
                        allPermissionsGranted = false;
                        break;
                    }
                }

                if (allPermissionsGranted) {
                    Log.i(TAG, "Permission granted");
                    Toast.makeText(MainActivity.this, "Permission granted", Toast.LENGTH_LONG).show();
                } else {
                    Log.i(TAG, "Permission denied");
                    Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_LOCATION_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void startFgLocationUpdates() {
        Log.i(TAG, "Starting fg location updates");
        Task<Void> requestTask = fusedLocationClient.requestLocationUpdates(getLocationRequest(), locationCallback, Looper.getMainLooper());
        requestTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    showToast("Fg location updates started successfully");
                } else {
                    showToast("Fg location updates start failed");
                    showToast(String.valueOf(task.getException()));
                }
            }
        });
    }

    private void startBgLocationUpdates() {
        Log.i(TAG, "Starting bg location updates");
        Task<Void> requestTask = fusedLocationClient.requestLocationUpdates(getLocationRequest(), getPendingIntent());
        requestTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    showToast("Bg location updates started successfully");
                } else {
                    showToast("Bg location updates start failed");
                    showToast(String.valueOf(task.getException()));
                }
            }
        });
    }

    private void stopFgLocationUpdates() {
        Log.i(TAG, "Stopping fg location updates");
        Task<Void> requestTask = fusedLocationClient.removeLocationUpdates(locationCallback);
        requestTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    showToast("Fg location updates stopped successfully");
                } else {
                    showToast("Fg location updates stop failed");
                    showToast(String.valueOf(task.getException()));
                }
            }
        });
    }

    private void stopBgLocationUpdates() {
        Log.i(TAG, "Stopping bg location updates");
        Task<Void> requestTask = fusedLocationClient.removeLocationUpdates(getPendingIntent());
        requestTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    showToast("Bg location updates stopped successfully");
                } else {
                    showToast("Bg location updates stop failed");
                    showToast(String.valueOf(task.getException()));
                }
            }
        });
    }

    private void showToast(String msg) {
        Log.d(TAG, msg);
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}

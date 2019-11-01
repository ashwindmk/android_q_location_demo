package com.ashwin.android.androidqlocationdemo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocationReceiver extends BroadcastReceiver {
    private static final String TAG = "location-updates";
    static final String ACTION_PROCESS_UPDATES = "com.ashwin.locationdemo.action.PROCESS_UPDATES";
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    private static final String CHANNEL_ID = "channel_01";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Log.w(TAG, "Received location update");

            if (ACTION_PROCESS_UPDATES.equals(intent.getAction())) {
                String title = getLocationsTitle("Bg");
                String message = getLocationsMessage(intent);
                showNotification(context, title, message);
            }
        }
    }

    static String getLocationsTitle(String source) {
        String timestamp = formatter.format(new Date());
        return timestamp + ": " + source + " location update";
    }

    private String getLocationsMessage(Intent intent) {
        LocationResult result = LocationResult.extractResult(intent);
        if (result != null) {
            List<Location> locations = result.getLocations();
            if (locations != null) {
                return getLocationsMessage(locations);
            }
        }
        return "null";
    }

    static String getLocationsMessage(List<Location> locations) {
        ArrayList<String> locationsMessageList = new ArrayList<>();
        for (Location location : locations) {
            String locationMessage = "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
            locationsMessageList.add(locationMessage);
        }
        return TextUtils.join(", ", locationsMessageList);
    }

    static void showNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(mChannel);
        }

        Intent notificationIntent = new Intent(context, MainActivity.class);

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(notificationPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        builder.setAutoCancel(true);

        notificationManager.notify(title.hashCode(), builder.build());
    }
}

package com.mia.smartlight.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.mia.smartlight.R;
import com.mia.smartlight.activity.NotificationActivity;
import com.mia.smartlight.model.Attribute;
import com.mia.smartlight.model.NexaUnit;

import java.util.ArrayList;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {

    private List<NexaUnit> units;
    static final String CHANNEL_ID = "GeofenceTransitionsIntentService";

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d("Class", "GeofenceTransitionsIntentService");
        Log.d("Intent", "Handling intent " + intent.toString());

        final GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e("GEO ERROR", String.valueOf(geofencingEvent.getErrorCode()));
            return;
        }
        Log.d("Geofence", "Triggering geofences: " + geofencingEvent.getTriggeringGeofences().toString());
        Log.d("Geofence", "Triggering location: " + geofencingEvent.getTriggeringLocation().toString());

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        Log.d("Geofence", "Triggering transition: " + geofenceTransition);

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            checkIfLampsOn();
        }
    }

    private void checkIfLampsOn() {

        NexaService.getInstance(this).getUnits(this, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                Toast.makeText(GeofenceTransitionsIntentService.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Object response) {

                units = new ArrayList<>();
                List<NexaUnit> list = (List<NexaUnit>) response;

                for (NexaUnit unit : list) {
                    checkLamp(unit.getId());
                }

            }
        });
    }

    private void checkLamp(int id) {
        NexaService.getInstance(this).getUnit(id, this, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                Log.e("Error", message);
                Toast.makeText(GeofenceTransitionsIntentService.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Object response) {
                NexaUnit unit = (NexaUnit) response;

                for (Attribute a : unit.getAttributes()) {
                    if (a.getName().equalsIgnoreCase("state") && a.getValue().equalsIgnoreCase("on") && units.isEmpty()) {
                        units.add(unit);
                        createNotification();
                    }
                }
            }
        });
    }

    private void createNotification() {

        Intent resultIntent = new Intent(this, NotificationActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_lightbulb_outline_dark)
                        .setContentTitle("Wait!")
                        .setAutoCancel(true)
                        .setContentText("Lamps are still turned on");

        builder.setContentIntent(resultPendingIntent);

        int notificationId = 1;

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(notificationId, builder.build());

        Log.d("Notification", "Created notification from GeofenceTransitionsIntentService");
    }
}

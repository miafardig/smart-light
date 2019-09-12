package com.mia.smartlight.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.mia.smartlight.R;
import com.mia.smartlight.activity.MainActivity;
import com.mia.smartlight.model.Attribute;
import com.mia.smartlight.model.NexaUnit;
import com.mia.smartlight.model.UserConfig;

import java.util.List;

public class ConnectivityIntentService extends JobIntentService {

    private UserConfig userConfig;
    private NexaUnit unit;
    private List<Attribute> attributes;
    static final int JOB_ID = 1000;
    static final String CHANNEL_ID = "ConnectivityIntentService";

    public ConnectivityIntentService() {
        super();
    }

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, ConnectivityIntentService.class, JOB_ID, work);
    }
    @Override
    public void onHandleWork(@NonNull Intent intent) {
        Log.d("Class", "ConnectivityIntentService");
        Log.d("Intent", "Handling intent " + intent.toString());

        userConfig = UserConfig.getInstance(this);

        createNotification();
        int id = userConfig.getLampToSetOnWifi().getId();
        if (id != 0) {
            turnUnitOn(intent, id);
        } else {
            Toast.makeText(this, "No lamp id is provided. Please set this in 'Settings'", Toast.LENGTH_LONG).show();
        }
    }

    private void turnUnitOn(final Intent intent, final int id) {

        NexaService.getInstance(this).getUnit(id, this, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                if (message.equals(NexaService.FLAG)) {
                    Toast.makeText(ConnectivityIntentService.this, "No server URL provided, please set this in 'Settings'", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ConnectivityIntentService.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
                    //needs to retry, since there may not be a stable internet connection during first call
                    turnUnitOn(intent, id);
                }
            }

            @Override
            public void onResponse(Object response) {

                unit = (NexaUnit) response;
                attributes = unit.getAttributes();

                for (Attribute a : attributes) {
                    //only turn the lamp on if it isn't turned on already
                    if (a.getName().equalsIgnoreCase("state") && a.getValue().equalsIgnoreCase("off")) {
                        invokeActionOnServer(intent, id);
                    } else if(a.getName().equalsIgnoreCase("state") && a.getValue().equalsIgnoreCase("on")) {
                        Log.d("Request", "Unit is already on");
                    }
                }
            }
        });
    }

    private void invokeActionOnServer(final Intent intent, final int id) {
        NexaService.getInstance(this).invokeAction(id, "on", this, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                if (message.equals(NexaService.FLAG)) {
                    Toast.makeText(ConnectivityIntentService.this, "No server URL provided, please set this in 'Settings'", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ConnectivityIntentService.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
                    //needs to retry, since there may not be a stable internet connection during first call
                    invokeActionOnServer(intent, id);
                }
            }

            @Override
            public void onResponse(Object response) {
                Log.d("Request", "Turned on " + unit.getName());
                Toast.makeText(ConnectivityIntentService.this, "Turned on " + unit.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNotification() {

        Intent resultIntent = new Intent(this, MainActivity.class);

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
                        .setContentTitle("Welcome home!")
                        .setAutoCancel(true)
                        .setContentText("Open SmartLight");

        builder.setContentIntent(resultPendingIntent);

        int notificationId = 2;

        NotificationManager manager =
                (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

        manager.notify(notificationId, builder.build());
    }
}

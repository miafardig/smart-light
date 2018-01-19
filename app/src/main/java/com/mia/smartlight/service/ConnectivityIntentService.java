package com.mia.smartlight.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.mia.smartlight.R;
import com.mia.smartlight.activity.MainActivity;
import com.mia.smartlight.model.Attribute;
import com.mia.smartlight.model.NexaUnit;
import com.mia.smartlight.model.UserConfig;
import com.mia.smartlight.receiver.NetworkChangeReceiver;

import java.util.List;

public class ConnectivityIntentService extends IntentService {

    private UserConfig userConfig;
    private NexaUnit unit;
    private List<Attribute> attributes;

    public ConnectivityIntentService(String name) {
        super(name);
    }

    public ConnectivityIntentService() {
        super("ConnectivityIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d("Class", "ConnectivityIntentService");
        Log.d("Intent", "Handling intent " + intent.toString());

        userConfig = UserConfig.getInstance(this);

        createNotification();
        int id = userConfig.getLampToSetOnWifi().getId();
        if (id != 0) {
            turnUnitOn(intent, id);
        } else {
            Toast.makeText(this, "No lamp id is provided. Please set this in 'Settings'", Toast.LENGTH_LONG).show();
            NetworkChangeReceiver.completeWakefulIntent(intent);
        }
    }

    private void turnUnitOn(final Intent intent, final int id) {

        NexaService.getInstance(this).getUnit(id, this, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                if (message.equals(NexaService.FLAG)) {
                    Toast.makeText(ConnectivityIntentService.this, "No server URL provided, please set this in 'Settings'", Toast.LENGTH_LONG).show();
                    NetworkChangeReceiver.completeWakefulIntent(intent);
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
                        NetworkChangeReceiver.completeWakefulIntent(intent);
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
                    NetworkChangeReceiver.completeWakefulIntent(intent);
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
                NetworkChangeReceiver.completeWakefulIntent(intent);
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
                new NotificationCompat.Builder(this)
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

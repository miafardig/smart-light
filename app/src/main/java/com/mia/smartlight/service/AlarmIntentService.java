package com.mia.smartlight.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.mia.smartlight.model.UserConfig;
import com.mia.smartlight.receiver.AlarmReceiver;

public class AlarmIntentService extends IntentService {

    private UserConfig userConfig;

    public AlarmIntentService() {
        super("AlarmService");
    }

    public AlarmIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d("Class", "AlarmIntentService");
        Log.d("Intent", "Handling intent " + intent.toString());

        userConfig = UserConfig.getInstance(this);

        invokeActionOnServer(intent);
    }

    private void invokeActionOnServer(final Intent intent) {

        final int id = userConfig.getLampToSetOnAlarm().getId();

        if (id != 0) {
            NexaService.getInstance(this).invokeAction(id, "on", this, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    if (message.equals(NexaService.FLAG)) {
                        Toast.makeText(AlarmIntentService.this, "No server URL provided, please set this in 'Settings'", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(AlarmIntentService.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
                    }
                    AlarmReceiver.completeWakefulIntent(intent);
                }

                @Override
                public void onResponse(Object response) {
                    Log.d("Request", "Turned on unit " + id);
                    Toast.makeText(AlarmIntentService.this, "Wake up!", Toast.LENGTH_SHORT).show();
                    AlarmReceiver.completeWakefulIntent(intent);
                }
            });
        }
    }
}

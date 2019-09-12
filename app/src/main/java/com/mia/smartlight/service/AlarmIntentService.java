package com.mia.smartlight.service;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import com.mia.smartlight.model.UserConfig;

public class AlarmIntentService extends JobIntentService {

    private UserConfig userConfig;
    static final int JOB_ID = 1001;

    public AlarmIntentService() {
        super();
    }

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, AlarmIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d("Class", "AlarmIntentService");
        Log.d("Intent", "Handling intent " + intent.toString());

        userConfig = UserConfig.getInstance(this);

        invokeActionOnServer();
    }

    private void invokeActionOnServer() {

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
                }

                @Override
                public void onResponse(Object response) {
                    Log.d("Request", "Turned on unit " + id);
                    Toast.makeText(AlarmIntentService.this, "Wake up!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

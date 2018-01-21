package com.mia.smartlight.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.mia.smartlight.service.AlarmIntentService;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("Class", "AlarmReceiver");
        Log.d("Intent", "Handling intent " + intent.toString());
        Log.d("Intent", "Sending intent to AlarmIntentService");

        Intent service = new Intent(context, AlarmIntentService.class);
        WakefulBroadcastReceiver.startWakefulService(context, service);

    }
}

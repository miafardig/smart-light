package com.mia.smartlight.receiver;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import com.mia.smartlight.service.ConnectivityIntentService;
import com.mia.smartlight.model.UserConfig;

import java.util.Calendar;


public class NetworkChangeReceiver extends WakefulBroadcastReceiver {

    private UserConfig userConfig;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("Class", "NetworkChangeReceiver");
        Log.d("Intent", "Handling intent " + intent.getAction());

        userConfig = UserConfig.getInstance(context);

        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minHour = 8;
        int maxHour = 22;

        if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION) && hour > minHour && hour < maxHour) {
            if (intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE) == SupplicantState.COMPLETED) {

                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                if (wifiInfo != null) {
                    boolean connected = checkConnectedToDesiredWifi(context, wifiInfo);
                    if (connected) {
                        Log.d("Intent", "Sending intent to ConnectivityIntentService");
                        Intent service = new Intent(context, ConnectivityIntentService.class);
                        WakefulBroadcastReceiver.startWakefulService(context, service);
                    }
                }
            }
        }

    }

    private boolean checkConnectedToDesiredWifi(Context context, WifiInfo wifiInfo) {
        boolean connected = false;

        String routerAddress = userConfig.getRouterAddress();
        String secondAddress = userConfig.getSecondRouterAddress();

        try {
            String bssid = wifiInfo.getBSSID();
            if (routerAddress.equalsIgnoreCase(bssid) || secondAddress.equalsIgnoreCase(bssid)) {
                connected = true;
            } else if (routerAddress.isEmpty() && secondAddress.isEmpty()) {
                Toast.makeText(context, "No router mac address is provided. Please set this in 'Settings'", Toast.LENGTH_LONG).show();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e("Error", e.toString());
        }

        return connected;
    }
}

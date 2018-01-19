package com.mia.smartlight.model;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class UserConfig {

    private static final String FILE_NAME = "SMART_LIGHT_PREF";
    private static UserConfig instance;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private String serverURL;
    private String routerAddress;
    private String secondRouterAddress;
    private NexaUnit lampToSetOnWifi;
    private NexaUnit lampToSetOnAlarm;
    private int alarmHour;
    private int alarmMinute;
    private boolean alarmOn;
    private double latitude;
    private double longitude;

    private UserConfig(Context context) {

        settings = context.getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        serverURL = getServerURL();
        routerAddress = getRouterAddress();
        secondRouterAddress = getSecondRouterAddress();
        lampToSetOnWifi = getLampToSetOnWifi();
        lampToSetOnAlarm = getLampToSetOnAlarm();
        alarmHour = getAlarmHour();
        alarmMinute = getAlarmMinute();
        alarmOn = isAlarmOn();
        latitude = getLatitude();
        longitude = getLongitude();
    }

    public static synchronized UserConfig getInstance(Context context){
        if(instance == null) {
            instance = new UserConfig(context);
        }
        return instance;
    }

    public String getServerURL() {
        if (serverURL == null) {
            serverURL = settings.getString("server_url", null);
        }
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
        editor.putString("server_url", serverURL);
        editor.commit();
    }

    public String getRouterAddress() {
        if (routerAddress == null) {
            routerAddress = settings.getString("router_address", null);
        }
        return routerAddress;
    }

    public void setRouterAddress(String routerAddress) {
        this.routerAddress = routerAddress;
        editor.putString("router_address", routerAddress);
        editor.commit();
    }

    public String getSecondRouterAddress() {
        if (secondRouterAddress == null) {
            secondRouterAddress = settings.getString("second_router_address", null);
        }
        return secondRouterAddress;
    }

    public void setSecondRouterAddress(String secondRouterAddress) {
        this.secondRouterAddress = secondRouterAddress;
        editor.putString("second_router_address", secondRouterAddress);
        editor.commit();
    }

    public NexaUnit getLampToSetOnWifi() {
        if (lampToSetOnWifi == null) {
            String lamp = settings.getString("lamp_to_set_on_wifi", null);
            if (lamp != null) {
                try {
                    JSONObject object = (JSONObject) new JSONTokener(lamp).nextValue();

                    lampToSetOnWifi = new NexaUnit(object.optString("name"), Integer.parseInt(object.optString("id")),
                            object.optString("category"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return lampToSetOnWifi;
    }

    public void setLampToSetOnWifi(NexaUnit unit) {
        this.lampToSetOnWifi = unit;
        String lamp ="{\"name\":\""+unit.getName()+"\",\"id\":\""+unit.getId()+"\",\"category\":\""+unit.getCategory()+"\"}";
        editor.putString("lamp_to_set_on_wifi", lamp);
        editor.commit();
    }

    public NexaUnit getLampToSetOnAlarm() {
        if (lampToSetOnAlarm == null) {
            String lamp = settings.getString("lamp_to_set_on_alarm", null);
            if (lamp != null) {
                try {
                    JSONObject object = (JSONObject) new JSONTokener(lamp).nextValue();

                    lampToSetOnAlarm = new NexaUnit(object.optString("name"), Integer.parseInt(object.optString("id")),
                            object.optString("category"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return lampToSetOnAlarm;
    }

    public void setLampToSetOnAlarm(NexaUnit unit) {
        this.lampToSetOnAlarm = unit;
        String lamp ="{\"name\":\""+unit.getName()+"\",\"id\":\""+unit.getId()+"\",\"category\":\""+unit.getCategory()+"\"}";
        editor.putString("lamp_to_set_on_alarm", lamp);
        editor.commit();
    }

    public int getAlarmHour() {
        if (alarmHour == 0 || alarmHour == -1) {
            alarmHour = settings.getInt("alarm_hour", -1);
        }
        return alarmHour;
    }

    public void setAlarmHour(int alarmHour) {
        this.alarmHour = alarmHour;
        editor.putInt("alarm_hour", alarmHour);
        editor.commit();
    }

    public int getAlarmMinute() {
        if (alarmMinute == 0 || alarmMinute == -1) {
            alarmMinute = settings.getInt("alarm_minute", -1);
        }
        return alarmMinute;
    }

    public void setAlarmMinute(int alarmMinute) {
        this.alarmMinute = alarmMinute;
        editor.putInt("alarm_minute", alarmMinute);
        editor.commit();
    }

    public boolean isAlarmOn() {
        if (alarmOn == false) {
            alarmOn = settings.getBoolean("alarm_on", false);
        }
        return alarmOn;
    }

    public void setAlarmOn(boolean alarmOn) {
        this.alarmOn = alarmOn;
        editor.putBoolean("alarm_on", alarmOn);
        editor.commit();
    }

    public double getLatitude() {
        if (latitude == 0) {
            latitude = (double) settings.getFloat("latitude", 0);
        }
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
        editor.putFloat("latitude", (float) latitude);
        editor.commit();
    }

    public double getLongitude() {
        if (longitude == 0) {
            longitude = (double) settings.getFloat("longitude", 0);
        }
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        editor.putFloat("longitude", (float) longitude);
        editor.commit();
    }
}

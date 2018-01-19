package com.mia.smartlight.activity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.mia.smartlight.model.UserConfig;
import com.mia.smartlight.service.GeofenceTransitionsIntentService;
import com.mia.smartlight.R;

import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private Intent intent;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private Location home;
    private List<Geofence> geofences;
    private PendingIntent geofencePendingIntent;
    private TextView coordinates;
    private UserConfig userConfig;
    private final int PERMISSION_CODE_1 = 1;
    private final int PERMISSION_CODE_2 = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        geofences = new ArrayList<>();
        userConfig = UserConfig.getInstance(this);

        coordinates = (TextView) findViewById(R.id.home_coordinates);
        if (userConfig.getLatitude() != 0 && userConfig.getLongitude() != 0) {
            coordinates.setText(userConfig.getLatitude() + ":" + userConfig.getLongitude());
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LocationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE_1);
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

    }

    public void addGeofence(View v) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LocationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE_2);
        }

        if (geofences != null)
            geofences.clear();

        home = lastLocation;

        String id = "home";
        Geofence geofence = new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(
                        home.getLatitude(),
                        home.getLongitude(),
                        500
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        geofences.add(geofence);
        userConfig.setLatitude(home.getLatitude());
        userConfig.setLongitude(home.getLongitude());

        LocationServices.GeofencingApi.addGeofences(
                googleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(this);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(geofences);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(LocationActivity.this, GeofenceTransitionsIntentService.class);
        geofencePendingIntent = PendingIntent.getService(LocationActivity.this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE_1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                }
                break;
            }
            case PERMISSION_CODE_2: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (geofences != null)
                        geofences.clear();

                    home = lastLocation;

                    String id = "home";
                    Geofence geofence = new Geofence.Builder()
                            .setRequestId(id)
                            .setCircularRegion(
                                    home.getLatitude(),
                                    home.getLongitude(),
                                    300
                            )
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build();

                    geofences.add(geofence);
                    userConfig.setLatitude(home.getLatitude());
                    userConfig.setLongitude(home.getLongitude());

                    LocationServices.GeofencingApi.addGeofences(
                            googleApiClient,
                            getGeofencingRequest(),
                            getGeofencePendingIntent()
                    ).setResultCallback(this);
                }
            }
        }
    }

    @Override
    public void onResult(@NonNull Status status) {

        Toast.makeText(this, "Location set", Toast.LENGTH_SHORT).show();

        coordinates.setText(home.getLatitude() + ":" + home.getLongitude());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_home:
                intent = new Intent(LocationActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.action_alarm:
                intent = new Intent(LocationActivity.this, AlarmActivity.class);
                startActivity(intent);
                break;
            case R.id.action_location:
                break;
            case R.id.action_settings:
                intent = new Intent(LocationActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

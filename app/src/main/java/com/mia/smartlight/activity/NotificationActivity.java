package com.mia.smartlight.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mia.smartlight.R;
import com.mia.smartlight.model.Attribute;
import com.mia.smartlight.model.NexaUnit;
import com.mia.smartlight.service.NexaService;
import com.mia.smartlight.service.VolleyResponseListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity shown when leaving the home zone with units still on. Shows a list with units the user might want to turn off.
 */
public class NotificationActivity extends AppCompatActivity {

    private Intent intent;
    private NexaService service;
    private List<NexaUnit> units;
    private ArrayAdapter<NexaUnit> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        service = NexaService.getInstance(this);

        units = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(NotificationActivity.this,
                android.R.layout.simple_list_item_1, android.R.id.text1, units);
        ListView listView = findViewById(R.id.nexa_list);
        listView.setAdapter(arrayAdapter);

        getNexaUnits();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void getNexaUnits() {

        service.getUnits(NotificationActivity.this, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                if (message.equals(NexaService.FLAG)) {
                    Toast.makeText(NotificationActivity.this, "No server URL provided, please set this in 'Settings'", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(NotificationActivity.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onResponse(Object response) {

                units = (List<NexaUnit>) response;

                getUnit();
            }
        });
    }

    private void getUnit() {

        for (NexaUnit unit : units) {
            service.getUnit(unit.getId(), NotificationActivity.this, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    if (message.equals(NexaService.FLAG)) {
                        Toast.makeText(NotificationActivity.this, "No server URL provided, please set this in 'Settings'", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(NotificationActivity.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onResponse(Object response) {

                    NexaUnit unit = (NexaUnit) response;

                    for (Attribute a : unit.getAttributes()) {
                        if (a.getName().equalsIgnoreCase("state") && a.getValue().equalsIgnoreCase("on")) {
                            arrayAdapter.add(unit);
                        }
                    }
                }
            });
        }
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
                intent = new Intent(NotificationActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.action_alarm:
                intent = new Intent(NotificationActivity.this, AlarmActivity.class);
                startActivity(intent);
                break;
            case R.id.action_location:
                intent = new Intent(NotificationActivity.this, LocationActivity.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                intent = new Intent(NotificationActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

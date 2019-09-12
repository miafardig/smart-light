package com.mia.smartlight.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mia.smartlight.service.NexaService;
import com.mia.smartlight.model.NexaUnit;
import com.mia.smartlight.R;
import com.mia.smartlight.service.VolleyResponseListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NexaService service;
    private List<NexaUnit> units;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        service = NexaService.getInstance(this);

        getNexaUnits();

    }

    private void getNexaUnits() {

        service.getUnits(MainActivity.this, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                if (message.equals(NexaService.FLAG)) {
                    Toast.makeText(MainActivity.this, "No server URL provided, please set this in 'Settings'", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onResponse(Object response) {

                units = (List<NexaUnit>) response;

                ArrayAdapter<NexaUnit> arrayAdapter = new ArrayAdapter<NexaUnit>(MainActivity.this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, units);
                ListView listView = (ListView) findViewById(R.id.nexa_list);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        intent = new Intent(MainActivity.this, LampActivity.class);
                        intent.putExtra("unitId", units.get(position).getId());
                        startActivity(intent);
                    }
                });

                listView.setAdapter(arrayAdapter);
            }
        });
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
                break;
            case R.id.action_alarm:
                intent = new Intent(MainActivity.this, AlarmActivity.class);
                startActivity(intent);
                break;
            case R.id.action_location:
                intent = new Intent(MainActivity.this, LocationActivity.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

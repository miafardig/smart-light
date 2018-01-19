package com.mia.smartlight.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mia.smartlight.model.Attribute;
import com.mia.smartlight.service.NexaService;
import com.mia.smartlight.model.NexaUnit;
import com.mia.smartlight.R;
import com.mia.smartlight.service.VolleyResponseListener;

import java.util.List;

public class LampActivity extends AppCompatActivity {

    private NexaService service;
    private Intent intent;
    private NexaUnit unit;
    private List<Attribute> attributes;
    private Button on;
    private Button off;
    private Button dim;
    private Button bright;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lamp);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int unitId = getIntent().getIntExtra("unitId", 0);
        service = NexaService.getInstance(this);

        service.getUnit(unitId, LampActivity.this, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                if (message.equals(NexaService.FLAG)) {
                    Toast.makeText(LampActivity.this, "No server URL provided, please set this in 'Settings'", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LampActivity.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onResponse(Object response) {
                unit = (NexaUnit) response;
                attributes = unit.getAttributes();
                displayContent();
            }
        });

    }

    private void displayContent() {

        TextView name = (TextView) findViewById(R.id.lamp_name);
        name.setText(unit.getName());
        TextView id = (TextView) findViewById(R.id.lamp_id);
        id.setText(String.valueOf(unit.getId()));

        addStateAndDimLevel();
        addButtons();
        setListeners();
    }

    private void addStateAndDimLevel() {
        TextView level = (TextView) findViewById(R.id.lamp_level);
        TextView state = (TextView) findViewById(R.id.lamp_state);

        for (Attribute a : attributes) {
            if (a.getName().equalsIgnoreCase("state")) {
                state.setText(a.getValue());
            } else if (unit.getClassName().equalsIgnoreCase("NexaLCLamp")) {
                level.setText("Not dimmable");
            } else if (a.getName().equalsIgnoreCase("level") && unit.getClassName().equalsIgnoreCase("NexaLCDimmer")) {
                level.setText(a.getValue());
            }
        }
    }

    private void addButtons() {
        LinearLayout buttonGroup = (LinearLayout) findViewById(R.id.button_group);

        on = new Button(this);
        on.setText("On");
        on.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonGroup.addView(on);
        off = new Button(this);
        off.setText("Off");
        off.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonGroup.addView(off);

        for (String s : unit.getActions()) {
            if (s.equalsIgnoreCase("dim")) {
                dim = new Button(this);
                dim.setText("Dim");
                dim.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                buttonGroup.addView(dim);
            } else if (s.equalsIgnoreCase("bright")) {
                bright = new Button(this);
                bright.setText("Bright");
                bright.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                buttonGroup.addView(bright);
            }
        }
    }

    private void setListeners() {

        on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            service.invokeAction(unit.getId(), "on", LampActivity.this, new VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    if (message.equals(NexaService.FLAG)) {
                        Toast.makeText(LampActivity.this, "No server URL provided, please set this in 'Settings'", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LampActivity.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onResponse(Object response) {

                    attributes = (List<Attribute>) response;
                    unit.setAttributes(attributes);
                    addStateAndDimLevel();
                }
            });

            }
        });

        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                service.invokeAction(unit.getId(), "off", LampActivity.this, new VolleyResponseListener() {
                    @Override
                    public void onError(String message) {
                        if (message.equals(NexaService.FLAG)) {
                            Toast.makeText(LampActivity.this, "No server URL provided, please set this in 'Settings'", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LampActivity.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onResponse(Object response) {

                        attributes = (List<Attribute>) response;
                        unit.setAttributes(attributes);
                        addStateAndDimLevel();
                    }
                });
            }
        });

        if (dim != null) {
            dim.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    service.invokeAction(unit.getId(), "dim", LampActivity.this, new VolleyResponseListener() {
                        @Override
                        public void onError(String message) {
                            if (message.equals(NexaService.FLAG)) {
                                Toast.makeText(LampActivity.this, "No server URL provided, please set this in 'Settings'", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(LampActivity.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onResponse(Object response) {

                            attributes = (List<Attribute>) response;
                            unit.setAttributes(attributes);
                            addStateAndDimLevel();
                        }
                    });
                }
            });
        }

        if (bright != null) {
            bright.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    service.invokeAction(unit.getId(), "bright", LampActivity.this, new VolleyResponseListener() {
                        @Override
                        public void onError(String message) {
                            if (message.equals(NexaService.FLAG)) {
                                Toast.makeText(LampActivity.this, "No server URL provided, please set this in 'Settings'", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(LampActivity.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onResponse(Object response) {

                            attributes = (List<Attribute>) response;
                            unit.setAttributes(attributes);
                            addStateAndDimLevel();
                        }
                    });
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
                intent = new Intent(LampActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.action_alarm:
                intent = new Intent(LampActivity.this, AlarmActivity.class);
                startActivity(intent);
                break;
            case R.id.action_location:
                intent = new Intent(LampActivity.this, LocationActivity.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                intent = new Intent(LampActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
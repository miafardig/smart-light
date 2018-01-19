package com.mia.smartlight.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mia.smartlight.R;
import com.mia.smartlight.model.NexaUnit;
import com.mia.smartlight.model.UserConfig;
import com.mia.smartlight.service.NexaService;
import com.mia.smartlight.service.VolleyResponseListener;

import java.util.List;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private UserConfig userConfig;
    private NexaService service;
    private Intent intent;
    private List<NexaUnit> units;
    private EditText editServer;
    private EditText editRouter;
    private EditText secondEditRouter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userConfig = UserConfig.getInstance(this);
        service = NexaService.getInstance(this);

        editServer = (EditText) findViewById(R.id.edit_server);
        editRouter = (EditText) findViewById(R.id.edit_router);
        secondEditRouter = (EditText) findViewById(R.id.edit_router_two);

        if (userConfig.getServerURL() != null) {
            if (!userConfig.getServerURL().isEmpty()) {
                editServer.setText(userConfig.getServerURL());
                loadLamps();
            }
        }
        if (userConfig.getRouterAddress() != null) {
            if (!userConfig.getRouterAddress().isEmpty()) {
                editRouter.setText(userConfig.getRouterAddress());
            }
        }
        if (userConfig.getSecondRouterAddress() != null) {
            if (!userConfig.getSecondRouterAddress().isEmpty()) {
                secondEditRouter.setText(userConfig.getSecondRouterAddress());
            }
        }

        setListeners();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loadLamps() {

        service.getUnits(SettingsActivity.this, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                if (message.equals(NexaService.FLAG)) {
                    Toast.makeText(SettingsActivity.this, "No server URL provided, please set this first", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onResponse(Object response) {

                units = (List<NexaUnit>) response;

                ArrayAdapter<NexaUnit> arrayAdapter = new ArrayAdapter<NexaUnit>(SettingsActivity.this,
                        android.R.layout.simple_spinner_item, android.R.id.text1, units);

                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                Spinner spinner = (Spinner) findViewById(R.id.lamp_list);

                spinner.setAdapter(arrayAdapter);
                if (userConfig.getLampToSetOnWifi() != null) {
                    for (NexaUnit unit : units) {
                        if (unit.getId() == userConfig.getLampToSetOnWifi().getId()) {
                            spinner.setSelection(arrayAdapter.getPosition(unit));
                        }
                    }
                }
                spinner.setOnItemSelectedListener(SettingsActivity.this);
            }
        });
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        NexaUnit unit = (NexaUnit) parent.getItemAtPosition(pos);
        userConfig.setLampToSetOnWifi(unit);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private void setListeners() {

        editServer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    userConfig.setServerURL(v.getText().toString());
                    InputMethodManager manager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    loadLamps();

                    handled = true;
                }
                return handled;
            }
        });

        editRouter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    userConfig.setRouterAddress(v.getText().toString());
                    InputMethodManager manager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    handled = true;
                }
                return handled;
            }
        });

        secondEditRouter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    userConfig.setSecondRouterAddress(v.getText().toString());
                    InputMethodManager manager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    handled = true;
                }
                return handled;
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
                intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.action_alarm:
                intent = new Intent(SettingsActivity.this, AlarmActivity.class);
                startActivity(intent);
                break;
            case R.id.action_location:
                intent = new Intent(SettingsActivity.this, LocationActivity.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}

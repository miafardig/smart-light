package com.mia.smartlight.activity;

import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mia.smartlight.receiver.AlarmReceiver;
import com.mia.smartlight.service.NexaService;
import com.mia.smartlight.model.NexaUnit;
import com.mia.smartlight.R;
import com.mia.smartlight.model.UserConfig;
import com.mia.smartlight.service.VolleyResponseListener;

import java.util.Calendar;
import java.util.List;

public class AlarmActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Intent intent;
    private List<NexaUnit> units;
    private NexaService service;
    private UserConfig userConfig;
    private Switch toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userConfig = UserConfig.getInstance(this);
        service = NexaService.getInstance(this);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(AlarmActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(AlarmActivity.this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        TextView view = (TextView) findViewById(R.id.chosen_time);

        toggle = (Switch) findViewById(R.id.alarm_switch);
        toggle.setChecked(userConfig.isAlarmOn());
        setSwitchListener();

        if (userConfig.getAlarmHour() != -1 && userConfig.getAlarmMinute() != -1) {
            view.setText((userConfig.getAlarmHour() < 10 ? "0" + userConfig.getAlarmHour() : userConfig.getAlarmHour())
                    + ":" + (userConfig.getAlarmMinute() < 10 ? "0" + userConfig.getAlarmMinute() : userConfig.getAlarmMinute()));
        } else {
            view.setText("No time chosen");
        }

        loadLamps();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loadLamps() {

        service.getUnits(AlarmActivity.this, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                if (message.equals(NexaService.FLAG)) {
                    Toast.makeText(AlarmActivity.this, "No server URL provided, please set this in 'Settings'", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AlarmActivity.this, "Could not connect to server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onResponse(Object response) {

                units = (List<NexaUnit>) response;

                ArrayAdapter<NexaUnit> arrayAdapter = new ArrayAdapter<>(AlarmActivity.this,
                        android.R.layout.simple_spinner_item, android.R.id.text1, units);

                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                Spinner spinner = (Spinner) findViewById(R.id.alarm_lamp_list);

                spinner.setAdapter(arrayAdapter);
                if (userConfig.getLampToSetOnAlarm() != null) {
                    for (NexaUnit unit : units) {
                        if (unit.getId() == userConfig.getLampToSetOnAlarm().getId()) {
                            spinner.setSelection(arrayAdapter.getPosition(unit));
                        }
                    }
                }
                spinner.setOnItemSelectedListener(AlarmActivity.this);
            }
        });
    }

    private void setSwitchListener() {

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    userConfig.setAlarmOn(true);
                    setAlarm();
                } else {
                    userConfig.setAlarmOn(false);
                    alarmManager.cancel(pendingIntent);
                }
            }
        });
    }

    private void setAlarm() {

        if (toggle.isChecked()) {
            if (userConfig.getAlarmHour() != -1 && userConfig.getAlarmMinute() != -1) {

                Calendar now = Calendar.getInstance();
                Calendar calendar = Calendar.getInstance();

                calendar.set(Calendar.HOUR_OF_DAY, userConfig.getAlarmHour());
                calendar.set(Calendar.MINUTE, userConfig.getAlarmMinute());
                calendar.set(Calendar.SECOND, 0);

                long diff = now.getTimeInMillis() - calendar.getTimeInMillis();
                if (diff > 0) {
                    calendar.add(Calendar.DATE, 1);
                }

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

                Toast.makeText(AlarmActivity.this, "Alarm is set", Toast.LENGTH_SHORT).show();
            }
        } else {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(AlarmActivity.this, "Alarm is disabled", Toast.LENGTH_SHORT).show();
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        NexaUnit unit = (NexaUnit) parent.getItemAtPosition(pos);
        userConfig.setLampToSetOnAlarm(unit);
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
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
                intent = new Intent(AlarmActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.action_alarm:
                break;
            case R.id.action_location:
                intent = new Intent(AlarmActivity.this, LocationActivity.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                intent = new Intent(AlarmActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

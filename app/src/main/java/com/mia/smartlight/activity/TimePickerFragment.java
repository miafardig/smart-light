package com.mia.smartlight.activity;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mia.smartlight.receiver.AlarmReceiver;
import com.mia.smartlight.R;
import com.mia.smartlight.model.UserConfig;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private UserConfig userConfig;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), getTheme(),this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        userConfig = UserConfig.getInstance(getActivity().getApplicationContext());
        userConfig.setAlarmHour(hourOfDay);
        userConfig.setAlarmMinute(minute);

        TextView timeView = (TextView) getActivity().findViewById(R.id.chosen_time);
        timeView.setText((hourOfDay < 10 ? "0" + hourOfDay : hourOfDay)
                + ":" + (minute < 10 ? "0" + minute : minute));

        setAlarm();

        Log.d("TimeSet", (hourOfDay < 10 ? "0" + hourOfDay : hourOfDay)
                + ":" + (minute < 10 ? "0" + minute : minute));

    }

    private void setAlarm() {

        Switch toggle = (Switch) getActivity().findViewById(R.id.alarm_switch);

        if (toggle.isChecked()) {

            Calendar calendar = Calendar.getInstance();
            Calendar now = Calendar.getInstance();

            calendar.set(Calendar.HOUR_OF_DAY, userConfig.getAlarmHour());
            calendar.set(Calendar.MINUTE, userConfig.getAlarmMinute());
            calendar.set(Calendar.SECOND, 0);

            long diff = now.getTimeInMillis() - calendar.getTimeInMillis();
            if (diff > 0) {
                calendar.add(Calendar.DATE, 1);
            }

            Intent intent = new Intent(getActivity(), AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

            Toast.makeText(getActivity(), "Alarm is set", Toast.LENGTH_SHORT).show();
        }
    }
}

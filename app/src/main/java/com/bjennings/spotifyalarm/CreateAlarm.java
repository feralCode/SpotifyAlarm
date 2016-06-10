package com.bjennings.spotifyalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import java.lang.reflect.Method;
import java.util.Calendar;

public class CreateAlarm extends AppCompatActivity implements SongPickerFragment.SongPickerListener {
    Method getHour, getMinute, setHour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMethods(Build.VERSION.SDK_INT);
        setContentView(R.layout.activity_create_alarm);
        final Context context = this;

        Toolbar tb = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("New Alarm");

        Button cancelBtn = (Button)findViewById(R.id.cancel_btn);
        Button saveBtn = (Button)findViewById(R.id.save_btn);
        assert saveBtn != null;
        assert cancelBtn != null;

        final TimePicker t = (TimePicker)findViewById(R.id.timePicker);
        assert t != null;
        LinearLayout ampm = (LinearLayout)t.findViewById(getResources().getIdentifier("android:id/ampm_layout", null, null));
        ampm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hours;
                if (Build.VERSION.SDK_INT > 22) {
                    hours = t.getHour();
                } else {
                    hours = t.getCurrentHour();
                }
                toggleTime((LinearLayout)v, t, hours);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float time = 0;
                int hours, minutes;
                TimePicker t = (TimePicker)findViewById(R.id.timePicker);
                assert t != null;
                if (Build.VERSION.SDK_INT > 22) {
                    time += t.getHour();
                    hours = t.getHour();
                    time += (float)t.getMinute()/60.0;
                    minutes = t.getMinute();
                } else {
                    time += t.getCurrentHour();
                    hours = t.getCurrentHour();
                    time += (float)t.getCurrentMinute()/60.0;
                    minutes = t.getCurrentMinute();
                }

                AlarmDbHelper dbHelper = new AlarmDbHelper(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(AlarmContract.AlarmDB.COLUMN_NAME_TIME, time);
                values.put(AlarmContract.AlarmDB.COLUMN_NAME_ENABLED, true);

                long id = db.insert(
                        AlarmContract.AlarmDB.TABLE_NAME,
                        null,
                        values
                );

                AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(context, AlarmReceiver.class);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(context, (int)id, intent, 0);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hours);
                calendar.set(Calendar.MINUTE, minutes);

                if (Build.VERSION.SDK_INT >= 23) {
                    alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                } else if ( Build.VERSION.SDK_INT >= 19) {
                    alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                } else {
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                }

                finish();
            }
        });
    }

    private void toggleTime(LinearLayout ampm, TimePicker t, int hours) {
        try {
            if (hours >= 12) {
                setHour.invoke(t, hours - 12);
                //View pm =;
            } else {
                setHour.invoke(t, hours + 12);
                //hide am, show pm
            }
        } catch (Exception e) {}
    }

    private void setMethods(int sdk) {
        Class[] args = {Integer.class};
        try {
            setHour = sdk >= 23 ? TimePicker.class.getMethod("setHour", args) : TimePicker.class.getMethod("setCurrentHour", args);
            getMinute = sdk >= 23 ? TimePicker.class.getMethod("getMinute", args) : TimePicker.class.getMethod("getCurrentMinute", args);
            getHour = sdk >= 23 ? TimePicker.class.getMethod("getHour", args) : TimePicker.class.getMethod("getCurrentHour", args);
        } catch (Exception e) {}
    }

    @Override
    public void onSelectSong(Bundle args) {

    }

    @Override
    public void onCancel(Fragment frag) {
        getSupportFragmentManager().beginTransaction().remove(frag).commit();
    }
}

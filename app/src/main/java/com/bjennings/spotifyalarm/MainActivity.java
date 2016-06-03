package com.bjennings.spotifyalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity implements ConfirmFragment.ConfirmEvents {
    private SQLiteDatabase db;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AlarmDbHelper dbHelper = new AlarmDbHelper(this);
        db = dbHelper.getWritableDatabase();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CreateAlarm.class);
                startActivity(intent);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onResume() {
        super.onResume();
        populateAlarms();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    private void populateAlarms() {
        String[] projection = {AlarmContract.AlarmDB._ID, AlarmContract.AlarmDB.COLUMN_NAME_ENABLED,
                AlarmContract.AlarmDB.COLUMN_NAME_TIME};
        String sortOrder = AlarmContract.AlarmDB.COLUMN_NAME_TIME + " ASC";
        Cursor c = db.query(
                AlarmContract.AlarmDB.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
        TreeSet<Alarm> alarmColl = parseTable(c);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout alarms = (LinearLayout)findViewById(R.id.alarm_container);
        assert alarms != null;
        alarms.removeAllViews();

        for (Alarm a : alarmColl) {
            final View aView = inflater.inflate(R.layout.alarm_layout, null);
            TextView time = (TextView)aView.findViewById(R.id.time);
            int hours = (int)a.time;
            int minutes = (int)Math.floor((a.time - (int)a.time) * 60);
            String letters;

            switch (hours) {
                case 0:
                    hours = 12;
                case 1:case 2:case 3:case 4:case 5:case 6:case 7:case 8:case 9:case 10:case 11:
                    letters = "AM";
                    break;
                case 12:
                    letters = "PM";
                    break;
                default:
                    hours -= 12;
                    letters = "PM";
            }

            time.setText(String.format(new Locale("en-US"), "%2d:%02d %s", hours, minutes, letters));

            Switch enabled = (Switch)aView.findViewById(R.id.enabled);
            enabled.setChecked(a.enabled);
            enabled.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    boolean enabled = ((Switch)aView.findViewById(R.id.enabled)).isChecked();

                    ContentValues values = new ContentValues();
                    values.put(AlarmContract.AlarmDB.COLUMN_NAME_ENABLED, enabled);

                    String selection = AlarmContract.AlarmDB._ID + " LIKE ?";
                    String[] selectionArgs = { String.valueOf(((TextView)aView.findViewById(R.id.id)).getText()) };

                    int count = db.update(
                            AlarmContract.AlarmDB.TABLE_NAME,
                            values,
                            selection,
                            selectionArgs);
                }
            });

            TextView id = (TextView)aView.findViewById(R.id.id);
            id.setText(String.format(new Locale("en-US"), "%d", a.id));

            aView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    DialogFragment newFragment = new OptionsFragment();
                    Bundle args = new Bundle();
                    String id = (String)((TextView)v.findViewById(R.id.id)).getText();
                    args.putString("message", "Are you sure you want to delete this alarm?");
                    args.putString("positive", "Delete");
                    args.putString("negative", "Cancel");
                    args.putString("id", id);
                    newFragment.setArguments(args);
                    newFragment.show(getSupportFragmentManager(), "options-" + id);

                    return true;
                }
            });

            alarms.addView(aView);
        }
    }

    private TreeSet<Alarm> parseTable(Cursor c) {
        TreeSet<Alarm> alarms = new TreeSet<Alarm>();
        boolean parse = c.moveToFirst();

        while (parse) {
            int id = c.getInt(c.getColumnIndexOrThrow(AlarmContract.AlarmDB._ID));
            float time = c.getFloat(c.getColumnIndexOrThrow(AlarmContract.AlarmDB.COLUMN_NAME_TIME));
            boolean enabled = c.getShort(c.getColumnIndexOrThrow(AlarmContract.AlarmDB.COLUMN_NAME_ENABLED)) == 1;

            alarms.add(new Alarm(id, time, enabled));
            parse = c.moveToNext();
        }

        return alarms;
    }

    @Override
    public void onConfirm(Bundle args) {
        String selection = AlarmContract.AlarmDB._ID + " = ?";
        String[] selectionArgs = { args.getString("id") };
        db.delete(AlarmContract.AlarmDB.TABLE_NAME, selection, selectionArgs);
        int id = Integer.parseInt(args.getString("id"));
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        alarmMgr.cancel(alarmIntent);
        populateAlarms();
    }

    @Override
    public void onCancel(Bundle args) {
        //nothing
    }

    private class Alarm implements Comparable<Alarm> {
        public int id;
        public float time;
        public boolean enabled;

        public Alarm(int _id, float _time, boolean _enabled) {
            id = _id;
            time = _time;
            enabled = _enabled;
        }

        public int compareTo(Alarm that) {
            return this.time > that.time ? 1 : this.time == that.time ? 0 : -1;
        }
    }
}

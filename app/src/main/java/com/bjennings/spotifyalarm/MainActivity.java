package com.bjennings.spotifyalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;

import java.util.Calendar;
import java.util.Locale;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity implements ConfirmFragment.ConfirmEvents, ConnectionStateCallback {
    private SQLiteDatabase db;
    private final Context context = this;

    private static final String CLIENT_ID = "88149444040e4b1b96119f43f0013040";
    private static final String REDIRECT_URI = "bestspotifyalarm://callback";
    private static final int REQUEST_CODE = 69;

    private Player mPlayer;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            /*if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                //possibly do something, but not now
            }*/
        }
    }

    @Override
    public void onLoggedIn() {}

    @Override
    public void onLoggedOut() {}

    @Override
    public void onLoginFailed(Throwable error) {}

    @Override
    public void onTemporaryError() {}

    @Override
    public void onConnectionMessage(String message) {}

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

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    public void onResume() {
        super.onResume();
        populateAlarms();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Spotify.destroyPlayer(this);
        db.close();
    }

    private void populateAlarms() {
        String[] projection = {AlarmContract.AlarmDB._ID, AlarmContract.AlarmDB.COLUMN_NAME_ENABLED,
                AlarmContract.AlarmDB.COLUMN_NAME_TIME, AlarmContract.AlarmDB.COLUMN_NAME_SONG_ID};
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

        for (final Alarm a : alarmColl) {
            final View aView = inflater.inflate(R.layout.alarm_layout, alarms, false);
            TextView time = (TextView)aView.findViewById(R.id.time);
            final int hours = (int)a.time;
            int displayHours = hours;
            final int minutes = Math.round((a.time - (int)a.time) * 60);
            String letters;

            switch (hours) {
                case 0:
                    displayHours = 12;
                case 1:case 2:case 3:case 4:case 5:case 6:case 7:case 8:case 9:case 10:case 11:
                    letters = "AM";
                    break;
                case 12:
                    letters = "PM";
                    break;
                default:
                    displayHours -= 12;
                    letters = "PM";
            }

            time.setText(String.format(new Locale("en-US"), "%2d:%02d %s", displayHours, minutes, letters));

            Switch enabled = (Switch)aView.findViewById(R.id.enabled);
            enabled.setChecked(a.enabled);
            enabled.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    int id = Integer.parseInt(String.valueOf(((TextView)aView.findViewById(R.id.id)).getText()));
                    ContentValues values = new ContentValues();
                    boolean enabled = ((Switch)v).isChecked();
                    values.put(AlarmContract.AlarmDB.COLUMN_NAME_ENABLED, enabled);

                    String selection = AlarmContract.AlarmDB._ID + " LIKE ?";
                    String[] selectionArgs = { String.valueOf(((TextView)aView.findViewById(R.id.id)).getText()) };

                    int count = db.update(
                            AlarmContract.AlarmDB.TABLE_NAME,
                            values,
                            selection,
                            selectionArgs);

                    if (enabled) {
                        Alarm.create(context, id, hours, minutes, 0, a.track);
                    } else {
                        Alarm.delete(context, id);
                    }
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
        TreeSet<Alarm> alarms = new TreeSet<>();
        boolean parse = c.moveToFirst();

        while (parse) {
            int id = c.getInt(c.getColumnIndexOrThrow(AlarmContract.AlarmDB._ID));
            float time = c.getFloat(c.getColumnIndexOrThrow(AlarmContract.AlarmDB.COLUMN_NAME_TIME));
            boolean enabled = c.getShort(c.getColumnIndexOrThrow(AlarmContract.AlarmDB.COLUMN_NAME_ENABLED)) == 1;
            String track = c.getString(c.getColumnIndexOrThrow(AlarmContract.AlarmDB.COLUMN_NAME_SONG_ID));

            alarms.add(new Alarm(id, time, enabled, track));
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

        Alarm.delete(context, id);
        populateAlarms();
    }

    @Override
    public void onCancel(Bundle args) {
        //nothing
    }
}

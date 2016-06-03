package com.bjennings.spotifyalarm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

public class AlarmReceiver extends WakefulBroadcastReceiver{
    @Override
    public void onReceive(final Context context, Intent intent) {
        //TODO start alarm activity
        Toast.makeText(context, "yo", Toast.LENGTH_SHORT).show();
    }
}

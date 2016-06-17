package com.bjennings.spotifyalarm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class AlarmReceiver extends WakefulBroadcastReceiver{

    @Override
    public void onReceive(final Context context, Intent intent) {
        Intent alarmServ = new Intent(context, AlarmIntentService.class);
        alarmServ.putExtra("id", intent.getIntExtra("id", 0));
        alarmServ.putExtra("hours", intent.getIntExtra("hours", 0));
        alarmServ.putExtra("minutes", intent.getIntExtra("minutes", 0));
        String track = intent.getStringExtra("track");
        alarmServ.putExtra("track", track);
        startWakefulService(context, alarmServ);
    }
}

package com.bjennings.spotifyalarm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class AlarmReceiver extends WakefulBroadcastReceiver{

    @Override
    public void onReceive(final Context context, Intent intent) {
        Intent alarmAct = new Intent(context, Alarm.class);
        startWakefulService(context, alarmAct);
    }
}

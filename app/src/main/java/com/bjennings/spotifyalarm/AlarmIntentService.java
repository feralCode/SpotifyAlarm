package com.bjennings.spotifyalarm;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

public class AlarmIntentService extends IntentService {
    public AlarmIntentService() {
        super("AlarmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent alarmAct = new Intent(this, Alarm.class);
        alarmAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(alarmAct);
        AlarmReceiver.completeWakefulIntent(intent);
    }
}

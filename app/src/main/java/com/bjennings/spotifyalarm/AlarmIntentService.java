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
        Intent alarmAct = new Intent(this, AlarmActivity.class);
        alarmAct.putExtra("id", intent.getIntExtra("id", 0));
        alarmAct.putExtra("hours", intent.getIntExtra("hours", 0));
        alarmAct.putExtra("minutes", intent.getIntExtra("minutes", 0));
        String track = intent.getStringExtra("track");
        alarmAct.putExtra("track", track);
        alarmAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(alarmAct);
        AlarmReceiver.completeWakefulIntent(intent);
    }
}

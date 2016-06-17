package com.bjennings.spotifyalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.Calendar;

public class Alarm implements Comparable<Alarm> {
    public int id;
    public float time;
    public boolean enabled;
    public String track;

    public Alarm(int _id, float _time, boolean _enabled, String _track) {
        id = _id;
        time = _time;
        enabled = _enabled;
        track = _track;
    }

    public int compareTo(@NonNull Alarm that) {
        return this.time > that.time ? 1 : this.time == that.time ? 0 : -1;
    }

    public static void create(Context context, int id, int hours, int minutes, int day, String track) {
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("id", id);
        intent.putExtra("hours", hours);
        intent.putExtra("minutes", minutes);
        intent.putExtra("track", track);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, id, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + day);

        if (Build.VERSION.SDK_INT >= 23) {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        } else if ( Build.VERSION.SDK_INT >= 19) {
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        } else {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        }
    }

    public static void delete(Context context, int id) {
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        alarmMgr.cancel(alarmIntent);
    }
}
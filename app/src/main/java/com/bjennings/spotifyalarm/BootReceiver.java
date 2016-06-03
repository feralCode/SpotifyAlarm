package com.bjennings.spotifyalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.TreeSet;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            AlarmDbHelper dbHelper = new AlarmDbHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

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
            //TODO reset the alarms on boot
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
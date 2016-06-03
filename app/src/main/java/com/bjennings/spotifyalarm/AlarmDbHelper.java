package com.bjennings.spotifyalarm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlarmDbHelper extends SQLiteOpenHelper{
    private static final String CREATE_ALARM =
            "CREATE TABLE " + AlarmContract.AlarmDB.TABLE_NAME +
            " (" + AlarmContract.AlarmDB._ID + " INTEGER PRIMARY KEY," +
            AlarmContract.AlarmDB.COLUMN_NAME_TIME + " REAL," +
            AlarmContract.AlarmDB.COLUMN_NAME_ENABLED + " TINYINT)";

    private static final String DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + AlarmContract.AlarmDB.TABLE_NAME;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SpotifyAlarm.db";

    public AlarmDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ALARM);
    }

    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        //implement later
    }
}

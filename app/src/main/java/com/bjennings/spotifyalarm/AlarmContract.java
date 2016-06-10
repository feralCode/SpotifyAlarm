package com.bjennings.spotifyalarm;

import android.provider.BaseColumns;

public final class AlarmContract {
    public AlarmContract() {}

    public static abstract class AlarmDB implements BaseColumns {
        public static final String TABLE_NAME = "alarm";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_ENABLED = "enabled";
        public static final String SONG_ID = "2aLJLYZe3dkIvMM9wGb4Gw";
    }
}

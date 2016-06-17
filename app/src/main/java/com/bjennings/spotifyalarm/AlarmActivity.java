package com.bjennings.spotifyalarm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

@SuppressWarnings("ConstantConditions")
public class AlarmActivity extends AppCompatActivity implements PlayerNotificationCallback, ConnectionStateCallback {
    private static final String CLIENT_ID = "88149444040e4b1b96119f43f0013040";
    private static final String REDIRECT_URI = "bestspotifyalarm://callback";
    private static final int REQUEST_CODE = 69;
    private Player mPlayer;
    private final Activity act = this;
    private int prevVolume;
    private String track;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        mPlayer = player;
                        mPlayer.addPlayerNotificationCallback(AlarmActivity.this);
                        mPlayer.play("spotify:track:" + track);
                    }

                    @Override
                    public void onError(Throwable throwable) {}
                });
            }
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
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {}

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        super.onCreate(savedInstanceState);
        int id = getIntent().getIntExtra("id", 0);
        int hours = getIntent().getIntExtra("hours", 0);
        int minutes = getIntent().getIntExtra("minutes", 0);
        track = getIntent().getStringExtra("track");

        AudioManager aMgr = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        prevVolume = aMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        aMgr.setStreamVolume(AudioManager.STREAM_MUSIC, aMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        Alarm.create(this, id, hours, minutes, 1, track);

        setContentView(R.layout.activity_alarm);

        findViewById(R.id.stop_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager aMgr = (AudioManager)act.getSystemService(Context.AUDIO_SERVICE);
                aMgr.setStreamVolume(AudioManager.STREAM_MUSIC, prevVolume, 0);
                act.finish();
            }
        });

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }
}

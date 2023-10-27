package com.example.ring;

import android.app.IntentService;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

public class AlarmService extends IntentService {
    public static final String ACTION_SNOOZE = "com.example.ring.SNOOZE";
    public static final String ACTION_DISMISS = "com.example.ring.DISMISS";

    public AlarmService() {
        super("AlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_SNOOZE.equals(action)) {
                snoozeAlarm();
            } else if (ACTION_DISMISS.equals(action)) {
                dismissAlarm();
            }
        }
    }

    private void snoozeAlarm() {
        // Stop the ringing and snooze for 30 seconds
        // Implement your snooze logic here

        // Schedule the alarm to ring again after snooze delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Start the alarm again
            Intent intent = new Intent(this, AlarmReceiver.class);
            sendBroadcast(intent);
        }, 30 * 1000); // 30 seconds delay
    }

    private void dismissAlarm() {
        // Stop the ringing
        stopRingtone();

        // You can also stop the AlarmReceiver service if necessary
        Intent stopServiceIntent = new Intent(this, AlarmReceiver.class);
        stopService(stopServiceIntent);

        // Perform any additional actions needed to dismiss the alarm
    }

    private Ringtone ringtone;

    private void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the ringtone with your alarm sound
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
    }
}

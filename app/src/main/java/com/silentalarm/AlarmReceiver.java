package com.silentalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    @SuppressWarnings("deprecation")
    public void onReceive(Context c, Intent intent) {
        if (!Scheduler.prefs(c).getBoolean("on", false)) return;
        int secs = Math.max(1, Scheduler.prefs(c).getInt("dur", 5));
        Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            long[] pattern = {0, 600, 300};
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM).build();
            // pattern repeats from index 1 until cancelled; stop after secs
            v.vibrate(VibrationEffect.createWaveform(pattern, 1), attrs);
            new android.os.Handler(android.os.Looper.getMainLooper())
                    .postDelayed(v::cancel, secs * 1000L);
        }
        Scheduler.scheduleNext(c);
    }
}

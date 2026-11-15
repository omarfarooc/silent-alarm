package com.silentalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        if (!Scheduler.prefs(c).getBoolean("on", false)) return;
        // pehle agla alarm lagayein, taake kuch fail ho to bhi silsila na tootay
        try { Scheduler.scheduleNext(c); } catch (Throwable ignored) {}
        try { vibrate(c); } catch (Throwable ignored) {}
    }

    @SuppressWarnings("deprecation")
    static void vibrate(Context c) {
        int secs = Math.max(1, Math.min(60, Scheduler.prefs(c).getInt("dur", 5)));
        Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null || !v.hasVibrator()) return;

        int cycles = Math.max(1, (secs * 1000) / 900);
        long[] pattern = new long[1 + cycles * 2];
        pattern[0] = 0;
        for (int i = 0; i < cycles; i++) {
            pattern[1 + 2 * i] = 600;
            pattern[2 + 2 * i] = 300;
        }

        if (Build.VERSION.SDK_INT >= 26) {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM).build();
            v.vibrate(VibrationEffect.createWaveform(pattern, -1), attrs);
        } else {
            v.vibrate(pattern, -1);
        }
    }
}

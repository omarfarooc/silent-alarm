package com.silentalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

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

        Vibrator v;
        if (Build.VERSION.SDK_INT >= 31) {
            VibratorManager vm = (VibratorManager) c.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            v = (vm != null) ? vm.getDefaultVibrator() : null;
        } else {
            v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (v == null) throw new RuntimeException("Vibrator service nahi mili");
        if (!v.hasVibrator()) throw new RuntimeException("Phone ne bataya ke vibrator nahi hai");

        int cycles = Math.max(1, (secs * 1000) / 900);
        int n = 1 + cycles * 2;
        long[] pattern = new long[n];
        int[] amps = new int[n];
        pattern[0] = 0;
        amps[0] = 0;
        for (int i = 0; i < cycles; i++) {
            pattern[1 + 2 * i] = 600;   // vibrate
            amps[1 + 2 * i] = 255;
            pattern[2 + 2 * i] = 300;   // rukna
            amps[2 + 2 * i] = 0;
        }

        if (Build.VERSION.SDK_INT >= 26) {
            VibrationEffect effect;
            if (v.hasAmplitudeControl()) {
                effect = VibrationEffect.createWaveform(pattern, amps, -1);
            } else {
                effect = VibrationEffect.createWaveform(pattern, -1);
            }
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM).build();
            v.vibrate(effect, attrs);
        } else {
            v.vibrate(pattern, -1);
        }
    }
}

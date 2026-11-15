package com.silentalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.util.Calendar;

public class Scheduler {
    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("alarm", Context.MODE_PRIVATE);
    }

    static PendingIntent receiverIntent(Context c) {
        Intent i = new Intent(c, AlarmReceiver.class);
        return PendingIntent.getBroadcast(c, 1, i,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static long nextTrigger(Context c) {
        SharedPreferences p = prefs(c);
        int sh = p.getInt("sh", 9), sm = p.getInt("sm", 0);
        int eh = p.getInt("eh", 10), em = p.getInt("em", 0);
        int interval = Math.max(1, p.getInt("interval", 10));
        int span = ((eh * 60 + em) - (sh * 60 + sm) + 1440) % 1440;
        long now = System.currentTimeMillis();
        for (int d = -1; d <= 1; d++) {
            Calendar s = Calendar.getInstance();
            s.add(Calendar.DAY_OF_YEAR, d);
            s.set(Calendar.HOUR_OF_DAY, sh);
            s.set(Calendar.MINUTE, sm);
            s.set(Calendar.SECOND, 0);
            s.set(Calendar.MILLISECOND, 0);
            long start = s.getTimeInMillis();
            long end = start + span * 60000L;
            for (long t = start; t <= end; t += interval * 60000L) {
                if (t > now + 1000) return t;
            }
        }
        return -1;
    }

    static void scheduleNext(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        long t = nextTrigger(c);
        if (t < 0) return;
        PendingIntent show = PendingIntent.getActivity(c, 2, new Intent(c, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        am.setAlarmClock(new AlarmManager.AlarmClockInfo(t, show), receiverIntent(c));
    }

    static void cancel(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        am.cancel(receiverIntent(c));
    }
}

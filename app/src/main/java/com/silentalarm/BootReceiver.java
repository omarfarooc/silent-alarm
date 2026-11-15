package com.silentalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        if (Scheduler.prefs(c).getBoolean("on", false)) Scheduler.scheduleNext(c);
    }
}

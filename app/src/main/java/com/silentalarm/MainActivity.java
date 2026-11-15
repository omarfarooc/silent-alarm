package com.silentalarm;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    SharedPreferences p;
    Button startBtn, endBtn;
    EditText interval, dur;
    Switch sw;

    static String fmt(int h, int m) { return String.format("%02d:%02d", h, m); }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        p = Scheduler.prefs(this);

        final Thread.UncaughtExceptionHandler old = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                java.io.StringWriter sw2 = new java.io.StringWriter();
                e.printStackTrace(new java.io.PrintWriter(sw2));
                p.edit().putString("crash", sw2.toString()).commit();
            } catch (Throwable ignored) {}
            if (old != null) old.uncaughtException(t, e);
        });

        String crash = p.getString("crash", null);
        if (crash != null) {
            p.edit().remove("crash").commit();
            ScrollView sv = new ScrollView(this);
            TextView tv = new TextView(this);
            tv.setText("ERROR (screenshot bhejein):\n\n" + crash);
            tv.setTextIsSelectable(true);
            tv.setPadding(30, 30, 30, 30);
            sv.addView(tv);
            setContentView(sv);
            return;
        }

        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("Silent Vibrate Alarm");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        startBtn = new Button(this);
        endBtn = new Button(this);
        root.addView(label("Start time"));
        root.addView(startBtn);
        root.addView(label("End time"));
        root.addView(endBtn);

        interval = num();
        dur = num();
        root.addView(label("Interval (minutes)"));
        root.addView(interval);
        root.addView(label("Vibration length (seconds)"));
        root.addView(dur);

        sw = new Switch(this);
        sw.setText("Alarm ON (daily)");
        sw.setPadding(0, pad, 0, 0);
        root.addView(sw);

        TextView note = new TextView(this);
        note.setText("Alarm sirf vibrate karega, awaaz nahi. Settings change karne ke baad switch dobara ON karein.");
        note.setPadding(0, pad, 0, 0);
        root.addView(note);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);

        interval.setText(String.valueOf(p.getInt("interval", 10)));
        dur.setText(String.valueOf(p.getInt("dur", 5)));
        sw.setChecked(p.getBoolean("on", false));
        refreshButtons();

        startBtn.setOnClickListener(v -> new TimePickerDialog(this, (tp, h, m) -> {
            p.edit().putInt("sh", h).putInt("sm", m).apply();
            refreshButtons();
            reapply();
        }, p.getInt("sh", 9), p.getInt("sm", 0), true).show());

        endBtn.setOnClickListener(v -> new TimePickerDialog(this, (tp, h, m) -> {
            p.edit().putInt("eh", h).putInt("em", m).apply();
            refreshButtons();
            reapply();
        }, p.getInt("eh", 10), p.getInt("em", 0), true).show());

        sw.setOnCheckedChangeListener((v, on) -> {
            save();
            p.edit().putBoolean("on", on).apply();
            if (on) {
                Scheduler.scheduleNext(this);
                Toast.makeText(this, "Alarm ON", Toast.LENGTH_SHORT).show();
            } else {
                Scheduler.cancel(this);
                Toast.makeText(this, "Alarm OFF", Toast.LENGTH_SHORT).show();
            }
        });
    }

    TextView label(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setPadding(0, 24, 0, 0);
        return t;
    }

    EditText num() {
        EditText e = new EditText(this);
        e.setInputType(InputType.TYPE_CLASS_NUMBER);
        return e;
    }

    void refreshButtons() {
        startBtn.setText(fmt(p.getInt("sh", 9), p.getInt("sm", 0)));
        endBtn.setText(fmt(p.getInt("eh", 10), p.getInt("em", 0)));
    }

    void save() {
        int i = 10, d = 5;
        try { i = Integer.parseInt(interval.getText().toString()); } catch (Exception ignored) {}
        try { d = Integer.parseInt(dur.getText().toString()); } catch (Exception ignored) {}
        p.edit().putInt("interval", Math.max(1, i)).putInt("dur", Math.max(1, d)).apply();
    }

    void reapply() {
        if (p.getBoolean("on", false)) { save(); Scheduler.scheduleNext(this); }
    }
}

package io.github.haobv.solvetowatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import java.time.LocalDate;

/** All gate state, in one SharedPreferences file shared by the service and the screens. */
final class Prefs {
    static final int[] MINUTE_CHOICES = {1, 5, 10, 15, 20, 30, 45, 60};

    private final SharedPreferences sp;
    private final Resources res;

    Prefs(Context context) {
        sp = context.getSharedPreferences("gate", Context.MODE_PRIVATE);
        res = context.getResources();
    }

    boolean enabled() { return sp.getBoolean("enabled", true); }
    void setEnabled(boolean on) { sp.edit().putBoolean("enabled", on).apply(); }

    int minutes() { return sp.getInt("minutes", res.getInteger(R.integer.default_minutes)); }
    void setMinutes(int m) { sp.edit().putInt("minutes", m).apply(); }

    int streak() { return sp.getInt("streak", res.getInteger(R.integer.default_streak)); }
    void setStreak(int n) { sp.edit().putInt("streak", n).apply(); }

    String pin() { return sp.getString("pin", res.getString(R.string.default_pin)); }
    void setPin(String pin) { sp.edit().putString("pin", pin).apply(); }

    long snoozeUntil() { return sp.getLong("snoozeUntil", 0); }
    void setSnoozeUntil(long millis) { sp.edit().putLong("snoozeUntil", millis).apply(); }

    /** The gate applies right now: switched on and not snoozed. */
    boolean active() { return enabled() && System.currentTimeMillis() >= snoozeUntil(); }

    /** YouTube is blocked until a quiz is passed or a parent lets it through. */
    boolean locked() { return sp.getBoolean("locked", false); }
    void lock() { sp.edit().putBoolean("locked", true).apply(); }

    int watchedSeconds() { return sp.getInt("watched", 0); }

    void addWatched(int seconds) {
        rollDay();
        sp.edit().putInt("watched", watchedSeconds() + seconds)
                .putInt("dayWatch", sp.getInt("dayWatch", 0) + seconds).apply();
    }

    void passQuiz() {
        rollDay();
        sp.edit().putBoolean("locked", false).putInt("watched", 0)
                .putInt("dayPasses", sp.getInt("dayPasses", 0) + 1).apply();
    }

    void parentUnlock() {
        sp.edit().putBoolean("locked", false).putInt("watched", 0).apply();
    }

    void recordAnswer(boolean correct) {
        rollDay();
        sp.edit().putInt("dayTotal", sp.getInt("dayTotal", 0) + 1)
                .putInt("dayCorrect", sp.getInt("dayCorrect", 0) + (correct ? 1 : 0)).apply();
    }

    String today(Context c) {
        rollDay();
        return c.getString(R.string.today, sp.getInt("dayWatch", 0) / 60, sp.getInt("dayPasses", 0),
                sp.getInt("dayCorrect", 0), sp.getInt("dayTotal", 0));
    }

    private void rollDay() {
        String day = LocalDate.now().toString();
        if (day.equals(sp.getString("day", ""))) return;
        sp.edit().putString("day", day).putInt("dayWatch", 0).putInt("dayPasses", 0)
                .putInt("dayCorrect", 0).putInt("dayTotal", 0).apply();
    }
}

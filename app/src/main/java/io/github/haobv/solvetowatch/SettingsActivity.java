package io.github.haobv.solvetowatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/** Parent settings. Opened from the TV's app list (asks for the PIN) or from the quiz after the PIN. */
public class SettingsActivity extends Activity {
    static final String EXTRA_AUTHED = "authed";
    private static final int REQ_PIN = 1;
    private static final long HOUR = 3_600_000L;

    private Prefs prefs;
    private ViewGroup rows;
    private int snoozeChoice;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_settings);
        prefs = new Prefs(this);
        rows = findViewById(R.id.rows);
        if (!getIntent().getBooleanExtra(EXTRA_AUTHED, false) && state == null) {
            rows.setVisibility(View.INVISIBLE);
            startActivityForResult(new Intent(this, PinActivity.class), REQ_PIN);
        }
        buildRows();
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (request != REQ_PIN) return;
        if (result != RESULT_OK) {
            finish();
            return;
        }
        rows.setVisibility(View.VISIBLE);
        rows.getChildAt(0).requestFocus();
    }

    private void buildRows() {
        addRow(R.string.row_enabled, () -> getString(prefs.enabled() ? R.string.on : R.string.off),
                step -> {
                    prefs.setEnabled(!prefs.enabled());
                    if (!prefs.enabled()) prefs.parentUnlock();
                });
        addRow(R.string.row_minutes, () -> getString(R.string.minutes, prefs.minutes()),
                step -> prefs.setMinutes(Prefs.MINUTE_CHOICES[cycle(indexOf(prefs.minutes()), step, Prefs.MINUTE_CHOICES.length)]));
        addRow(R.string.row_streak, () -> getString(R.string.count, prefs.streak()),
                step -> prefs.setStreak(1 + cycle(prefs.streak() - 1, step, 5)));
        addRow(R.string.row_snooze, this::snoozeText, step -> snooze());
        addRow(R.string.row_pin, () -> "", step ->
                startActivity(new Intent(this, PinActivity.class).putExtra(PinActivity.EXTRA_NEW, true)));
        rows.getChildAt(0).requestFocus();
        refresh();
    }

    /**
     * One row: OK calls change with +1; Left and Right call it with -1 and +1, so the number rows
     * step both ways while the other rows simply act on any of the three.
     */
    private void addRow(int label, Supplier<String> value, IntConsumer change) {
        View row = getLayoutInflater().inflate(R.layout.item_row, rows, false);
        ((TextView) row.findViewById(R.id.label)).setText(label);
        row.setTag(value);
        row.setOnClickListener(v -> {
            change.accept(1);
            refresh();
        });
        row.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode != KeyEvent.KEYCODE_DPAD_LEFT && keyCode != KeyEvent.KEYCODE_DPAD_RIGHT) return false;
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                change.accept(keyCode == KeyEvent.KEYCODE_DPAD_LEFT ? -1 : 1);
                refresh();
            }
            return true;
        });
        rows.addView(row);
    }

    @SuppressWarnings("unchecked")
    private void refresh() {
        for (int i = 0; i < rows.getChildCount(); i++) {
            View row = rows.getChildAt(i);
            ((TextView) row.findViewById(R.id.value)).setText(((Supplier<String>) row.getTag()).get());
        }
        ((TextView) findViewById(R.id.stats)).setText(prefs.today(this));
    }

    /** Off → 1 hour → 3 hours → until midnight → off. Snoozing also lets a locked round through. */
    private void snooze() {
        snoozeChoice = (snoozeChoice + 1) % 4;
        long now = System.currentTimeMillis();
        long until;
        switch (snoozeChoice) {
            case 1: until = now + HOUR; break;
            case 2: until = now + 3 * HOUR; break;
            case 3: until = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(); break;
            default: until = 0;
        }
        prefs.setSnoozeUntil(until);
        if (until > now) prefs.parentUnlock();
    }

    private String snoozeText() {
        long until = prefs.snoozeUntil();
        if (until <= System.currentTimeMillis()) return getString(R.string.snooze_none);
        LocalDateTime t = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(until), ZoneId.systemDefault());
        return getString(R.string.snooze_until, t.format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private static int indexOf(int minutes) {
        for (int i = 0; i < Prefs.MINUTE_CHOICES.length; i++) if (Prefs.MINUTE_CHOICES[i] == minutes) return i;
        return 3;
    }

    private static int cycle(int index, int step, int size) {
        return ((index + step) % size + size) % size;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }
}

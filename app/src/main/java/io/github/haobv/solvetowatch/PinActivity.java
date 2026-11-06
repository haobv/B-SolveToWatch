package io.github.haobv.solvetowatch;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Four-digit PIN pad for the remote. RESULT_OK when the PIN matches, or when a new one is saved. */
public class PinActivity extends Activity {
    static final String EXTRA_NEW = "new";

    private Prefs prefs;
    private boolean newPin;
    private final StringBuilder entered = new StringBuilder();
    private TextView dots, message;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_pin);
        prefs = new Prefs(this);
        newPin = getIntent().getBooleanExtra(EXTRA_NEW, false);
        ((TextView) findViewById(R.id.title)).setText(newPin ? R.string.pin_new : R.string.pin_title);
        dots = findViewById(R.id.dots);
        message = findViewById(R.id.message);
        GridLayout keys = findViewById(R.id.keys);
        String[] labels = {"1", "2", "3", "4", "5", "6", "7", "8", "9",
                getString(R.string.key_delete), "0", getString(R.string.key_cancel)};
        LayoutInflater inflater = getLayoutInflater();
        for (String label : labels) {
            TextView key = (TextView) inflater.inflate(R.layout.item_key, keys, false);
            key.setText(label);
            key.setOnClickListener(v -> press(label));
            keys.addView(key);
        }
        keys.getChildAt(0).requestFocus();
        showDots();
    }

    private void press(String label) {
        if (label.equals(getString(R.string.key_cancel))) {
            finish();
        } else if (label.equals(getString(R.string.key_delete))) {
            if (entered.length() > 0) entered.setLength(entered.length() - 1);
            showDots();
        } else {
            digit(label.charAt(0));
        }
    }

    private void digit(char d) {
        if (entered.length() >= 4) return;
        entered.append(d);
        message.setText("");
        showDots();
        if (entered.length() < 4) return;
        if (newPin) {
            prefs.setPin(entered.toString());
            Toast.makeText(this, R.string.pin_saved, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else if (entered.toString().equals(prefs.pin())) {
            setResult(RESULT_OK);
            finish();
        } else {
            entered.setLength(0);
            message.setText(R.string.pin_wrong);
            showDots();
        }
    }

    private void showDots() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 4; i++) s.append(i < entered.length() ? '●' : '○');
        dots.setText(s);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            digit((char) ('0' + keyCode - KeyEvent.KEYCODE_0));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

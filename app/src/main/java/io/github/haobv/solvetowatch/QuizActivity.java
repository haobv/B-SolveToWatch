package io.github.haobv.solvetowatch;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

public class QuizActivity extends Activity {
    static final String EXTRA_APP = "app";
    private static final int REQ_PIN = 1;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private Prefs prefs;
    private ImageView gugu;
    private TextView title, question, hint;
    private LinearLayout dots;
    private ViewGroup answers;
    private MathQuestion current;
    private int grade, streak, target;
    private boolean busy, done;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_quiz);
        prefs = new Prefs(this);
        grade = getResources().getInteger(R.integer.grade);
        gugu = findViewById(R.id.gugu);
        title = findViewById(R.id.title);
        question = findViewById(R.id.question);
        hint = findViewById(R.id.hint);
        dots = findViewById(R.id.dots);
        answers = findViewById(R.id.answers);
        title.setText(getString(R.string.quiz_title, getString(R.string.character_name), getString(R.string.child_name)));
        ((TextView) findViewById(R.id.status)).setText(getString(R.string.paused, appName(getIntent().getStringExtra(EXTRA_APP))));
        for (int i = 0; i < answers.getChildCount(); i++) {
            final int index = i;
            answers.getChildAt(i).setOnClickListener(v -> answer(index));
        }
        findViewById(R.id.parent).setOnClickListener(v ->
                startActivityForResult(new Intent(this, PinActivity.class), REQ_PIN));
        target = prefs.streak();
        float dp = getResources().getDisplayMetrics().density;
        for (int i = 0; i < target; i++) {
            ImageView dot = new ImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (18 * dp), (int) (18 * dp));
            lp.setMargins((int) (5 * dp), 0, (int) (5 * dp), 0);
            dots.addView(dot, lp);
        }
        nextQuestion();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // A parent may have unlocked or snoozed from elsewhere.
        if (!done && (!prefs.locked() || !prefs.active())) finish();
    }

    private void nextQuestion() {
        busy = false;
        current = MathQuestion.next(grade, random);
        gugu.setImageResource(R.drawable.gugu_teacher);
        question.setText(current.text + " = ?");
        for (int i = 0; i < answers.getChildCount(); i++) {
            ((TextView) answers.getChildAt(i)).setText(String.valueOf(current.choices.get(i)));
        }
        hint.setText(getString(R.string.quiz_hint, target));
        updateDots();
        answers.getChildAt(0).requestFocus();
    }

    private void answer(int index) {
        if (busy) return;
        busy = true;
        boolean correct = current.choices.get(index) == current.answer;
        prefs.recordAnswer(correct);
        if (correct) {
            streak++;
            updateDots();
            if (streak >= target) {
                celebrate();
                return;
            }
            hint.setText(R.string.quiz_right);
            handler.postDelayed(this::nextQuestion, 900);
        } else {
            streak = 0;
            updateDots();
            gugu.setImageResource(R.drawable.gugu);
            hint.setText(getString(R.string.quiz_wrong, current.text, current.answer));
            handler.postDelayed(this::nextQuestion, 2500);
        }
    }

    private void celebrate() {
        done = true;
        prefs.passQuiz();
        gugu.setImageResource(R.drawable.gugu_happy);
        title.setVisibility(View.INVISIBLE);
        answers.setVisibility(View.GONE);
        question.setText(R.string.quiz_done);
        hint.setText(getString(R.string.quiz_done_hint, prefs.minutes()));
        hint.setTextSize(22);
        handler.postDelayed(() -> {
            finish();
            handler.postDelayed(() -> Media.play(getApplicationContext()), 700);
        }, 2000);
    }

    private String appName(String pkg) {
        try {
            PackageManager pm = getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
            return "YouTube";
        }
    }

    private void updateDots() {
        for (int i = 0; i < dots.getChildCount(); i++) {
            ((ImageView) dots.getChildAt(i)).setImageResource(i < streak ? R.drawable.dot_done : R.drawable.dot_empty);
        }
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (request == REQ_PIN && result == RESULT_OK) {
            done = true;
            prefs.parentUnlock();
            startActivity(new Intent(this, SettingsActivity.class).putExtra(SettingsActivity.EXTRA_AUTHED, true));
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Back does not leave the quiz; Home does, and YouTube brings it back.
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        if (!done) handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}

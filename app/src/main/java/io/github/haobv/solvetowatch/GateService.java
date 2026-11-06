package io.github.haobv.solvetowatch;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Counts the time a gated app (config.xml gated_packages) is on screen. When a round is used up it
 * pauses the video and puts the quiz on top; while the round stays locked, a gated app coming back to
 * the front brings the quiz back.
 * Nothing is filtered: the service only reads which app is in front.
 */
public class GateService extends AccessibilityService {
    private static final long TICK_MS = 5000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = this::onTick;
    private Prefs prefs;
    private Set<String> gated;
    private String inFront;
    private boolean ticking;
    private long lastTick;

    @Override
    protected void onServiceConnected() {
        prefs = new Prefs(this);
        gated = new HashSet<>(Arrays.asList(getResources().getStringArray(R.array.gated_packages)));
        evaluate();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        evaluate();
    }

    @Override
    public void onInterrupt() {
    }

    private void evaluate() {
        if (prefs == null) return;
        if (!gatedAppInFront() || !prefs.active()) {
            stopTicking();
        } else if (prefs.locked()) {
            stopTicking();
            showQuiz();
        } else if (!ticking) {
            ticking = true;
            lastTick = SystemClock.elapsedRealtime();
            handler.postDelayed(tick, TICK_MS);
        }
    }

    private void onTick() {
        ticking = false;
        long now = SystemClock.elapsedRealtime();
        int seconds = (int) ((now - lastTick) / 1000);
        lastTick = now;
        if (!gatedAppInFront() || !prefs.active() || !getSystemService(PowerManager.class).isInteractive()) return;
        prefs.addWatched(seconds);
        if (prefs.watchedSeconds() >= prefs.minutes() * 60) {
            prefs.lock();
            showQuiz();
        } else {
            ticking = true;
            handler.postDelayed(tick, TICK_MS);
        }
    }

    private void stopTicking() {
        handler.removeCallbacks(tick);
        ticking = false;
    }

    private void showQuiz() {
        Media.pause(this);
        startActivity(new Intent(this, QuizActivity.class).putExtra(QuizActivity.EXTRA_APP, inFront)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /** The top-most app window belongs to a gated app (overlays such as the volume bar are not app windows). */
    private boolean gatedAppInFront() {
        AccessibilityWindowInfo top = null;
        for (AccessibilityWindowInfo w : getWindows()) {
            if (w.getType() == AccessibilityWindowInfo.TYPE_APPLICATION && (top == null || w.getLayer() > top.getLayer())) {
                top = w;
            }
        }
        if (top == null) return false;
        AccessibilityNodeInfo root = top.getRoot();
        if (root == null || root.getPackageName() == null) return false;
        inFront = root.getPackageName().toString();
        return gated.contains(inFront);
    }
}

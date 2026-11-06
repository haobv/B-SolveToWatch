package io.github.haobv.solvetowatch;

import android.content.Context;
import android.media.AudioManager;
import android.view.KeyEvent;

/** Pause and resume whatever is playing, through the media key path every player listens to. */
final class Media {
    static void pause(Context c) { send(c, KeyEvent.KEYCODE_MEDIA_PAUSE); }
    static void play(Context c) { send(c, KeyEvent.KEYCODE_MEDIA_PLAY); }

    private static void send(Context c, int key) {
        AudioManager am = c.getSystemService(AudioManager.class);
        am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, key));
        am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, key));
    }
}

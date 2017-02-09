package uk.co.nevarneyok.controllers;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.view.View;

import uk.co.nevarneyok.R;

/**
 * Created by mcagrikarakaya on 8.02.2017.
 * Kişi listesi ile ilgili işlemler
 */

public final class BeepRunnable implements Runnable {
    private final MediaPlayer mediaPlayer;
    private final View view;
    private final int repeats;
    private final int interval;
    private int currentRepeat;

    public BeepRunnable(@NonNull View view, int repeats, int interval) {
        this.view = view;
        mediaPlayer = MediaPlayer.create(this.view.getContext(), R.raw.music);
        this.repeats = repeats;
        this.interval = interval;
    }

    @Override
    public void run() {
        mediaPlayer.start();
        if (currentRepeat < repeats) {
            // set to beep again
            currentRepeat = currentRepeat + 1;
            view.postDelayed(this, interval);
        }
        else {
            // beep is over, just reset the counter
            reset();
        }
    }

    public void reset() {
        currentRepeat = 0;
    }

    public void destroy() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        mediaPlayer.release();
        view.removeCallbacks(this);
    }
}
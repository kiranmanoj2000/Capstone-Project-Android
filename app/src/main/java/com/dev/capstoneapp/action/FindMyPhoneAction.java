package com.dev.capstoneapp.action;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;


public final class FindMyPhoneAction {
    static final ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    static final Handler handler = new Handler();

    public static void startTone(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,100);
                handler.postDelayed(this,250);
            }
        },100);
    }

    public static void endTone(){
        handler.removeMessages(0);
    }
}

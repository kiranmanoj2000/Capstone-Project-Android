package com.dev.capstoneapp.action;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Handler;


public class FindMyPhoneAction {
    static final ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    static final Handler handler = new Handler();
    private Context context;
    public boolean isPlayingTone;

    public FindMyPhoneAction(Context context){
        this.context = context;
    }

    public void startTone(){
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isPlayingTone = true;
                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,100);
                handler.postDelayed(this,250);
            }
        },500);
    }

    public void stopTone(){
        handler.removeCallbacksAndMessages(null);
        isPlayingTone = false;
    }

    public boolean isPlayingTone(){
        return isPlayingTone;
    }

}

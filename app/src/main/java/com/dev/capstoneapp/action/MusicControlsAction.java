package com.dev.capstoneapp.action;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.SystemClock;
import android.view.KeyEvent;

public class MusicControlsAction {
   private Context context;
   private AudioManager audioManager;

   public MusicControlsAction(Context context){
      this.context = context;
      this.audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
   }

   public void togglePause(){
      this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
      long eventtime = SystemClock.uptimeMillis();
      if(audioManager.isMusicActive()){
         KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE, 0);
         audioManager.dispatchMediaKeyEvent(downEvent);
      }
      else{
         KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
         audioManager.dispatchMediaKeyEvent(downEvent);
      }
   }

   public void playNext(){
      this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
      long eventtime = SystemClock.uptimeMillis();
      if(audioManager.isMusicActive()){
         KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
         audioManager.dispatchMediaKeyEvent(downEvent);
      }
   }

   public void playPrevious(){
      this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
      long eventtime = SystemClock.uptimeMillis();
      if(audioManager.isMusicActive()){
         KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0);
         audioManager.dispatchMediaKeyEvent(downEvent);
      }
   }
}

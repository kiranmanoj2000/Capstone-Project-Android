package com.dev.capstoneapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.dev.capstoneapp.R;
import com.dev.capstoneapp.receiver.NotificationListenerReceiver;

public class MyNotificationListenerService extends NotificationListenerService {
   private int status = 0;

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      super.onStartCommand(intent, flags, startId);
      if(intent.getAction().equals(NotificationListenerReceiver.ACTION_STOP_NOTIFICATION_LISTENER)){
         stopForeground(true);
         stopSelf();
         status = 0;
      } else {
         status = 1;
         startForeground(2, buildNotification());
      }
      return START_STICKY;

   }

   public Notification buildNotification(){
      String NOTIFICATION_CHANNEL_ID = "com.dev.capstoneproject";
      String channelName = "My Background Service";
      NotificationChannel chan = null;
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
         chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         chan.setLightColor(Color.BLUE);
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
      }
      NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      assert manager != null;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         manager.createNotificationChannel(chan);
      }

      NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
      Notification notification = notificationBuilder.setOngoing(true)
              .setSmallIcon(R.drawable.ic_launcher_background)
              .setContentTitle("App is running in background")
              .setPriority(NotificationManager.IMPORTANCE_MIN)
              .setCategory(Notification.CATEGORY_SERVICE)
              .build();
      return notification;
   }

   @Override
   public void onNotificationPosted(final StatusBarNotification sbn) {
      if(status == 0)
         return;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
         //sbn.getPackageName()
         Notification notification = sbn.getNotification();
         String text = notification.extras.get(Notification.EXTRA_BIG_TEXT).toString();

         Toast.makeText(getApplicationContext(),"Hellooo",Toast.LENGTH_SHORT).show();
      }
   }

   @Override
   public void onNotificationRemoved(StatusBarNotification sbn) {
      if(status == 0)
         return;
      Toast.makeText(getApplicationContext(),"swiped",Toast.LENGTH_SHORT).show();
   }

   @Override
   public void onListenerDisconnected() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
         // Notification listener disconnected - requesting rebind
         requestRebind(new ComponentName(this, MyNotificationListenerService.class));
      }
   }


}

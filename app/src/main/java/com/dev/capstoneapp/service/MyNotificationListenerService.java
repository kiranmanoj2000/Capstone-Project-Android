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

import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.text.nlclassifier.BertNLClassifier;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MyNotificationListenerService extends NotificationListenerService {
   private int status = 0;
   public static final String NOTIFY_ACTION = "com.dev.capstoneapp.service.NOTIFY_ACTION";
   private BertNLClassifier.BertNLClassifierOptions options;
   private BertNLClassifier classifier;

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      super.onStartCommand(intent, flags, startId);
      if(intent.getAction().equals(NotificationListenerReceiver.ACTION_STOP_NOTIFICATION_LISTENER)){
         stopForeground(true);
         stopSelf();
         status = 0;
      } else {
         options = BertNLClassifier.BertNLClassifierOptions.builder()
                 .setBaseOptions(BaseOptions.builder().setNumThreads(4).build())
                 .build();
         try {
            classifier =
                    BertNLClassifier.createFromFileAndOptions(this, "model.tflite", options);
         } catch (IOException e) {
            e.printStackTrace();
         }

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
              .setContentTitle("Intelligent notification listening enabled")
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
         String text = notification.extras.get(Notification.EXTRA_TEXT).toString();
         List<Category> results = classifier.classify(text);
         // if over 50% likely its urgent
         if(results.get(1).getScore() > 0.5){
            this.getApplicationContext().sendBroadcast(new Intent().setAction(NOTIFY_ACTION));
         }
      }
   }

   @Override
   public void onListenerDisconnected() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
         // Notification listener disconnected - requesting rebind
         requestRebind(new ComponentName(this, MyNotificationListenerService.class));
      }
   }


}

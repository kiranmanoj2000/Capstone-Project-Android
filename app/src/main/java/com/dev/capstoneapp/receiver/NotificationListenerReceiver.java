package com.dev.capstoneapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.dev.capstoneapp.service.MyNotificationListenerService;

public class NotificationListenerReceiver extends BroadcastReceiver {
    public static final String ACTION_START_NOTIFICATION_LISTENER =
            "com.dev.capstoneapp.receiver.ACTION_START_NOTIFICATION_LISTENER";
    public static final String ACTION_STOP_NOTIFICATION_LISTENER =
            "com.dev.capstoneapp.receiver.ACTION_STOP_NOTIFICATION_LISTENER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(NotificationListenerReceiver.ACTION_START_NOTIFICATION_LISTENER) || intent.getAction().equals(NotificationListenerReceiver.ACTION_STOP_NOTIFICATION_LISTENER)) {
            Intent startIntent = new Intent(context, MyNotificationListenerService.class);
            startIntent.setAction(intent.getAction());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent);
            }
        }
    }
}

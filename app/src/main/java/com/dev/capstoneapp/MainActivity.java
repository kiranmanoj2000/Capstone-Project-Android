package com.dev.capstoneapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.dev.capstoneapp.receiver.NotificationListenerReceiver;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Settings.Secure.getString(this.getContentResolver(),"enabled_notification_listeners").contains(getApplicationContext().getPackageName()))
        {
            //service is enabled do something
        } else {
            //service is not enabled try to enabled by calling...
            this.startActivity(new Intent(
                    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }
    }

    public void onStartListener(View v){
        Intent in = new Intent(NotificationListenerReceiver.ACTION_START_NOTIFICATION_LISTENER);
        in.setComponent(new ComponentName(getApplicationContext(), NotificationListenerReceiver.class));
        sendBroadcast(in);
    }

    public void onStopListener(View v){
        Intent in = new Intent(NotificationListenerReceiver.ACTION_STOP_NOTIFICATION_LISTENER);
        in.setComponent(new ComponentName(getApplicationContext(), NotificationListenerReceiver.class));
        sendBroadcast(in);
    }
}
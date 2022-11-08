package com.dev.capstoneapp.receiver;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.dev.capstoneapp.service.MyNotificationListenerService;

public class BluetoothConnectionReceiver extends BroadcastReceiver {
    public static final String ACTION_START_NOTIFICATION_LISTENER =
            "com.dev.capstoneapp.receiver.ACTION_START_NOTIFICATION_LISTENER";
    public static final String ACTION_STOP_NOTIFICATION_LISTENER =
            "com.dev.capstoneapp.receiver.ACTION_STOP_NOTIFICATION_LISTENER";

    public static final String TAG = "Bluetooth Receiver";
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String name;
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (device != null) {
            name = device.getName();
            Log.v(TAG, "Device=" + device.getName());
        }
        else {
            name = "None";
        }

        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            Log.v(TAG, "connected: " + device);
            Toast.makeText(context,"NAME CONNECTED: " + name,
                    Toast.LENGTH_SHORT).show();
        }
        else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            Toast.makeText(context,"NAME disconnected: " + name,
                    Toast.LENGTH_SHORT).show();
            Log.v(TAG, "disconnected: " + device);
        }
    }
}
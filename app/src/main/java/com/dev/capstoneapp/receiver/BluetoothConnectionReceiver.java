package com.dev.capstoneapp.receiver;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.ToneGenerator;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.dev.capstoneapp.action.FindMyPhoneAction;
import com.dev.capstoneapp.action.MusicControlsAction;
import com.dev.capstoneapp.action.SOSMessageAction;

import java.io.IOException;

public class BluetoothConnectionReceiver extends BroadcastReceiver {
    public static final String ACTION_START_NOTIFICATION_LISTENER =
            "com.dev.capstoneapp.receiver.ACTION_START_NOTIFICATION_LISTENER";
    public static final String ACTION_STOP_NOTIFICATION_LISTENER =
            "com.dev.capstoneapp.receiver.ACTION_STOP_NOTIFICATION_LISTENER";

    public static final String TAG = "Bluetooth Receiver";
    public FindMyPhoneAction findMyPhoneAction;
    public MusicControlsAction musicControlsAction;
    public boolean started = false;

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
            if(findMyPhoneAction == null){
                findMyPhoneAction = new FindMyPhoneAction(context);
            }

            Log.v(TAG, "connected: " + device);
            Toast.makeText(context,"NAME CONNECTED: " + name, Toast.LENGTH_SHORT).show();
            try {
                new SOSMessageAction(context);
                //findMyPhoneAction.startTone();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            if(findMyPhoneAction == null){
                findMyPhoneAction = new FindMyPhoneAction(context);
            }

            if(musicControlsAction == null){
                musicControlsAction = new MusicControlsAction(context);
            }

//            if(findMyPhoneAction != null){
//                findMyPhoneAction.stopTone();
//            }
            if(!started){
                //findMyPhoneAction.startTone();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //findMyPhoneAction.stopTone();
                    }
                },10000);
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    musicControlsAction.togglePause();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            musicControlsAction.togglePause();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    musicControlsAction.playNext();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            musicControlsAction.playPrevious();
                                        }
                                    }, 5000);
                                }
                            }, 5000);

                        }
                    }, 5000);
                }
            }, 5000);


            Toast.makeText(context,"NAME disconnected: " + name,
                    Toast.LENGTH_SHORT).show();
            Log.v(TAG, "disconnected: " + device);
        }
    }
}
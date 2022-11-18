package com.dev.capstoneapp.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.dev.capstoneapp.R;
import com.dev.capstoneapp.action.FindMyPhoneAction;
import com.dev.capstoneapp.action.MusicControlsAction;
import com.dev.capstoneapp.action.SOSMessageAction;
import com.dev.capstoneapp.receiver.NotificationListenerReceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {

   private static final UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

   public static final UUID SERIAL_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
   public static final UUID SERIAL_RX_SERVICE_UUID = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");


   public static final UUID RX_CHAR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
   public static final UUID TX_CHAR_UUID = UUID.fromString("00002a05-0000-1000-8000-00805f9b34fb");

   private static final String SINGLE_TAP = "SingleTap";
   private static final String DOUBLE_TAP = "DoubleTap";
   private static final String TRIPLE_TAP = "TripleTap";
   private static final String LONG_PRESS = "LongPress";


   private Binder binder = new LocalBinder();

   public static final String TAG = "BluetoothLeService";
   private BluetoothGatt bluetoothGatt;

   private BluetoothAdapter bluetoothAdapter;
   private Context context;
   private final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

   public final static String ACTION_GATT_SERVICES_DISCOVERED =
           "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
   public final static String ACTION_GATT_CONNECTED =
           "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
   public final static String ACTION_GATT_DISCONNECTED =
           "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";

   private FindMyPhoneAction findMyPhoneAction;
   private MusicControlsAction musicControlsAction;

   private BroadcastReceiver notificationNotifyReceiver;



   @Nullable
   @Override
   public IBinder onBind(Intent intent) {
      return binder;
   }

   @SuppressLint("MissingPermission")
   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      super.onStartCommand(intent, flags, startId);
      String action = intent.getAction();


      if(action != null && action.equals(ACTION_STOP_FOREGROUND_SERVICE)){
         bluetoothGatt.close();
         bluetoothGatt = null;
         stopNotificationListener();
         stopForeground(true);
         stopSelfResult(startId);
         return START_NOT_STICKY;
      }


      context = this;
      findMyPhoneAction = new FindMyPhoneAction(this);
      musicControlsAction = new MusicControlsAction(this);

      final IntentFilter theFilter = new IntentFilter();
      theFilter.addAction(MyNotificationListenerService.NOTIFY_ACTION);
      this.notificationNotifyReceiver = new BroadcastReceiver() {

         @Override
         public void onReceive(Context context, Intent intent) {
            send("T");
         }
      };
      this.registerReceiver(this.notificationNotifyReceiver, theFilter);

      startForeground(START_STICKY, buildNotification());
      return START_STICKY;
   }

   public Notification buildNotification(){
      String NOTIFICATION_CHANNEL_ID = "com.dev.capstoneproject";
      String channelName = "Bluetooth Service";
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

      Intent cancelIntent = new Intent(this, this.getClass());
      cancelIntent.setAction(ACTION_STOP_FOREGROUND_SERVICE);
      PendingIntent pendingCancelIntent = PendingIntent.getService(this, 0, cancelIntent, 0);
      NotificationCompat.Action cancelAction = new NotificationCompat.Action(android.R.drawable.ic_media_play, "Cancel", pendingCancelIntent);

      NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
      Notification notification = notificationBuilder.setOngoing(true)
              .setSmallIcon(R.drawable.ic_launcher_background)
              .setContentTitle("Ring connected via BLE")
              .setPriority(NotificationManager.IMPORTANCE_MIN)
              .setCategory(Notification.CATEGORY_SERVICE)
              .addAction(cancelAction)
              .build();
      return notification;
   }

   public class LocalBinder extends Binder {
      public BluetoothLeService getService() {
         return BluetoothLeService.this;
      }
   }

   public boolean initialize() {
      bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if (bluetoothAdapter == null) {
         Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
         return false;
      }
      return true;
   }


   @SuppressLint("MissingPermission")
   public boolean connect(final String address, Context context) {
      if (bluetoothAdapter == null || address == null) {
         Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
         return false;
      }
      try {
         final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
         // connect to the GATT server on the device
         bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
         return true;
      } catch (IllegalArgumentException exception) {
         Log.w(TAG, "Device not found with provided address.  Unable to connect.");
         return false;
      }
   }

   public void toast(String message){
      new Handler(Looper.getMainLooper()).post(new Runnable() {
         @Override
         public void run() {
            Toast.makeText(BluetoothLeService.this.getApplicationContext(), message, Toast.LENGTH_LONG).show();               }
      });
   }

   private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
      @SuppressLint("MissingPermission")
      @Override
      public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
         if (newState == BluetoothProfile.STATE_CONNECTED) {
            // successfully connected to the GATT Server
            toast("Successfully connected to BLE device");
            BluetoothLeService.this.getApplicationContext().sendBroadcast(new Intent().setAction(ACTION_GATT_CONNECTED));
            // Attempts to discover services after successful connection.
            bluetoothGatt.discoverServices();
         } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            // disconnected from the GATT Server
            BluetoothLeService.this.getApplicationContext().sendBroadcast(new Intent().setAction(ACTION_GATT_DISCONNECTED));

            toast("Disconnected from BLE device");

         }
      }


      @Override
      public void onServicesDiscovered(BluetoothGatt gatt, int status) {
         if (status == BluetoothGatt.GATT_SUCCESS) {
            BluetoothLeService.this.getApplicationContext().sendBroadcast(new Intent().setAction(ACTION_GATT_SERVICES_DISCOVERED));
            //toast("Services discovered");
         } else {
            Log.w(TAG, "onServicesDiscovered received: " + status);
         }
      }

      @Override
      public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
         if (status == BluetoothGatt.GATT_SUCCESS) {
            //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            //toast("Characteristic read");
         }
      }


      @Override
      public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
         if (status == BluetoothGatt.GATT_SUCCESS) {
            //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            //toast("Characteristic written");
         }
      }


      @Override
      public void onCharacteristicChanged(
              BluetoothGatt gatt,
              BluetoothGattCharacteristic characteristic
      ) {
         String action = characteristic.getStringValue(0).replace("\n", "");
         try {
            actionHandler(action);
         } catch (IOException e) {
            e.printStackTrace();
         }
         //toast("characteristic changed: " + characteristic.getStringValue(0));
      }


   };


   @Override
   public boolean onUnbind(Intent intent) {
      //close();
      return super.onUnbind(intent);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      close();
   }

   @SuppressLint("MissingPermission")
   private void close() {
      if (bluetoothGatt == null) {
         return;
      }
      bluetoothGatt.close();
      bluetoothGatt = null;
      stopNotificationListener();
      stopForeground(true);
      stopSelf();

   }

   @SuppressLint("MissingPermission")
   public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
      if (bluetoothGatt == null) {
         Log.w(TAG, "BluetoothGatt not initialized");
         return;
      }
      bluetoothGatt.setCharacteristicNotification(characteristic, enabled);

      BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_UUID);
      descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
      bluetoothGatt.writeDescriptor(descriptor);
      return;
   }

   public List<BluetoothGattService> getSupportedGattServices () {
      if (bluetoothGatt == null) return null;
      List<BluetoothGattService> services = bluetoothGatt.getServices();
      BluetoothGattService serialService = bluetoothGatt.getService(SERIAL_SERVICE_UUID);
      BluetoothGattCharacteristic rxChar = serialService.getCharacteristic(RX_CHAR_UUID);

      setCharacteristicNotification(rxChar, true);
      startNotificationListener();
      return services;
   }

   public void startNotificationListener(){
      Intent in = new Intent(NotificationListenerReceiver.ACTION_START_NOTIFICATION_LISTENER);
      in.setComponent(new ComponentName(getApplicationContext(), NotificationListenerReceiver.class));
      sendBroadcast(in);
   }
   public void stopNotificationListener(){
      Intent in = new Intent(NotificationListenerReceiver.ACTION_STOP_NOTIFICATION_LISTENER);
      in.setComponent(new ComponentName(getApplicationContext(), NotificationListenerReceiver.class));
      sendBroadcast(in);
   }


   public void send(String string) {
      int len = string.length(); int pos = 0;
      StringBuilder stringBuilder = new StringBuilder();

      while (len != 0) {
         stringBuilder.setLength(0);
         if (len >= 20) {
            stringBuilder.append(string.toCharArray(), pos, 20);
            len -= 20;
            pos += 20;
         } else {
            stringBuilder.append(string.toCharArray(), pos, len);
            len = 0;
         }
         send(stringBuilder.toString().getBytes());
      }
   }

   @SuppressLint("MissingPermission")
   public void send(byte[] data) {
      BluetoothGattService serialService = bluetoothGatt.getService(SERIAL_SERVICE_UUID);
      BluetoothGattCharacteristic rxChar = serialService.getCharacteristic(RX_CHAR_UUID);

      rxChar.setValue(data);
      bluetoothGatt.writeCharacteristic(rxChar);
   }

   public void actionHandler(String action) throws IOException {
      switch (action){
         case LONG_PRESS:
            new SOSMessageAction(BluetoothLeService.this.getApplicationContext());
            break;
         case SINGLE_TAP:
            musicControlsAction.togglePause();
            break;
         case DOUBLE_TAP:
            musicControlsAction.playNext();
            break;
         case TRIPLE_TAP:
            if (findMyPhoneAction.isPlayingTone()){
               findMyPhoneAction.stopTone();
            } else {
               findMyPhoneAction.startTone();
            }
            break;
         default:
            break;
      }
   }
}

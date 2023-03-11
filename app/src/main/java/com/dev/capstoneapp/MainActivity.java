package com.dev.capstoneapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dev.capstoneapp.action.FindMyPhoneAction;
import com.dev.capstoneapp.action.MusicControlsAction;
import com.dev.capstoneapp.action.SOSMessageAction;
import com.dev.capstoneapp.service.BluetoothLeService;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String UPDATE_THRESHOLD_VAL_ACTION = "UPDATE_THRESHOLD_VAL_ACTION";
    private EditText editEmergencyContactNumberInput;
    private TextView statusTextView;

    private EditText thresholdValueBox;

    private SharedPreferences sharedPref;
    private String emergencyContactNumber;
    private int userThreshVal;

    private SharedPreferences.Editor editor;

    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning = false;
    private Handler handler = new Handler();

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeService bluetoothService;
    private Context context;
    public static final String RING_ADDRESS = "02:80:E1:00:00:A2";
    private String connectionStatus = "Status: Scanning";

    private Button connectButton;

    private BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                statusTextView.setText("Status: Connected!");
                connectButton.setEnabled(false);
                removeLoadingIcon();
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                statusTextView.setText("Status: Disconnected");
                connectButton.setEnabled(true);
                removeLoadingIcon();
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
//                List<BluetoothGattService> services = bluetoothService.getSupportedGattServices();
//                BluetoothGattService n = services.get(0);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());

        statusTextView = (TextView)findViewById(R.id.statusTextView);
        statusTextView.setText(connectionStatus);
        thresholdValueBox = (EditText)findViewById(R.id.thresholdValueBox);



        connectButton = (Button) findViewById(R.id.connectButton);

        sharedPref = this.getSharedPreferences("com.dev.capstoneapp", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        editEmergencyContactNumberInput = (EditText) findViewById(R.id.editTextPhone);
        emergencyContactNumber = sharedPref.getString("emergencyContactNumber", "");
        if(!emergencyContactNumber.equals("")){
            editEmergencyContactNumberInput.setText(emergencyContactNumber);
        }

        userThreshVal = sharedPref.getInt("userThreshVal", -1);
        if(userThreshVal != -1){
            thresholdValueBox.setText(String.valueOf(userThreshVal));
        }

        editEmergencyContactNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                editor.putString("emergencyContactNumber", editEmergencyContactNumberInput.getText().toString());
                editor.apply();
            }
        });

        thresholdValueBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!thresholdValueBox.getText().toString().equals("")){
                    int val = Integer.parseInt(thresholdValueBox.getText().toString());
                    editor.putInt("userThreshVal", val);
                    editor.apply();
//                    Intent intent = new Intent();
//                    intent.setAction(MainActivity.UPDATE_THRESHOLD_VAL_ACTION);
//                    intent.putExtra("val", val);
//                    sendBroadcast(intent);
                }

            }
        });
        // TODO implement permisionns request

        if (Settings.Secure.getString(this.getContentResolver(),"enabled_notification_listeners").contains(getApplicationContext().getPackageName()))
        {
            //service is enabled do something
        } else {
            //service is not enabled try to enabled by calling...
            this.startActivity(new Intent(
                    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            bluetoothManager = this.getSystemService(BluetoothManager.class);
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        context = this;

        scanLeDevice();

        // REMOVE LATER
//        Intent in = new Intent(NotificationListenerReceiver.ACTION_START_NOTIFICATION_LISTENER);
//        in.setComponent(new ComponentName(getApplicationContext(), NotificationListenerReceiver.class));
//        sendBroadcast(in);

    }


    @SuppressLint("MissingPermission")
    private void scanLeDevice() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    //private LeDeviceListAdapter leDeviceListAdapter = new LeDeviceListAdapter();

    // Device scan callback.
    private final ScanCallback leScanCallback = new ScanCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if(result.getDevice().getAddress().equals(RING_ADDRESS)){
                        // enable the connect button
                        statusTextView.setText("Status: Device found");
                        removeLoadingIcon();
                        connectButton.setEnabled(true);
                        bluetoothLeScanner.stopScan(this);
                        scanning = false;
                    }
                }
    };


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothService = ((BluetoothLeService.LocalBinder) service).getService();
            if (bluetoothService != null) {
                if (!bluetoothService.initialize()) {
                    Log.e("MAIN ACTIVITY", "Unable to initialize Bluetooth");
                    finish();
                }
                // perform device connection
                bluetoothService.connect(RING_ADDRESS, context);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //bluetoothService = null;
        }
    };

    @Override
    protected void onDestroy() {
        if (gattUpdateReceiver != null) {
            unregisterReceiver(gattUpdateReceiver);
            gattUpdateReceiver = null;
        }
        super.onDestroy();
    }

    public void onClickConnect(View view){
        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        startService(gattServiceIntent);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        statusTextView.setText("Status: Connecting...");
        showLoadingIcon();
        connectButton.setEnabled(false);
    }

    public void removeLoadingIcon(){
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

    }

    public void showLoadingIcon(){
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
    }


}
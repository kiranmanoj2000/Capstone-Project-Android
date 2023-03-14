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
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.capstoneapp.action.FindMyPhoneAction;
import com.dev.capstoneapp.action.MusicControlsAction;
import com.dev.capstoneapp.action.SOSMessageAction;
import com.dev.capstoneapp.adapter.InputMethodAdapter;
import com.dev.capstoneapp.models.InputMethod;
import com.dev.capstoneapp.service.BluetoothLeService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static String UPDATE_THRESHOLD_VAL_ACTION = "UPDATE_THRESHOLD_VAL_ACTION";
    private EditText editEmergencyContactNumberInput;
    private TextView statusTextView;

    private EditText thresholdValueBox;

    private Spinner sosSpinner;
    private Spinner toggleMusicSpinner;
    private Spinner playNextSpinner;
    private Spinner playPrevSpinner;
    private Spinner findMySpinner;

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
    private boolean isEditing = false;
    private TableLayout table;

    public ArrayList<InputMethod>  inputMethods = new ArrayList<>();
    public ArrayList<Spinner>  spinners = new ArrayList<>();



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
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());

        sharedPref = this.getSharedPreferences("com.dev.capstoneapp", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        inputMethods.add(new InputMethod("Single tap", BluetoothLeService.SINGLE_TAP));
        inputMethods.add(new InputMethod("Double tap", BluetoothLeService.DOUBLE_TAP));
        inputMethods.add(new InputMethod("Triple tap", BluetoothLeService.TRIPLE_TAP));
        inputMethods.add(new InputMethod("Long press", BluetoothLeService.LONG_PRESS));
        inputMethods.add(new InputMethod("Double tap + Long press", BluetoothLeService.DOUBLE_TAP_AND_LONG_PRESS));

        // set default pos of each spinner in sharedPref the if not already saved
        initDefaultInputMappingSharedPref();




        statusTextView = (TextView)findViewById(R.id.statusTextView);
        statusTextView.setText(connectionStatus);
        thresholdValueBox = (EditText)findViewById(R.id.thresholdValueBox);

        View featureList = findViewById(R.id.featureList);

        sosSpinner = (Spinner) featureList.findViewById(R.id.sos_spinner);
        toggleMusicSpinner = (Spinner) featureList.findViewById(R.id.toggle_music_spinner);
        playNextSpinner = (Spinner) featureList.findViewById(R.id.play_next_spinner);
        playPrevSpinner = (Spinner) featureList.findViewById(R.id.play_prev_spinner);
        findMySpinner = (Spinner) featureList.findViewById(R.id.find_phone_spinner);
        setDropDownStatus(false);

        InputMethodAdapter sosAdapter = new InputMethodAdapter(MainActivity.this, inputMethods);
        InputMethodAdapter toggleMusicAdapter = new InputMethodAdapter(MainActivity.this, inputMethods);
        InputMethodAdapter playNextAdapter = new InputMethodAdapter(MainActivity.this, inputMethods);
        InputMethodAdapter playPrevAdapter = new InputMethodAdapter(MainActivity.this, inputMethods);
        InputMethodAdapter findMyAdapter = new InputMethodAdapter(MainActivity.this, inputMethods);

        sosSpinner.setAdapter(sosAdapter);
        // get it from local storage

        sosSpinner.setSelection(4);

        toggleMusicSpinner.setAdapter(toggleMusicAdapter);
        toggleMusicSpinner.setSelection(0);

        playNextSpinner.setAdapter(playNextAdapter);
        playNextSpinner.setSelection(1);

        playPrevSpinner.setAdapter(playPrevAdapter);
        playPrevSpinner.setSelection(2);

        findMySpinner.setAdapter(findMyAdapter);
        findMySpinner.setSelection(3);



        spinners.add(sosSpinner);
        spinners.add(toggleMusicSpinner);
        spinners.add(playNextSpinner);
        spinners.add(playPrevSpinner);
        spinners.add(findMySpinner);

        setSpinnerDefaultSelection(sosSpinner, "sos");
        setSpinnerDefaultSelection(toggleMusicSpinner, "toggleMusic");
        setSpinnerDefaultSelection(playNextSpinner, "playNext");
        setSpinnerDefaultSelection(playPrevSpinner, "playPrev");
        setSpinnerDefaultSelection(findMySpinner, "findMy");


        sosSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id)
                    {

                        handleFeatureInputMethodUpdate("sos", (InputMethod)
                                parent.getItemAtPosition(position), position);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent)
                    {
                    }
                });
        toggleMusicSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id)
                    {

                        handleFeatureInputMethodUpdate("toggleMusic", (InputMethod)
                                parent.getItemAtPosition(position), position);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent)
                    {
                    }
                });
        playNextSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id)
                    {

                        handleFeatureInputMethodUpdate("playNext", (InputMethod)
                                parent.getItemAtPosition(position), position);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent)
                    {
                    }
                });
        playPrevSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id)
                    {

                        handleFeatureInputMethodUpdate("playPrev", (InputMethod)
                                parent.getItemAtPosition(position), position);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent)
                    {
                    }
                });
        findMySpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id)
                    {

                        handleFeatureInputMethodUpdate("findMy", (InputMethod)
                                parent.getItemAtPosition(position), position);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent)
                    {
                    }
                });

        table = (TableLayout) featureList.findViewById(R.id.tableLayout);

        thresholdValueBox.setVisibility(View.INVISIBLE);
        connectButton = (Button) findViewById(R.id.connectButton);


        editEmergencyContactNumberInput = (EditText) findViewById(R.id.editTextPhone);
        emergencyContactNumber = sharedPref.getString("emergencyContactNumber", "");
        if(!emergencyContactNumber.equals("")){
            editEmergencyContactNumberInput.setText(emergencyContactNumber);
        }

        userThreshVal = sharedPref.getInt("userThreshVal", -1);
        if(userThreshVal != -1){
            thresholdValueBox.setText(String.valueOf(userThreshVal));
        }

        editEmergencyContactNumberInput.setEnabled(false);
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
                //editor.apply();
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

    }

    public void initDefaultInputMappingSharedPref(){
        boolean notInitialized = sharedPref.getInt("sos", -1) == -1;
        if(notInitialized){
            editor.putInt("sos", 4);
            editor.putString(inputMethods.get(4).getKey(), "sos");
            editor.putInt("toggleMusic", 0);
            editor.putString(inputMethods.get(0).getKey(), "toggleMusic");

            editor.putInt("playNext", 1);
            editor.putString(inputMethods.get(1).getKey(), "playNext");

            editor.putInt("playPrev", 2);
            editor.putString(inputMethods.get(2).getKey(), "playPrev");

            editor.putInt("findMy", 3);
            editor.putString(inputMethods.get(3).getKey(), "findMy");

            editor.commit();
        }
    }

    public void setSpinnerDefaultSelection(Spinner spinner, String feature){
        spinner.setSelection(sharedPref.getInt(feature, 0));
    }

    public void handleFeatureInputMethodUpdate(String feature, InputMethod inputMethod, int index){
        editor.putInt(feature, index);
        editor.putString(inputMethod.getKey(), feature);
    }

    public boolean areInputAssignmentsValid(){
        Set<Integer> set = new HashSet<>();
        for(Spinner s : spinners){
            if(set.contains(s.getSelectedItemPosition())){
                return false;
            }
            set.add(s.getSelectedItemPosition());
        }
        return true;
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

    public void onToggleEdit(View view) {
        if (isEditing) {
            if (areInputAssignmentsValid()) {
                editor.apply();
                editEmergencyContactNumberInput.setEnabled(false);
                setDropDownStatus(false);
                table.setBackgroundColor(Color.parseColor("#303030"));
                isEditing = !isEditing;
                Toast.makeText(this.getApplicationContext(), "Saved!", Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(this.getApplicationContext(), "An input can only be mapped to exactly one feature", Toast.LENGTH_LONG).show();
            table.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce_interpolator));

        }
        else{
            table.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce_interpolator));
            table.setBackgroundColor(getResources().getColor(R.color.purple_700));
            setDropDownStatus(true);
            editEmergencyContactNumberInput.setEnabled(true);


            isEditing = !isEditing;
        }

    }

    public void setDropDownStatus(boolean isEnabled){
        sosSpinner.setEnabled(isEnabled);
        sosSpinner.setClickable(isEnabled);

        toggleMusicSpinner.setEnabled(isEnabled);
        toggleMusicSpinner.setClickable(isEnabled);

        playNextSpinner.setEnabled(isEnabled);
        playNextSpinner.setClickable(isEnabled);

        playPrevSpinner.setEnabled(isEnabled);
        playPrevSpinner.setClickable(isEnabled);

        findMySpinner.setEnabled(isEnabled);
        findMySpinner.setClickable(isEnabled);
    }

    public void removeLoadingIcon(){
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

    }

    public void showLoadingIcon(){
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
    }


}
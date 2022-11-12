package com.dev.capstoneapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.dev.capstoneapp.action.MusicControlsAction;
import com.dev.capstoneapp.receiver.NotificationListenerReceiver;

public class MainActivity extends AppCompatActivity {

    private EditText editEmergencyContactNumberInput;
    private SharedPreferences sharedPref;
    private String emergencyContactNumber;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sharedPref = this.getSharedPreferences("com.dev.capstoneapp", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        editEmergencyContactNumberInput = (EditText) findViewById(R.id.editTextPhone);
        emergencyContactNumber = sharedPref.getString("emergencyContactNumber", "");
        if(!emergencyContactNumber.equals("")){
            editEmergencyContactNumberInput.setText(emergencyContactNumber);
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
        // TODO implement permisionns request

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
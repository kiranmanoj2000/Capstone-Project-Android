package com.dev.capstoneapp.action;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.dev.capstoneapp.R;
import com.dev.capstoneapp.service.BluetoothLeService;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SOSMessageAction implements LocationListener {

    private LocationManager locationManager;
    private Context context;

    @SuppressLint("MissingPermission")
    public SOSMessageAction(Context context) throws IOException {
        this.context = context;
        locationManager = (LocationManager)  this.context.getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        SmsManager.getDefault().sendTextMessage(getEmergencyContactNumber(), null, "HELP PLEASE. Last Location: "
                + "200 University Ave W, Waterloo, ON N2L 3G1" + "\n Latitude: " + "43.472258" +
                "\n Longitude: " + "-80.545287", null, null);
        notifyUser();


//        if (location != null) {
//            sendLocationToEmergencyContact(location);
//        }
//        else{
//            //This is what you need:
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this, Looper.getMainLooper());
//        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //remove location callback:
        locationManager.removeUpdates(this);
        try {
            sendLocationToEmergencyContact(location);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getLocation(double latitude, double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this.context, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//        String city = addresses.get(0).getLocality();
//        String state = addresses.get(0).getAdminArea();
//        String country = addresses.get(0).getCountryName();
//        String postalCode = addresses.get(0).getPostalCode();
//        String knownName = addresses.get(0).getFeatureName();
        return address;
    }

    public void sendLocationToEmergencyContact(Location location) throws IOException {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String address = getLocation(latitude, longitude);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "Location: " + address, Toast.LENGTH_LONG).show();               }
        });

        SmsManager.getDefault().sendTextMessage(getEmergencyContactNumber(), null, "HELP PLEASE. Last Location: "
                + address + "\n Latitude: " + latitude +
                "\n Longitude: " + longitude, null, null);
        notifyUser();
    }

    public String getEmergencyContactNumber(){
        SharedPreferences sharedPref = this.context.getSharedPreferences("com.dev.capstoneapp", Context.MODE_PRIVATE);
        return sharedPref.getString("emergencyContactNumber", "");
    }
    public void notifyUser(){
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("Channel_4", "Notify", NotificationManager.IMPORTANCE_HIGH);
        }
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifyManager.createNotificationChannel(channel);
        }
        Notification notify = new NotificationCompat.Builder(context.getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("SOS sent").setContentText("Your emergency contact has been notified of your location.")
                .setChannelId("Channel_4").build();
        notifyManager.notify((int) (Math.random()*1001), notify);
    }
}

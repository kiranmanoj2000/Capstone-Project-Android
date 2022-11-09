package com.dev.capstoneapp.action;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public final class SOSMessageAction {
    public static void sendSOS(Context context) throws IOException {
        SharedPreferences sharedPref = context.getSharedPreferences("com.dev.capstoneapp", Context.MODE_PRIVATE);
        String emergencyPhoneNumber = sharedPref.getString("emergencyContactNumber", "");
        Toast.makeText(context,"Emergency contact: " + emergencyPhoneNumber,
                Toast.LENGTH_SHORT).show();
        HashMap<String, Double> coordinates = getCoordinates(context);
        double latitude = coordinates.get("latitude");
        double longitude = coordinates.get("longitude");
        String address = getLocation(context, latitude, longitude);

        SmsManager.getDefault().sendTextMessage(emergencyPhoneNumber, null, "HELP PLEASE. Last Location: "
         + address + "\n Latitude: " + latitude +
                "\n Longitude: " + longitude, null, null);
    }

    public static HashMap<String, Double> getCoordinates(Context context){
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        HashMap<String, Double> res = new HashMap<>();
        res.put("latitude", latitude);
        res.put("longitude", longitude);
        return res;
    }
    public static String getLocation(Context context, double latitude, double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//        String city = addresses.get(0).getLocality();
//        String state = addresses.get(0).getAdminArea();
//        String country = addresses.get(0).getCountryName();
//        String postalCode = addresses.get(0).getPostalCode();
//        String knownName = addresses.get(0).getFeatureName();
        return address;
    }
}

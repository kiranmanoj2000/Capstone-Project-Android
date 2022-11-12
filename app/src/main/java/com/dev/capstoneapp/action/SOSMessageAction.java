package com.dev.capstoneapp.action;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
        if (location != null) {
            sendLocationToEmergencyContact(location);
        }
        else{
            //This is what you need:
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
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
        //open the map:

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
        Toast.makeText(context,"Location: " + address,
                Toast.LENGTH_LONG).show();
    }
}

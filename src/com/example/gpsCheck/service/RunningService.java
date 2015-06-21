package com.example.gpsCheck.service;

/**
 * Created by KLiakopoulos on 6/19/2015.
 */

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.example.gpsCheck.ExtApplication;


public class RunningService extends IntentService implements LocationListener{

    private int result = Activity.RESULT_CANCELED;
    public static final String LATLONLIST = "latLonList";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "com.example.gpsCheck";

    private LocationManager locationManager;

    public RunningService() {
        super("DownloadService");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);


        // Get the location manager
        if (((ExtApplication) getApplication()).running) {
            locationManager = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates("gps", 2000, 3, this);
        }
    }

    @Override
    public boolean stopService(Intent name) {

        locationManager.removeUpdates(this);
        return super.stopService(name);
    }

    @Override
    public void onLocationChanged(Location location) {


        ((ExtApplication) getApplication()).setLatLonList( ((ExtApplication) getApplication()).getLatLonList()+","+String.valueOf(location.getLatitude())+","+ String.valueOf(location.getLongitude()));

        return;

    }


    @Override
    public void onProviderEnabled(String provider) {
//        Toast.makeText(getActivity(), "Enabled provider " + provider,
//                Toast.LENGTH_SHORT).show();
//        selectUsernameSpinner.setClickable(true);
//        initializeMap();
//        LinearLayout noLocation  = (LinearLayout) getView().findViewById(R.id.noLocation);
//        noLocation.setVisibility(View.GONE);

    }

    @Override
    public void onProviderDisabled(String provider) {
//        Toast.makeText(getActivity(), "Disabled provider " + provider,
//                Toast.LENGTH_SHORT).show();
//
//        LinearLayout noLocation  = (LinearLayout) getView().findViewById(R.id.noLocation);
//        noLocation.setVisibility(View.VISIBLE);
//
//        selectProvider();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }



    // will be called asynchronously by Android
    @Override
    protected void onHandleIntent(Intent intent) {


        String fileName = intent.getStringExtra(LATLONLIST);

        publishResults();
    }

    private void publishResults() {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(LATLONLIST, "sdfdfs");
        sendBroadcast(intent);
    }


}
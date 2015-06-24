package com.example.gpsCheck.service;

/**
 * Created by KLiakopoulos on 6/19/2015.
 */

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.os.IBinder;
import android.widget.Toast;
import com.example.gpsCheck.ExtApplication;


public class RunningService extends Service implements LocationListener{

    private int result = Activity.RESULT_CANCELED;
    public static final String LATLONLIST = "latLonList";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "com.example.gpsCheck";


    private LocationManager locationManager;

    public RunningService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        if (intent!=null) {
            String opponent = intent.getStringExtra("username");
//            ((ExtApplication) getApplication()).setOpponentUsername(opponent);
            Toast.makeText(this, "Set name", Toast.LENGTH_SHORT).show();
        }


//        ((ExtApplication) getApplication()).setRunning(true);
        // Get the location manager
//        if (((ExtApplication) getApplication()).running) {
            locationManager = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates("gps", 2000, 3, this);
//        }


        // Let it continue running until it is stopped.
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public boolean stopService(Intent name) {

//        Toast.makeText(this, "Service Stopped "+ ((ExtApplication) getApplication()).isRunning(), Toast.LENGTH_LONG).show();

        locationManager.removeUpdates(this);
        return super.stopService(name);
    }

    @Override
    public void onLocationChanged(Location location) {

//        Toast.makeText(this,((ExtApplication) getApplication()).getOpponentUsername() +" ||||| "+ ((ExtApplication) getApplication()).getLatLonList()+" ||||| "+ ((ExtApplication) getApplication()).isRunning(), Toast.LENGTH_LONG).show();
//
//        if (((ExtApplication) getApplication()).getLatLonList().equals(""))
//             ((ExtApplication) getApplication()).setLatLonList(String.valueOf(location.getLatitude())+","+ String.valueOf(location.getLongitude()));
//        else
//            ((ExtApplication) getApplication()).setLatLonList( ((ExtApplication) getApplication()).getLatLonList()+","+String.valueOf(location.getLatitude())+","+ String.valueOf(location.getLongitude()));

        return;

    }


    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled service provider " + provider,
                Toast.LENGTH_SHORT).show();


    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled service provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }



}
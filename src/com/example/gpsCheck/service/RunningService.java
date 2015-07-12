package com.example.gpsCheck.service;

/**
 * Created by KLiakopoulos on 6/19/2015.
 */

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;

import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.example.gpsCheck.ExtApplication;


public class RunningService extends IntentService implements LocationListener{

    private int result = Activity.RESULT_CANCELED;
    public static final String LATLONLIST = "latLonList";
    public static final String NEW_LOCATION = "newLocation";
    public static final String OPPONENT = "opponent";
    public static final String TARGET_DIST = "targetDist";
    public static final String TOTAL_DIST = "totalDist";
    public static final String GOAL_REACHED = "finished";
    public static final String NOTIFICATION = "com.example.gpsCheck";


    public String latLonList;
    public String opponentUsername;
    public float targetDistance;
    public float totalDistance;
    public Location lastLocation;


    @Override
    protected void onHandleIntent(Intent intent) {

        Toast.makeText(this,  "handle intent", Toast.LENGTH_SHORT).show();
        opponentUsername=intent.getStringExtra(OPPONENT);
    }



    @Override
    public void onCreate() {
        super.onCreate();

//        Toast.makeText(this,  "on create", Toast.LENGTH_SHORT).show();

    }

    private LocationManager locationManager;

    public RunningService() {
        super("RunningService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        //it will start two times:
        //the first is with intent values
        //the second is with null intent and values from shared prefs

        if (intent!=null){//first
            opponentUsername = intent.getStringExtra(OPPONENT);
            latLonList= intent.getStringExtra(LATLONLIST);
            targetDistance = intent.getFloatExtra(TARGET_DIST, 0);
            totalDistance = intent.getFloatExtra(TOTAL_DIST, 0);
        }else{//second
            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
            opponentUsername =  app_preferences.getString(OPPONENT, "");
            latLonList = app_preferences.getString(LATLONLIST, "");
            targetDistance = app_preferences.getFloat(TARGET_DIST, 0);
            totalDistance = app_preferences.getFloat(TOTAL_DIST, 0);
        }

        locationManager = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates("gps", 2000, 3, this);

        return START_STICKY;
    }





    @Override
    public void onTaskRemoved(Intent rootIntent) {//killing the app here, store in shared prefs

        Toast.makeText(getApplication(), "removed", Toast.LENGTH_SHORT).show();

        ExtApplication application = (ExtApplication) getApplication().getApplicationContext();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);
        SharedPreferences.Editor editor = app_preferences.edit();

        editor.putString(LATLONLIST, latLonList);
        editor.putString(OPPONENT, opponentUsername);
        editor.putFloat(TARGET_DIST, targetDistance);
        editor.putFloat(TOTAL_DIST, totalDistance);

        editor.commit();

        super.onTaskRemoved(rootIntent);
    }

    @Override
    public boolean stopService(Intent name) {

        Toast.makeText(this, "Service Stopped programmaticaly", Toast.LENGTH_LONG).show();
        locationManager.removeUpdates(this);
        return super.stopService(name);
    }

    @Override
    public void onLocationChanged(Location location) {


        if (totalDistance<targetDistance) {

            Toast.makeText(this, "1. " + opponentUsername + " ||||| " + latLonList, Toast.LENGTH_LONG).show();

            if (latLonList.equals("")) {
                latLonList = String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude());
            } else {
                latLonList += "," + String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude());
            }

            if (lastLocation!=null){
                totalDistance+= lastLocation.distanceTo(location);
            }else{
                lastLocation = location;
            }



        }

        publishResults(String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()));
    }




    private void publishResults(String newLocation) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(NEW_LOCATION, newLocation);
        intent.putExtra(LATLONLIST, latLonList);
        intent.putExtra(OPPONENT, opponentUsername);
        intent.putExtra(GOAL_REACHED, totalDistance>=targetDistance);
        sendBroadcast(intent);
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

    public float getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(float totalDistance) {
        this.totalDistance = totalDistance;
    }

    public String getLatLonList() {
        return latLonList;
    }

    public void setLatLonList(String latLonList) {
        this.latLonList = latLonList;
    }

    public String getOpponentUsername() {
        return opponentUsername;
    }

    public void setOpponentUsername(String opponentUsername) {
        this.opponentUsername = opponentUsername;
    }

//    public class MyBinder extends Binder {
//        public RunningService getService() {
//            return RunningService.this;
//        }
//    }
}
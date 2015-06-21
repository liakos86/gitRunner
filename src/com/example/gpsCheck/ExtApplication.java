package com.example.gpsCheck;

import android.app.Application;
import android.content.Context;
//import com.parse.Parse;
//import com.parse.PushService;
//import org.acra.ACRA;
//import org.acra.ReportField;
//import org.acra.ReportingInteractionMode;
//import org.acra.annotation.ReportsCrashes;
//import org.acra.sender.HttpSender;
//import org.apache.http.client.CookieStore;
import android.support.v4.app.NotificationCompat;
import org.apache.http.impl.client.DefaultHttpClient;


import org.acra.*;
import org.acra.annotation.*;


import java.io.File;

@ReportsCrashes(formKey = "", // will not be used
        mailTo = "liakos86@gmail.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class ExtApplication extends Application {

//    private static final String TAG = Thread.currentThread().getStackTrace()[2].getClassName();


    private int position;

    public boolean running;
    String latLonList="";

    public NotificationCompat.Builder mBuilder;

    public NotificationCompat.Builder getmBuilder() {
        return mBuilder;
    }

    public void setmBuilder(NotificationCompat.Builder mBuilder) {
        this.mBuilder = mBuilder;
    }

    public String getLatLonList() {
        return latLonList;
    }

    public void setLatLonList(String latLonList) {
        this.latLonList = latLonList;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }

    public static ExtApplication getApplication(Context context) {
        return (ExtApplication) context.getApplicationContext();

    }

    /**
     * Builds a new HttpClient with the same CookieStore than the previous one.
     * This allows to follow the http session, without keeping in memory the
     * full DefaultHttpClient.
     */
    protected DefaultHttpClient getHttpClient() {
        final DefaultHttpClient httpClient = new DefaultHttpClient();
        return httpClient;
    }

    private Long userID;

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long id) {
        userID = id;
    }


    public void setExitCode(Long exitCode) {
        userID = exitCode;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
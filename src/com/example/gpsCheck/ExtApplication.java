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
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File; 

//@ReportsCrashes(formKey = "",
//        formUri = "http://10.40.48.65:7007/portal/client/cms/AppExceptionHandler.action",//FIXME itadev
//        httpMethod = HttpSender.Method.POST,
//        customReportContent = {ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT},
//        mode = ReportingInteractionMode.TOAST,
//        resToastText = R.string.crash_toast_text)
public class ExtApplication extends Application {

//    private static final String TAG = Thread.currentThread().getStackTrace()[2].getClassName();


    private int position;

    @Override
    public void onCreate() {
        super.onCreate();
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
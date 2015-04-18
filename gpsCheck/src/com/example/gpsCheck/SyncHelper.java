
package com.example.gpsCheck;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;
import com.example.gpsCheck.model.Database;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.gpsCheck.dbObjects.Running;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class SyncHelper {
    private static final String TAG = Thread.currentThread().getStackTrace()[2].getClassName()
            .substring(0, 23);

    private ExtApplication application;
    Database dbHelper;
    String workoutIP, runnerIP, challengeIP;
    DefaultHttpClient client;
//    private static final String prefix = "/portal";

    // private int tech1, tech2;
    private SharedPreferences app_preferences;

    private Activity activity;

    public SyncHelper(Activity activity) {

        this.activity = activity;
        workoutIP = activity.getResources().getString(R.string.workout_url);
        runnerIP = activity.getResources().getString(R.string.runner_url);
        challengeIP = activity.getResources().getString(R.string.challenge_url);
        application = (ExtApplication) activity.getApplicationContext();
        app_preferences = PreferenceManager.getDefaultSharedPreferences(application);
        dbHelper = new Database(application);
        client = application.getHttpClient();

    }

    private void saveRunnerId(JSONObject runner) throws JSONException{

        SharedPreferences.Editor editor = app_preferences.edit();

          editor.putString ("mongoId",     ((String)((JSONObject) runner.get("_id")).get("$oid") ));
          editor.commit();
    }



    public int insertMongoUser() {

        Log.v(TAG, "inserting user");

//        HashMap  params = new HashMap();
        DefaultHttpClient client = application.getHttpClient();
        client.setParams(getMyParams());
        HttpPost httpPost = new HttpPost(runnerIP);


        try {
            JSONObject runner = new JSONObject();
            runner.put("username", "kostas");
            runner.put("password", "a8a8a8");
            runner.put("email", "liak@liak.gr");
            runner.put("totalRuns", "10");
            runner.put("totalScore", "0");

//            JSONObject holder = getJsonObjectFromMap(params);

            //passes the results to a string builder/entity
//            StringEntity se = new StringEntity(holder.toString());
//            httpPost.setEntity(se);



            StringEntity se = new StringEntity( runner.toString());
            setDefaultPostHeaders(httpPost);
            httpPost.setEntity(se);

            Log.v(TAG, "Fetching runs - requesting");
            HttpResponse response = client.execute(httpPost);
            Log.v(TAG, "Fetching runs - responce received");

            HttpEntity entity = response.getEntity();

            StatusLine statusLine = response.getStatusLine();

            Log.v(TAG, String.format("Fetching stores - status [%s]", statusLine));

            if (statusLine.getStatusCode() >= 300) {
//                Toast.makeText(activity,R.string.server_error,Toast.LENGTH_LONG).show();
                Log.e(TAG,statusLine.getStatusCode()+" - "+statusLine.getReasonPhrase());
            }

            String resultString = null;

            if (entity != null) {
                InputStream instream = entity.getContent();
                Header contentEncoding = response.getFirstHeader("Content-Encoding");
                if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                    instream = new GZIPInputStream(instream);
                }
                resultString = Utils.convertStreamToString(instream);

                JSONObject runnerResponse = new JSONObject(resultString);

                saveRunnerId(runnerResponse);


                instream.close();
            }


        } catch (Exception e) {
            Log.e(TAG, "Exception fetching stores", e);
            return -1;
        }

        Log.v(TAG, String.format("Fetching stores - done"));

        return 0;

    }

    public int createMongoChallenge() {

        Log.v(TAG, "inserting challenge");

//        HashMap  params = new HashMap();
        DefaultHttpClient client = application.getHttpClient();
        client.setParams(getMyParams());
        HttpPost httpPost = new HttpPost(challengeIP);


        try {
            JSONObject runner = new JSONObject();
            runner.put("user_id", "hgfhgfghfhj");
            runner.put("distance", "10");
            runner.put("time", "100000");
            runner.put("date", "15apr2015 23:52:22");
            runner.put("opponent_id", "asdjnjfej");


            StringEntity se = new StringEntity( runner.toString());
            setDefaultPostHeaders(httpPost);
            httpPost.setEntity(se);

            Log.v(TAG, "Fetching runs - requesting");
            HttpResponse response = client.execute(httpPost);
            Log.v(TAG, "Fetching runs - responce received");

            HttpEntity entity = response.getEntity();

            StatusLine statusLine = response.getStatusLine();

            Log.v(TAG, String.format("Fetching stores - status [%s]", statusLine));

            if (statusLine.getStatusCode() >= 300) {
//                Toast.makeText(activity,R.string.server_error,Toast.LENGTH_LONG).show();
                Log.e(TAG,statusLine.getStatusCode()+" - "+statusLine.getReasonPhrase());
            }

            String resultString = null;

            if (entity != null) {
                InputStream instream = entity.getContent();
                Header contentEncoding = response.getFirstHeader("Content-Encoding");
                if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                    instream = new GZIPInputStream(instream);
                }
                resultString = Utils.convertStreamToString(instream);

                JSONObject runnerResponse = new JSONObject(resultString);

                saveRunnerId(runnerResponse);


                instream.close();
            }


        } catch (Exception e) {
            Log.e(TAG, "Exception fetching stores", e);
            return -1;
        }

        Log.v(TAG, String.format("Fetching stores - done"));

        return 0;

    }



    public int FetchWorkoutsJSON() {

        Log.v(TAG, "Fetching workouts");

        int ll_rows = 0;

        String url = workoutIP;

        DefaultHttpClient client = application.getHttpClient();
        client.setParams(getMyParams());

        HttpGet httpRequest = new HttpGet(url);

        try {

            setDefaultGetHeaders(httpRequest);

            Log.v(TAG, "Fetching runs - requesting");
            HttpResponse response = client.execute(httpRequest);
            Log.v(TAG, "Fetching runs - responce received");

            HttpEntity entity = response.getEntity();

            StatusLine statusLine = response.getStatusLine();

            Log.v(TAG, String.format("Fetching stores - status [%s]", statusLine));

            if (statusLine.getStatusCode() >= 300) {
//                Toast.makeText(activity,R.string.server_error,Toast.LENGTH_LONG).show();
                Log.e(TAG,statusLine.getStatusCode()+" - "+statusLine.getReasonPhrase());
            }

            String resultString = null;

            if (entity != null) {
                InputStream instream = entity.getContent();
                Header contentEncoding = response.getFirstHeader("Content-Encoding");
                if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                    instream = new GZIPInputStream(instream);
                }
                resultString = Utils.convertStreamToString(instream);

                instream.close();
            }

            Log.v(TAG, String.format("Deserialising [%s]", resultString));

            Gson gson = new Gson();
            List<Running> StoreList = (List<Running>) gson.fromJson(resultString,
                    new TypeToken<List<Running>>() {
                    }.getType());

//            dbHelper.deleteAllStores();

            Log.v(TAG, String.format("Fetching parts - ready to insert [%d] parts", StoreList.size()));

//            for (int i = 0; i < StoreList.size(); i++) {
//                dbHelper.addSite(StoreList.get(i));
//                ll_rows++;
//            }

        } catch (Exception e) {
            Log.e(TAG, "Exception fetching stores", e);
            return 0;
        }

        Log.v(TAG, String.format("Fetching stores - done"));

        return ll_rows;

    }



    private void setDefaultGetHeaders(HttpGet httpRequest) throws UnsupportedEncodingException {
        httpRequest.setHeader("Accept", "application/json");
        httpRequest.setHeader("Content-type", "application/json");
    }

    private void setDefaultPostHeaders(HttpPost httpPost) throws UnsupportedEncodingException {
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
    }


    private HttpParams getMyParams() {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 5 * 60 * 1000);
        return httpParams;
    }

    public ExtApplication getApplication() {
        return application;
    }


    private static JSONObject getJsonObjectFromMap(Map params) throws JSONException {

        //all the passed parameters from the post request
        //iterator used to loop through all the parameters
        //passed in the post request
        Iterator iter = params.entrySet().iterator();

        //Stores JSON
        JSONObject holder = new JSONObject();

        //using the earlier example your first entry would get email
        //and the inner while would get the value which would be 'foo@bar.com'
        //{ fan: { email : 'foo@bar.com' } }

        //While there is another entry
        while (iter.hasNext())
        {
            //gets an entry in the params
            Map.Entry pairs = (Map.Entry)iter.next();

            //creates a key for Map
            String key = (String)pairs.getKey();

            //Create a new map
            Map m = (Map)pairs.getValue();

            //object for storing Json
            JSONObject data = new JSONObject();

            //gets the value
            Iterator iter2 = m.entrySet().iterator();
            while (iter2.hasNext())
            {
                Map.Entry pairs2 = (Map.Entry)iter2.next();
                data.put((String)pairs2.getKey(), (String)pairs2.getValue());
            }

            //puts email and 'foo@bar.com'  together in map
            holder.put(key, data);
        }
        return holder;
    }


}

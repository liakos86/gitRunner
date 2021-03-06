
package com.example.gpsCheck;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;
import com.example.gpsCheck.dbObjects.User;
import com.example.gpsCheck.model.ContentDescriptor;
import com.example.gpsCheck.model.Database;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.gpsCheck.dbObjects.Running;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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

//todo sendme
public class SyncHelper {

    private static final String TAG = Thread.currentThread().getStackTrace()[2].getClassName()
            .substring(0, 23);
    private ExtApplication application;
    Database dbHelper;
    String workoutPath, authUrl, apiKey, runnerPath;
    DefaultHttpClient client;
    private SharedPreferences app_preferences;

    private Activity activity;

    public SyncHelper(Activity activity) {

        this.activity = activity;
        workoutPath = activity.getResources().getString(R.string.challenge_path);
        authUrl = activity.getResources().getString(R.string.auth_url);
        apiKey = activity.getResources().getString(R.string.apiKey);
        runnerPath = activity.getResources().getString(R.string.runner_path);
        application = (ExtApplication) activity.getApplicationContext();
        app_preferences = PreferenceManager.getDefaultSharedPreferences(application);
        dbHelper = new Database(application);
        client = application.getHttpClient();

    }

    //fixme commit
    public int getMongoUser(String email, String username, String password) {

        Log.v(TAG, "Fetching user");

        int returnCode = -2;//server error default ?
        Uri uri=null;

        if (email==null&&username==null&&password==null) {//use share prefs mongoId
//             url = runnerCollection + app_preferences.getString("mongoId", "") + "?apiKey=" + apiKey;
            uri = new Uri.Builder()
            .scheme("https")
            .authority(authUrl)
            .path(runnerPath + app_preferences.getString("mongoId", ""))
            .appendQueryParameter("apiKey", apiKey)
                    .build();



        }else if (username==null&&email!=null&&password!=null){//try get existing user by email

             uri = new Uri.Builder()
                    .scheme("https")
                    .authority(authUrl)
                    .path(runnerPath)
                    .appendQueryParameter("q", "{ 'email':'"+email+"' , 'password' :'"+password+"' }")
                    .appendQueryParameter("apiKey", apiKey)
                     .appendQueryParameter("fo", "true")
                    .build();


//             url =  runnerCollection+ "?q= { 'email':'"+email+"' , 'password' :'"+password+"' }&apiKey=" +apiKey;
        }else if (username!=null&&email!=null&&password!=null) {//insert new user
            uri = new Uri.Builder()
                    .scheme("https")
                    .authority(authUrl)
                    .path(runnerPath)
                    .appendQueryParameter("apiKey", apiKey)
                    .build();


        }


            DefaultHttpClient client = application.getHttpClient();
            client.setParams(getMyParams());



        try {

            HttpResponse response;

            if (username!=null&&email!=null&&password!=null) {
                JSONObject runner = new JSONObject();
                runner.put("username", username);
                runner.put("password", password);
                runner.put("email", email);
                StringEntity se = new StringEntity( runner.toString());

                HttpPost httpPost = new HttpPost(uri.toString());
                setDefaultPostHeaders(httpPost);
                httpPost.setEntity(se);
                Log.v(TAG, "Fetching runs - requesting");
                response = client.execute(httpPost);

            }else {
                HttpGet httpRequest = new HttpGet(uri.toString());
                setDefaultGetHeaders(httpRequest);
                Log.v(TAG, "Fetching runs - requesting");
                response = client.execute(httpRequest);
            }


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
            User user2 = (User) gson.fromJson(resultString,
                    new TypeToken<User>() {
                    }.getType());

//            dbHelper.deleteAllStores();

            if (email!=null&&user2==null){
                returnCode = -1; //no user found
            }else if (email!=null&&user2!=null){

               SharedPreferences.Editor editor = app_preferences.edit();
               editor.putString("mongoId", user2.get_id().get$oid());
                editor.putString("username", user2.getUsername());
                editor.putInt("totalScore", user2.getTotalScore());
               editor.putInt("totalChallenges", user2.getTotalChallenges());
                editor.putLong("totalTime", user2.getTotalTime());
                editor.putFloat("totalDistance", user2.getTotalDistance());
                editor.putInt("wonChallenges", user2.getwonChallenges());
                editor.putString("friends", user2.getFriends());
                editor.putString("friendRequests", user2.getFriendRequests());
                editor.commit();
               returnCode = 2;
            }else if (email==null&&user2!=null){
                SharedPreferences.Editor editor = app_preferences.edit();
                editor.putInt("totalScore", user2.getTotalScore());
                editor.putString("username", user2.getUsername());
                editor.putInt("totalChallenges", user2.getTotalChallenges());
                editor.putLong("totalTime", user2.getTotalTime());
                editor.putFloat("totalDistance", user2.getTotalDistance());
                editor.putInt("wonChallenges", user2.getwonChallenges());
                editor.putString("friends", user2.getFriends());
                editor.putString("friendRequests", user2.getFriendRequests());
                editor.commit();



                returnCode = 0;
            }

            Log.v(TAG, String.format("Fetching parts - ready to insert 1 user", 1));

        } catch (Exception e) {
            Log.e(TAG, "Exception fetching user", e);
            return -2;
        }

        Log.v(TAG, String.format("Fetching stores - done"));



        return returnCode;

    }

    public List<Running> getMongoChallenges() {

        Log.v(TAG, "Fetching challenges for me");

        List<Running>challenges = new ArrayList<Running>();

        List<Running>challengesExtClosed = new ArrayList<Running>();

        List<Running>challengesDBClosed = new ArrayList<Running>();


        String query = "  " +
                "{   $or: [";

        query += "{ 'user_name': '" + app_preferences.getString("username","") + "'},";

        query += "{ 'opponent_name': '" + app_preferences.getString("username","") + "'}";

        query += "]  } ";
//todo add status query
//        query += ", 'status': '0'     }   ";

          Uri  uri = new Uri.Builder()
                    .scheme("https")
                    .authority(authUrl)
                    .path(workoutPath)
                    .appendQueryParameter("q", query)
                    .appendQueryParameter("apiKey", apiKey)
                    .build();


        DefaultHttpClient client = application.getHttpClient();
        client.setParams(getMyParams());



        try {

            HttpResponse response;


                HttpGet httpRequest = new HttpGet(uri.toString());
                setDefaultGetHeaders(httpRequest);
                Log.v(TAG, "Fetching challenges - requesting");
                response = client.execute(httpRequest);



            Log.v(TAG, "Fetching challenges - responce received");

            HttpEntity entity = response.getEntity();

            StatusLine statusLine = response.getStatusLine();

            Log.v(TAG, String.format("Fetching challenges - status [%s]", statusLine));

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
            challenges = (List<Running>) gson.fromJson(resultString,
                    new TypeToken<List<Running>>() {
                    }.getType());

            dbHelper.deleteAllOpenChallenges();



//            Log.v(TAG, String.format("Fetching parts - ready to insert [%d] parts", StoreList.size()));
            int size = challenges.size();
            for (int i = 0; i < size; i++) {


                //if closed challenge and user has responded to it (not created!)
                if ( challenges.get(i).getStatus()==1 ){
                    challengesExtClosed.add(challenges.get(i));
                    challenges.remove(i);
                    --i;
                    --size;
                }else if ( challenges.get(i).getStatus()==0)  {
                    challenges.get(i).setType(1);
                    challenges.get(i).setRunning_id(-1);
                    dbHelper.addRunning(challenges.get(i));
                }
//                ll_rows++;
            }


            challengesDBClosed = dbHelper.fetchClosedRunsFromDb();
            int sizeClosed = challengesDBClosed.size();

            size = challengesExtClosed.size();
            for (int j=0; j<size; j++){

                boolean exists=false;

                for (int k=0; k<sizeClosed; k++){

                    if (challengesExtClosed.get(j).get_id().get$oid().equals(challengesDBClosed.get(k).getMongoId())){
                        exists=true;
                        continue;
                    }

                }

                if (!exists) {

                    challengesExtClosed.get(j).setMongoId(challengesExtClosed.get(j).get_id().get$oid());
                    challengesExtClosed.get(j).setRunning_id(-1);
                    challengesExtClosed.get(j).setType(1);
                    challengesDBClosed.add(challengesExtClosed.get(j));
                    dbHelper.addRunning(challengesExtClosed.get(j));
                }



            }



            for (Running run:challengesDBClosed){
                challenges.add(run);
                if (run.getDeleted()!=1 && run.getWinner()!=null && (run.getUser_name().equals(app_preferences.getString("username","")))){
                    deleteChallenge(run.getMongoId());
                    dbHelper.setDeletedFlag(run.getMongoId());
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception fetching challenges", e);
            return challenges;
        }

        Log.v(TAG, String.format("Fetching challenges - done"));



        return challenges;

    }

    public void createMongoChallenge(String opponentName, long totalTime, float totalDistance, String latLonList, String description) {

        Log.v(TAG, "inserting challenge");

        Uri uri = new Uri.Builder()
                .scheme("https")
                .authority(authUrl)
                .path(workoutPath)
                .appendQueryParameter("apiKey", apiKey)
                .build();

//        HashMap  params = new HashMap();
        DefaultHttpClient client = application.getHttpClient();
        client.setParams(getMyParams());
        HttpPost httpPost = new HttpPost(uri.toString());


        try {

            Date now = new Date();
            JSONObject workout = new JSONObject();
            workout.put("user_name", app_preferences.getString("username",""));
            workout.put("distance", totalDistance);
            workout.put("time", totalTime);
            workout.put("date", now.toString());
            workout.put("opponent_name", opponentName);
            workout.put("latLonList", latLonList);
            workout.put("description", description);



            StringEntity se = new StringEntity( workout.toString());
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




                instream.close();
            }


            updateTimeAndDistance(app_preferences.getString("username",""));

        } catch (Exception e) {
            Log.e(TAG, "Exception creating challenge", e);
        }

        Log.v(TAG, String.format("Creating challenge - done"));


    }

    public void updateTimeAndDistance(String username){


        Log.v(TAG, "Saving time and dist");

        String query = "{'username': '"+username+"'}";


        Uri uri = new Uri.Builder()
                .scheme("https")
                .authority(authUrl)
                .path(runnerPath)
                .appendQueryParameter("q", query)
                .appendQueryParameter("apiKey", apiKey)
                .build();


        DefaultHttpClient client = application.getHttpClient();
        client.setParams(getMyParams());


        try {
            JSONObject obj = new JSONObject();


            obj.put("totalDistance" , app_preferences.getFloat("totalDistance",0));

            obj.put("totalTime" , app_preferences.getLong("totalTime", 0));


            JSONObject lastObj = new JSONObject();
            lastObj.put("$set", obj);

            StringEntity se = new StringEntity(lastObj.toString());



            HttpPut httpRequest = new HttpPut(uri.toString());

            httpRequest.setEntity(se);


            setDefaultPutHeaders(httpRequest);

            Log.v(TAG, "setting user stats - requesting");
            HttpResponse response = client.execute(httpRequest);
            Log.v(TAG, "setting finished - responce received");

            HttpEntity entity = response.getEntity();

            StatusLine statusLine = response.getStatusLine();

            Log.v(TAG, String.format("Setting time & dist - status [%s]", statusLine));

            if (statusLine.getStatusCode() >= 300) {
//                Toast.makeText(activity,R.string.server_error,Toast.LENGTH_LONG).show();
                Log.e(TAG, statusLine.getStatusCode() + " - " + statusLine.getReasonPhrase());
                return;
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



//            dbHelper.deleteAllStores();

            Log.v(TAG, String.format("Uploaded user time and dist"));

        } catch (Exception e) {
            Log.e(TAG, "Exception uploading user stats", e);
        }
    }

    public List<User> getLeaderBoardOrFriend(String friends, int type){

        List<User> users = new ArrayList<User>();

        if (type==1)
            Log.v(TAG, "Fetching user "+friends);
        else
            Log.v(TAG, "Fetching leaderboard");

            String[] friendsArray;
            String query;

            if (friends == null || friends.equals("")) {
                return users;
            } else {

                if (type==0) {

                    friendsArray = friends.split(" ");
                    int length = friendsArray.length;
                    query = "{ $or: [";
                    for (int i = 0; i < length - 1; i++) {
                        query += "{ 'username': '" + friendsArray[i] + "'},";
                    }
                    query += "{ 'username': '" + friendsArray[length - 1] + "'}";
                    query += "] }";
                }else {

                    query = "{ 'username' : '"+friends+"'}";

                }

            }

//        https://api.mongolab.com/api/1/databases/auction/collections/runner?
//        q={ $or: [ { "email": "liak@liak.gr" }, { "email": "liak2@liak.gr" } ] }
//        &apiKey=fft3Q2J8bB2l-meOoBHZK_z3E_b5cuBz


            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .authority(authUrl)
                    .path(runnerPath)
                    .appendQueryParameter("q", query)
                    .appendQueryParameter("s", "{'totalScore': -1}")
                    .appendQueryParameter("apiKey", apiKey)
                    .build();


            DefaultHttpClient client = application.getHttpClient();
            client.setParams(getMyParams());


            try {

                HttpGet httpRequest = new HttpGet(uri.toString());


                setDefaultGetHeaders(httpRequest);

                Log.v(TAG, "Fetching runs - requesting");
                HttpResponse response = client.execute(httpRequest);
                Log.v(TAG, "Fetching runs - responce received");

                HttpEntity entity = response.getEntity();

                StatusLine statusLine = response.getStatusLine();

                Log.v(TAG, String.format("Fetching stores - status [%s]", statusLine));

                if (statusLine.getStatusCode() >= 300) {
//                Toast.makeText(activity,R.string.server_error,Toast.LENGTH_LONG).show();
                    Log.e(TAG, statusLine.getStatusCode() + " - " + statusLine.getReasonPhrase());
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


                users = (List<User>) gson.fromJson(resultString,
                        new TypeToken<List<User>>() {
                        }.getType());


                //add a new friend request to the other user
                if (type==1&&users.size()==1){

                    getMongoUserByUsernameForFriend(users.get(0).getUsername(), 1);

                }else if (type==0){

                    dbHelper.deleteLeaderboard();
                    if (users.size()>0){
                        for (User u:users) {
                            u.setUser_id(-1l);
                            dbHelper.addLeader(u);
                        }
                    }

                }

//            dbHelper.deleteAllStores();

                Log.v(TAG, String.format("Fetching parts - retrieved [%d] users", users.size()));

//            for (int i = 0; i < StoreList.size(); i++) {
//                dbHelper.addSite(StoreList.get(i));
//                ll_rows++;
//            }

            } catch (Exception e) {
                Log.e(TAG, "Exception fetching leaderboard or friend", e);
                return users;
            }

            Log.v(TAG, String.format("Fetching leader or friend - done"));


            return users;




    }

    public User getMongoUserByUsernameForFriend(String username, int type) {// 1 send request, 0 accept, 2 reject, else just get user

        Log.v(TAG, "Fetching user");

        String myUsername = app_preferences.getString("username", "");

        User user = null;

        Uri uri=null;

            uri = new Uri.Builder()
                    .scheme("https")
                    .authority(authUrl)
                    .path(runnerPath)
                    .appendQueryParameter("q", "{ 'username':'"+username+"' }")
                    .appendQueryParameter("fo", "true")
                    .appendQueryParameter("apiKey", apiKey)
                    .build();





        DefaultHttpClient client = application.getHttpClient();
        client.setParams(getMyParams());



        try {

            HttpResponse response;


                HttpGet httpRequest = new HttpGet(uri.toString());
                setDefaultGetHeaders(httpRequest);
                Log.v(TAG, "Fetching user - requesting");
                response = client.execute(httpRequest);



            Log.v(TAG, "Fetching user - responce received");

            HttpEntity entity = response.getEntity();

            StatusLine statusLine = response.getStatusLine();

            Log.v(TAG, String.format("Fetching user - status [%s]", statusLine));

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
             user = (User) gson.fromJson(resultString,
                    new TypeToken<User>() {
                    }.getType());

            // refresh other users requests
            if (type==1) {
                uploadNewFriendOrRequest(user.getFriendRequests() + " " + myUsername, user.getUsername(), type);
                SharedPreferences.Editor editor = app_preferences.edit();
                editor.putString("sentRequests",  app_preferences.getString("sentRequests","")+user.getUsername());
                editor.commit();
                //refresh other users friends
            }else if (type==0|| type==2) {

                //add friend to both users list


                if ((type==0)&&(!user.getFriends().contains(myUsername) && !app_preferences.getString("friends", "").contains(user.getUsername()))){
                    uploadNewFriendOrRequest(user.getFriends() + " " + myUsername, user.getUsername(), type);
                    uploadNewFriendOrRequest(app_preferences.getString("friends", "") + " " + user.getUsername(), myUsername, type);
                }

                //remove his name
                String  newFriendRequests = app_preferences.getString("friendRequests","").replace(" " + user.getUsername() + " ", " ");
                newFriendRequests = newFriendRequests.replace(user.getUsername() + " ", " ");
                newFriendRequests = newFriendRequests.replace(" "+user.getUsername(), " ");
                newFriendRequests = newFriendRequests.replace(user.getUsername(), "");
                uploadNewFriendOrRequest(newFriendRequests, myUsername, 1);

                SharedPreferences.Editor editor = app_preferences.edit();
                editor.putString("friendRequests", newFriendRequests);
                if (type==0)
                editor.putString("friends", app_preferences.getString("friends","") + " " + user.getUsername());
                editor.commit();

//                Toast.makeText(getApplication(), "Friend added!", Toast.LENGTH_LONG).show();



            }

//            Log.v(TAG, String.format("Fetching parts - ready to insert 1 user", 1));

        } catch (Exception e) {
            Log.e(TAG, "Exception fetching user", e);
            return user;
        }

        Log.v(TAG, String.format("Fetching user - done"));



        return user;

    }


    public boolean uploadNewFriendOrRequest(String friends,String username, int type){


        Log.v(TAG, "Uploading new friend");

        String query = "{'username': '"+username+"'}";







//        https://api.mongolab.com/api/1/databases/auction/collections/runner?
//        q={ $or: [ { "email": "liak@liak.gr" }, { "email": "liak2@liak.gr" } ] }
//        &apiKey=fft3Q2J8bB2l-meOoBHZK_z3E_b5cuBz


        Uri uri = new Uri.Builder()
                .scheme("https")
                .authority(authUrl)
                .path(runnerPath)
                .appendQueryParameter("q", query)
                .appendQueryParameter("apiKey", apiKey)
                .build();


        DefaultHttpClient client = application.getHttpClient();
        client.setParams(getMyParams());


        try {
            JSONObject obj = new JSONObject();

            if (type==0)
                obj.put("friends" , friends);
            else if (type==1)
                obj.put("friendRequests" , friends);

            JSONObject lastObj = new JSONObject();
            lastObj.put("$set", obj);

            StringEntity se = new StringEntity(lastObj.toString());



            HttpPut httpRequest = new HttpPut(uri.toString());

            httpRequest.setEntity(se);


            setDefaultPutHeaders(httpRequest);

            Log.v(TAG, "new friend - requesting");
            HttpResponse response = client.execute(httpRequest);
            Log.v(TAG, "new friend - responce received");

            HttpEntity entity = response.getEntity();

            StatusLine statusLine = response.getStatusLine();

            Log.v(TAG, String.format("Fetching stores - status [%s]", statusLine));

            if (statusLine.getStatusCode() >= 300) {
//                Toast.makeText(activity,R.string.server_error,Toast.LENGTH_LONG).show();
                Log.e(TAG, statusLine.getStatusCode() + " - " + statusLine.getReasonPhrase());
                return false;
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



//            dbHelper.deleteAllStores();

            Log.v(TAG, String.format("Fetching parts - new friend added"));

//            for (int i = 0; i < StoreList.size(); i++) {
//                dbHelper.addSite(StoreList.get(i));
//                ll_rows++;
//            }

        } catch (Exception e) {
            Log.e(TAG, "Exception inserting friend", e);
            return false;
        }

        Log.v(TAG, String.format("uploaded friend - done"));
        return true;

    }


    public void replyToChallenge(String opponentName, boolean won) {

        updateTimeAndDistance(app_preferences.getString("username",""));

        Log.v(TAG, "replying to challenge");

//        Toast.makeText(getApplication(), opponentName+"  "+won, Toast.LENGTH_LONG).show();


        String query = "{ $and: [";
        query = "{ 'user_name': '" + opponentName + "' ,";
        query += " 'opponent_name': '" + app_preferences.getString("username","") + "'}";
//        query += "] }";


        Uri uri = new Uri.Builder()
                .scheme("https")
                .authority(authUrl)
                .path(workoutPath)
                .appendQueryParameter("q", query)
                .appendQueryParameter("apiKey", apiKey)
                .build();

//        HashMap  params = new HashMap();
        DefaultHttpClient client = application.getHttpClient();
        client.setParams(getMyParams());
        HttpPut httpPut = new HttpPut(uri.toString());


        try {

            JSONObject obj = new JSONObject();
            obj.put("winner" , won?app_preferences.getString("username",""):opponentName);
            obj.put("status" , 1);
            JSONObject lastObj = new JSONObject();
            lastObj.put("$set", obj);

            StringEntity se = new StringEntity(lastObj.toString());


            setDefaultPutHeaders(httpPut);
            httpPut.setEntity(se);

            Log.v(TAG, "Fetching runs - requesting");
            HttpResponse response = client.execute(httpPut);
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


                fixScoreAndChallenges(app_preferences.getString("username",""), won);
                fixScoreAndChallenges(opponentName, won);





                instream.close();
            }

//            Toast.makeText(getApplication(), "Challenged updated", Toast.LENGTH_LONG).show();


        } catch (Exception e) {
            Log.e(TAG, "Exception replying to challenge", e);
        }

        Log.v(TAG, String.format("Fetching stores - done"));


    }

    private void fixScoreAndChallenges(String username, boolean won){

        User user = getMongoUserByUsernameForFriend(username, -1);


        if (user!=null) {

            user.setTotalChallenges(user.getTotalChallenges() + 1);
            if (won) {
                user.setWonChallenges(user.getwonChallenges() + 1);
                user.setTotalScore(user.getTotalScore() + 1000);
            } else {
                if (user.getTotalScore() - 500 > 0)
                    user.setTotalScore(user.getTotalScore() - 500);
                else
                    user.setTotalScore(0);

            }

            setScoreAndChallenges(user);

        }

    }

    private void setScoreAndChallenges(User user){

        Log.v(TAG, "Saving new score");

        String query = "{'username': '"+user.getUsername()+"'}";


        Uri uri = new Uri.Builder()
                .scheme("https")
                .authority(authUrl)
                .path(runnerPath)
                .appendQueryParameter("q", query)
                .appendQueryParameter("apiKey", apiKey)
                .build();


        DefaultHttpClient client = application.getHttpClient();
        client.setParams(getMyParams());


        try {
            JSONObject obj = new JSONObject();


            obj.put("totalScore" , user.getTotalScore());

            obj.put("totalChallenges" , user.getTotalChallenges());

            obj.put("wonChallenges" , user.getWonChallenges());

            JSONObject lastObj = new JSONObject();
            lastObj.put("$set", obj);

            StringEntity se = new StringEntity(lastObj.toString());



            HttpPut httpRequest = new HttpPut(uri.toString());

            httpRequest.setEntity(se);


            setDefaultPutHeaders(httpRequest);

            Log.v(TAG, "setting user stats - requesting");
            HttpResponse response = client.execute(httpRequest);
            Log.v(TAG, "setting finished - responce received");

            HttpEntity entity = response.getEntity();

            StatusLine statusLine = response.getStatusLine();

            Log.v(TAG, String.format("Fetching stores - status [%s]", statusLine));

            if (statusLine.getStatusCode() >= 300) {
//                Toast.makeText(activity,R.string.server_error,Toast.LENGTH_LONG).show();
                Log.e(TAG, statusLine.getStatusCode() + " - " + statusLine.getReasonPhrase());
                return;
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



//            dbHelper.deleteAllStores();

            Log.v(TAG, String.format("Uploaded user score and challenges"));

        } catch (Exception e) {
            Log.e(TAG, "Exception uploading user stats", e);
        }

    }

    public void deleteChallenge(String id){


//
//       String query =  "{_'id': '{ '$oid': '"+id+"'}";
//         query =  "{'description' : 'xaxa'}";

        Uri uri = new Uri.Builder()
                .scheme("https")
                .authority(authUrl)
                .path(workoutPath+"/"+id)
                .appendQueryParameter("apiKey", apiKey)
                .build();


        DefaultHttpClient client = application.getHttpClient();
        client.setParams(getMyParams());

        try{
            HttpDelete httpDelete = new HttpDelete(uri.toString());
            HttpResponse response;


            setDefaultDeleteHeaders(httpDelete);
            Log.v(TAG, "deleting run - requesting");
            response = client.execute(httpDelete);



            Log.v(TAG, "deleting run - responce received");

            HttpEntity entity = response.getEntity();

            StatusLine statusLine = response.getStatusLine();

            Log.v(TAG, String.format("deleting run [%s]", statusLine));

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
        }catch (Exception e) {
            Log.e(TAG, "Exception deleting run", e);

        }


    }

    private void setDefaultDeleteHeaders(HttpDelete httpRequest) throws UnsupportedEncodingException {
        httpRequest.setHeader("Accept", "application/json");
        httpRequest.setHeader("Content-type", "application/json");
    }

    private void setDefaultGetHeaders(HttpGet httpRequest) throws UnsupportedEncodingException {
        httpRequest.setHeader("Accept", "application/json");
        httpRequest.setHeader("Content-type", "application/json");
    }

    private void setDefaultPostHeaders(HttpPost httpPost) throws UnsupportedEncodingException {
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
    }

    private void setDefaultPutHeaders(HttpPut httpPut) throws UnsupportedEncodingException {
        httpPut.setHeader("Accept", "application/json");
        httpPut.setHeader("Content-type", "application/json");
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

}

package com.example.gpsCheck;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.example.gpsCheck.dbObjects.User;
import java.util.ArrayList;
import java.util.List;

//fixme commit

public class FrgShowChallenge extends BaseFragment {

    LeaderArrayAdapterItem adapter;
    RequestsArrayAdapterItem adapterForReq;
    List<User>leaders;
    List<String>friendRequests;
    EditText friendName;
    Button addFriend;

    SyncHelper sh;



    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.showchallenge_frg, container, false);
        sh = new SyncHelper(getActivity());




        setList(v);

        getLeaderBoard();



//        setListeners(v);


//        selectProvider();



//        if (provider==null){
//
//
//            return v;
//        }
//
//        lastLocation = getLastLocation();// locationManager.getLastKnownLocation(provider);

//        providerField.setText(provider);

        // Initialize the location fields
//        if (lastLocation != null) {
//
//            initializeMap();
//
//
//        }
//        else {
//            locationManager.requestLocationUpdates(provider, 500, 0, this);
////            latituteField.setText("Location not available");
////            longitudeField.setText("Location not available");
//        }






        return  v;
    }

    public void getLeaderBoard(){

        ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);

        String friends = app_preferences.getString("friends",null);
        if (friends!=null && !friends.equals("")){
            new getLeaderBoardOrFriend(getActivity(),friends, 0).execute();
        }

    }

    private void setList(View v){

        ListView runningListView = (ListView) v.findViewById(R.id.listLeaders);
        addFriend = (Button) v.findViewById(R.id.buttonAddFriend);
        friendName = (EditText) v.findViewById(R.id.editNewFriend);
        runningListView.setDivider(null);
        leaders = new ArrayList<User>();
        friendRequests = new ArrayList<String>();

        String[] friendList = user.getFriendRequests().split(" ");
        for (String fr:friendList){
            if (!fr.equals("null")&& !fr.equals("") && !fr.equals(" ")) friendRequests.add(fr);
        }

        final ViewSwitcher vs = (ViewSwitcher) v.findViewById(R.id.chalSwitcher);

        Button leaderBoard = (Button) v.findViewById(R.id.buttonLeaders);
        Button friendReq = (Button) v.findViewById(R.id.buttonRequests);

        ListView friendsListView = (ListView) v.findViewById(R.id.listFriendRequests);
        friendsListView.setDivider(null);

        adapter = new LeaderArrayAdapterItem(getActivity().getApplicationContext(),
                R.layout.list_running_row, leaders);
        runningListView.setAdapter(adapter);


        adapterForReq = new RequestsArrayAdapterItem(getActivity().getApplicationContext(),
                R.layout.list_request_row, friendRequests);
        friendsListView.setAdapter(adapterForReq);


        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              fetchFriend();
            }
        });

        leaderBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vs.setDisplayedChild(0);
            }
        });
        friendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vs.setDisplayedChild(1);
            }
        });




    }

    public void refreshRequests(){

        friendRequests.clear();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());



        String[] friendList = app_preferences.getString("friendRequests","").split(" ");
        for (String fr:friendList){
            if (!fr.equals("null")&& !fr.equals("") && !fr.equals(" ")) friendRequests.add(fr);
        }
        adapterForReq.notifyDataSetChanged();
    }

    private void fetchFriend() {

        if (friendName.getText().length() > 3) {
            if (!alreadyFriend(friendName.getText().toString())) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(addFriend.getWindowToken(), 0);
                new getLeaderBoardOrFriend(getActivity(), friendName.getText().toString(), 1).execute();
            }
        } else {
            Toast.makeText(getActivity(), "Insert valid name", Toast.LENGTH_LONG).show();
        }
    }

    private boolean alreadyFriend(String friendName){

        ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);
        String friends = app_preferences.getString("friends", null);
        if (friends != null && !friends.equals("")) {
            String[] friendsList = friends.split(" ");
            for (String friendEmail : friendsList) {
                if (friendEmail.equals(friendName)) {
                    Toast.makeText(getActivity(), "Already a friend", Toast.LENGTH_LONG).show();
                    return true;
                }
            }

        }
        return false;



    }

//    public void selectProvider(){
//
//
//        // Get the location manager
//        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//        // Define the criteria how to select the locatioin provider -> use
//        // default
////        Criteria criteria = new Criteria();
////        provider = locationManager.getBestProvider(criteria, false);
////        WifiManager wifi = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
////        if ((locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))&&(wifi.isWifiEnabled())){
////            provider = "network";
////        }else
//
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//            provider = "gps";
//        }else{
//            provider=null;
//        }
//
//        if (provider==null){
//            Toast.makeText(getActivity(), "Please enable location services",
//                    Toast.LENGTH_SHORT).show();
//        }else {
//
//            lastLocation = getLastLocation();// locationManager.getLastKnownLocation(provider);
//            if (lastLocation!= null) {
//
//                initializeMap();
//
//            }
////            else{
////                locationManager.requestLocationUpdates(provider, 500, 0, this);
////            }
//        }
//
//
//    }

//    public void getUpdates(boolean shouldUpdate){
//        if (shouldUpdate) {
//
//            firstChange=false;
//
//            if (googleMap!=null){
//
//                for (Polyline line:mapLines){
//                    line.remove();
//                }
//
//            }
////            googleMap.clear();
//            running=true;
//            locationManager.requestLocationUpdates(provider, 2000, 3, this);
//            save.setText("Stop");
//
//
//            startMarker=null;
//            runnerMarker=null;
//            totalDistance =0;
//
//
//
//
//            totalTime = 0L;
//
//            if(mStartTime == 0L){
//                mStartTime = SystemClock.uptimeMillis();
//                mHandler.removeCallbacks(mUpdateTimeTask);
//                mHandler.postDelayed(mUpdateTimeTask, 100);
//
//            }
//
//
//
//        }else{
//            locationManager.removeUpdates(this);
//
//            running=false;
//            save.setText("Start");
//
//            mHandler.removeCallbacks(mUpdateTimeTask);
//
//            textChalTimer.setText(timerStop1);
//            mStartTime = 0L;
//
//        }
//
//    }

//    public void setListeners(View v){
//
//
//
//        Button buttonTarget = (Button) v.findViewById(R.id.buttonTarget);
//        final LinearLayout ll = (LinearLayout) v.findViewById(R.id.targetWindow);
//        final EditText et = (EditText) v.findViewById(R.id.targetValue);
//
//        buttonTarget.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                targetDistance = Float.parseFloat(et.getText().toString());
//                ll.setVisibility(View.GONE);
//
//            }
//        });
//
//
//        save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if (!running) {
//
//
//                    if (goalReached){
//                        uploadChallenge();
//                        getUpdates(false);
//
//                    }else {
//
//                        if (provider != null)
//                            getUpdates(true);
//                        else
//                            selectProvider();
//                    }
//
//
//                }else{
//                    getUpdates(false);
//
//                }
//
//            }
//        });
//
//
//
//    }

//    private void getTargetDistance(){
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
//                getActivity());
//        alertDialogBuilder.setTitle("Store Info");
//        alertDialogBuilder
//                .setMessage(Html.fromHtml(
//                        "<b>Distance: </b>" + "      " + "<br>" ))
//                .setCancelable(false)
//                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//
//                        targetDistance = 5;
//                        start.setClickable(true);
//                        clear.setClickable(true);
//                        dialog.cancel();
//                    }
//                });
//        AlertDialog alertDialog = alertDialogBuilder.create();
//        alertDialog.show();
//    }

//    public void uploadChallenge(){
//
//
//
//        if (!running){
//
//            Toast.makeText(getActivity(),"Saving...",Toast.LENGTH_LONG).show();
//
//            Date now = new Date();
//            Running tr = new Running(-1, "I am running",
//                    totalTime,
//                    now.toString(),totalDistance, 1, "opponent_id", latLonList);
//
//
//
//            Database db = new Database(getActivity().getBaseContext());
//            db.addRunning(tr);
//
//            try {
//
//                JSONObject workout = new JSONObject();
//                workout.put("user_id", "hgfhgfghfhj");
//                workout.put("distance", String.valueOf(totalDistance));
//                workout.put("time", String.valueOf(totalTime));
//                workout.put("date", now.toString());
//                workout.put("type", "1");
//                workout.put("opponent_id", "asdjnjfej");
//                workout.put("latLonList", latLonList);
//
//                new uploadMongoChallenge(getActivity(), workout).execute();
//
//
//            }catch (JSONException e){
//                Toast.makeText(getActivity(),"Error uploading!", Toast.LENGTH_LONG).show();
//            }
//
//        }else{
//            Toast.makeText(getActivity(), "Still running!",
//                    Toast.LENGTH_SHORT).show();
//        }
//
//    }

//    public void initializeMap(){
//        SupportMapFragment fm = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapChalKostas);
//        googleMap = fm.getMap();
//
//        googleMap.setMyLocationEnabled(true);
//        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//
//
//        startMarker = googleMap.addMarker(new MarkerOptions()
////                        .infoWindowAnchor(0.48f, 4.16f)
//
//                        .position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
//                        .title("You are here")
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))
//
//        );
//
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15));
//        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
//
//        System.out.println("Provider " + provider + " has been selected.");
////        onLocationChanged(lastLocation);
//    }

    /* Request updates at startup */
//    @Override
//    public void onResume() {
//        super.onResume();
//        locationManager.requestLocationUpdates(provider, 2000, 0, this);
//    }
//
//    /* Remove the locationlistener updates when Activity is paused */
//    @Override
//    public void onPause() {
//        super.onPause();
//        locationManager.removeUpdates(this);
//    }

//    @Override
//    public void onLocationChanged(Location location) {
//
//
//
//        if (startMarker==null){
//            lastLocation=location;
//
//            latLonList="";
//            latLonList = String.valueOf(location.getLatitude())+","+ String.valueOf(location.getLongitude());
//
//
//            googleMap.clear();
//            startMarker = googleMap.addMarker(new MarkerOptions()
////                        .infoWindowAnchor(0.48f, 4.16f)
//
//                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
//                            .title("You are here")
//                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))
//            );
//            return;
//        }
//
//        if (runnerMarker!=null)
//            runnerMarker.remove();
//
//        runnerMarker = googleMap.addMarker(new MarkerOptions()
////                        .infoWindowAnchor(0.48f, 4.16f)
//
//                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
//                        .title("You are here")
//        );
//
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
//
//        //todo try zoomby to keep the users zoom every time
//        googleMap.animateCamera(CameraUpdateFactory.zoomBy(0), 2000, null);
//
//
//
//
//        if (firstChange) {
//
//            totalDistance += location.distanceTo(lastLocation);
//
//            latLonList +=","+ String.valueOf(location.getLatitude())+","+ String.valueOf(location.getLongitude());
//
//
//
//            PolylineOptions line =
//                    new PolylineOptions().add(new LatLng(location.getLatitude(),
//                                    location.getLongitude()),
//                            new LatLng(lastLocation.getLatitude(),
//                                    lastLocation.getLongitude())
//                    )
//                            .width(5).color(Color.RED);
//
//            lastLocation = location;
//
//            Polyline pline = googleMap.addPolyline(line);
//            mapLines.add(pline);
//
//            // getSpeed is in meters/second so km/hour is meters*1000  / seconds*3600
//            textChalSpeed.setText("Speed: " + String.format("%1$,.2f", ((location.getSpeed() * 3600) / 1000)));
//
//            textChalSpeedAvg.setText("Avg Speed: " + String.format("%1$,.2f", (double) (totalDistance) / (double) (totalTime / (3600))));
//
//            textChalDistance.setText("Distance: " + String.format("%1$,.2f", (double) (totalDistance / 1000)));
//
//            if (totalDistance>=targetDistance){
//
//                goalReached=true;
//                running=false;
////                start.performClick();
//                save.setText("Upload challenge");
//            }
//
//
//        }else{
//            firstChange = true;
//        }
//
//
//    }

//    public Location getLastLocation(){
//
//        Location bestLocation = null;
//
//        long minTime=Long.MAX_VALUE;  float bestAccuracy = Float.MAX_VALUE;
//        long bestTime = Long.MIN_VALUE;
//
//        List<String> matchingProviders = locationManager.getAllProviders();
//        for (String provider: matchingProviders) {
//            Location location = locationManager.getLastKnownLocation(provider);
//            if (location != null) {
//                float accuracy = location.getAccuracy();
//                long time = location.getTime();
//
//
//
//                if ((time > minTime && accuracy < bestAccuracy)) {
//                    bestLocation = location;
//                    bestAccuracy = accuracy;
//                    bestTime = time;
//                }
//                else if (time < minTime &&
//                        bestAccuracy == Float.MAX_VALUE && time > bestTime){
//                    bestLocation = location;
//                    bestTime = time;
//                }
//            }
//        }
//        return bestLocation;
//    }


    private void setAdapter(List<User>users) {
        //todo maybe empty and refill

        leaders.clear();

        for (User user: users) leaders.add(user);
        int num = users.size();
        int score = user.getTotalScore();
        for (int i=0; i<num; i++){
            if (score>leaders.get(i).getTotalScore()){
                leaders.add(i,user);
                break;
            }
            if (i==num-1)
            leaders.add(num,user);
        }


//        leaders = users;
        adapter.notifyDataSetChanged();
    }

    private void refreshAdapter(List<User>users) {
        //todo maybe empty and refill

//        if (users.size()>0) {
//
//            ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();
//            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);
//            SharedPreferences.Editor editor = app_preferences.edit();
//
//            String friends = app_preferences.getString("friends", null);
//
//                friends += " " + users.get(0).getUsername();
//                editor.putString("friends", friends);
//
//            editor.commit();
            Toast.makeText(getActivity(), "Friend request sent!", Toast.LENGTH_LONG).show();

//            int num = leaders.size();
//            int score = users.get(0).getTotalScore();
//            for (int i=0; i<num; i++){
//                if (score>leaders.get(i).getTotalScore()){
//                    leaders.add(i,users.get(0));
//                    break;
//                }
//                if (i==num-1)
//                leaders.add(num,users.get(0));
//
//            }
////        leaders = users;
//            adapter.notifyDataSetChanged();
//
//        }else{
//            Toast.makeText(getActivity(), "Username does not exist!", Toast.LENGTH_LONG).show();
//
//        }
    }


//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//        Toast.makeText(getActivity(), "Enabled provider " + provider,
//                Toast.LENGTH_SHORT).show();
//
//    }

//    @Override
//    public void onProviderDisabled(String provider) {
//        Toast.makeText(getActivity(), "Disabled provider " + provider,
//                Toast.LENGTH_SHORT).show();
//
//        selectProvider();
//    }



//    private Runnable mUpdateTimeTask = new Runnable(){
//
//        public void run() {

//            final long start = mStartTime;
//            totalTime = SystemClock.uptimeMillis()- start;
//
//            int seconds = (int) (totalTime / 1000);
//            int minutes = seconds / 60;
//            seconds = seconds % 60;
//
//            textChalTimer.setText("" + minutes + ":"
//                    + String.format("%02d", seconds));
//
//            timerStop1 = minutes + ":"
//                    + String.format("%02d", seconds);
//
//            mHandler.postDelayed(this, 200);
//
//        }
//    };
//

    static FrgShowChallenge init(int val) {
        FrgShowChallenge truitonList = new FrgShowChallenge();

        // Supply val input as an argument.
        Bundle args = new Bundle();
        args.putInt("val", val);
        truitonList.setArguments(args);

        return truitonList;
    }

//    private class uploadMongoChallenge extends AsyncTask<Void, Void, Integer> {
//        private Activity activity;
//        JSONObject workout;
//
//        public uploadMongoChallenge(Activity activity, JSONObject workout) {
//            this.activity = activity;
//            this.workout = workout;
//        }
//
//        protected void onPreExecute() {
//
//        }
//
//        @Override
//        protected Integer doInBackground(Void... unused) {
//            SyncHelper sh = new SyncHelper(activity);
//
//
//            int ll_rows = sh.createMongoChallenge(workout);
//
//            return 0;
//        }
//
//        @Override
//        protected void onPostExecute(Integer result) {
//
//        }
//
//
//    }



    private class getLeaderBoardOrFriend extends AsyncTask<Void, Void, List<User>> {
        private Activity activity;
        String friends;
        int type;

        public getLeaderBoardOrFriend(Activity activity, String friends, int type) {
            this.activity = activity;
            this.friends = friends;
            this.type = type;
        }

        protected void onPreExecute() {
            addFriend.setClickable(false);
        }

        @Override
        protected List<User> doInBackground(Void... unused) {



               return sh.getLeaderBoardOrFriend(friends, type);


        }

        @Override
        protected void onPostExecute(List<User> users) {

            addFriend.setClickable(true);

            if (type==0)
                setAdapter(users);
            else if (type==1)
                refreshAdapter(users);


        }


    }


    private class acceptRequest extends AsyncTask<Void, Void, User> {
        private Activity activity;
        String friend;


        public acceptRequest(Activity activity, String friend) {
            this.activity = activity;
            this.friend = friend;
        }

        protected void onPreExecute() {
            addFriend.setClickable(false);
        }

        @Override
        protected User doInBackground(Void... unused) {



                return      sh.getMongoUserByUsernameForFriend(friend, 0);



        }

        @Override
        protected void onPostExecute(User user) {

            addFriend.setClickable(true);
            refreshRequests();




        }


    }



    // here's our beautiful adapter
    public class LeaderArrayAdapterItem extends ArrayAdapter<User> {

        Context mContext;
        int layoutResourceId;
        List<User> data;

        public LeaderArrayAdapterItem(Context mContext, int layoutResourceId,
                                List<User> data) {

            super(mContext, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.mContext = mContext;
            this.data = data;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            leaderViewHolder holder =null;
            if (convertView == null || !(convertView.getTag() instanceof leaderViewHolder)) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_leaderboard_row, parent, false);

                holder = new leaderViewHolder();

                holder.username = (TextView) convertView
                        .findViewById(R.id.leader_username);
                holder.score =  (TextView) convertView
                        .findViewById(R.id.leader_points);



                convertView.setTag(holder);
            } else {
                holder = (leaderViewHolder) convertView.getTag();

            }

            // object item based on the position
             User user1 = data.get(position);


            if (user1.getUsername().length()>0)
                holder.username.setText(user1.getUsername());
            else holder.username.setText("-- No name --");

            if (user1.getUsername().equals(user.getUsername())){
                holder.username.setTextColor(getResources().getColor(R.color.runner_green));
            }else{
                holder.username.setTextColor(getResources().getColor(R.color.drawer_black));

            }



            holder.score.setText(String.valueOf(user1.getTotalScore()));



            return convertView;

        }

    }

    // here's our beautiful adapter
    public class RequestsArrayAdapterItem extends ArrayAdapter<String> {

        Context mContext;
        int layoutResourceId;
        List<String> data;

        public RequestsArrayAdapterItem(Context mContext, int layoutResourceId,
                                      List<String> data) {

            super(mContext, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.mContext = mContext;
            this.data = data;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            leaderViewHolder holder =null;
            if (convertView == null || !(convertView.getTag() instanceof leaderViewHolder)) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_request_row, parent, false);

                holder = new leaderViewHolder();

                holder.username = (TextView) convertView
                        .findViewById(R.id.friend_name);

                holder.add = (ImageView) convertView.findViewById(R.id.trImageAdd);


                convertView.setTag(holder);
            } else {
                holder = (leaderViewHolder) convertView.getTag();

            }

            // object item based on the position
          final  String friend= data.get(position);


          holder.username.setText(friend+" wants to add you as a friend!");
            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new acceptRequest(getActivity(),friend).execute();

                }
            });


            return convertView;

        }

    }

    private class leaderViewHolder{
        TextView username;
        TextView score;
        ImageView add;
    }


}



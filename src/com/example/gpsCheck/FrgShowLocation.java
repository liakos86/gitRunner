package com.example.gpsCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.example.gpsCheck.dbObjects.Running;
import com.example.gpsCheck.model.Database;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FrgShowLocation extends BaseFragment implements LocationListener {
    private long mStartTime = 0L, totalTime=0L;
    private Handler mHandler = new Handler();
    private LocationManager locationManager;
    private String provider;
    GoogleMap googleMap;
    Marker runnerMarker, startMarker;
    Location lastLocation;
    Button buttonStartStop, buttonTarget, clear, buttonResume;
    boolean running=false, firstChange=false, goalReached=false;
    String timerStop1;
    String latLonList="";
    LinearLayout ll, actionButtons;
    RelativeLayout rl;
    FrameLayout fl;
    EditText targetDist, description ;

    Running challenge;

    List<Polyline>mapLines;

    private TextView textChalSpeed, textChalSpeedAvg, textChalDistance, textChalTimer;
    float totalDistance=0, targetDistance, targetTime;


    String opponentUsername, descriptionForChallenge;
    Spinner selectUsernameSpinner;

    List<String>usernames;



    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);


        View v = inflater.inflate(R.layout.showlocation_frg, container, false);

        textChalSpeed = (TextView) v.findViewById(R.id.textChalSpeed);
        textChalSpeedAvg = (TextView) v.findViewById(R.id.textChalSpeedAvg);
        textChalDistance = (TextView) v.findViewById(R.id.textChalDistance);
        textChalTimer = (TextView) v.findViewById(R.id.textChalTimer);
        buttonStartStop = (Button) v.findViewById(R.id.buttonChalSave);
        clear = (Button) v.findViewById(R.id.buttonChalClear);
        buttonResume = (Button) v.findViewById(R.id.buttonResume);
//        buttonSaveRun = (Button) v.findViewById(R.id.buttonSaveRun);

        mapLines = new ArrayList<Polyline>();
        setListeners(v);

        selectProvider();

        if (provider==null){

            Toast.makeText(getActivity(),"Cannot get location provider", Toast.LENGTH_LONG).show();
            return v;
        }

        lastLocation = getLastLocation();// locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (lastLocation != null) {

            initializeMap();


        }

        setSpinner(v);

        //default is personal run
        setSaveListener();

        return  v;
    }

    private void setSpinner(View v) {
        selectUsernameSpinner = (Spinner) v.findViewById(R.id.friendsSpinner);
        String[]names = user.getFriends().split(" ");
        usernames=new ArrayList<String>();
        for (String name:names)if (name!=null && !name.equals("") && !name.equals("null"))  usernames.add(name);

        MyAdapter adapter = new MyAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, usernames);//FIXME Only for the first account
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);

        addListenerOnSpinnerItemSelection(v);
        selectUsernameSpinner.setAdapter(adapter);
    }


    public void selectProvider(){


        // Get the location manager
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
//        Criteria criteria = new Criteria();
//        provider = locationManager.getBestProvider(criteria, false);
//        WifiManager wifi = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
//        if ((locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))&&(wifi.isWifiEnabled())){
//            provider = "network";
//        }else

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            provider = "gps";
        }else{
            provider=null;
        }

        if (provider==null){
            Toast.makeText(getActivity(), "Please enable location services",
                    Toast.LENGTH_SHORT).show();
        }else {

            lastLocation = getLastLocation();// locationManager.getLastKnownLocation(provider);
            if (lastLocation!= null) {

                initializeMap();

            }
//            else{
//                locationManager.requestLocationUpdates(provider, 500, 0, this);
//            }
        }


    }

    public void getUpdates(boolean shouldUpdate){
        if (shouldUpdate) {

            ((ActMainTest)getActivity()).togglePagerClickable(false);

            selectUsernameSpinner.setClickable(false);

            firstChange=false;

            if (googleMap!=null){

                for (Polyline line:mapLines){
                    line.remove();
                }

            }
//            googleMap.clear();
            running=true;
            locationManager.requestLocationUpdates(provider, 2000, 3, this);
            buttonStartStop.setText("Stop");


            startMarker=null;
            runnerMarker=null;
            totalDistance =0;




            totalTime = 0L;

            if(mStartTime == 0L){
                mStartTime = SystemClock.uptimeMillis();
                mHandler.removeCallbacks(mUpdateTimeTask);
                mHandler.postDelayed(mUpdateTimeTask, 100);

            }



        }else{
            ((ActMainTest)getActivity()).togglePagerClickable(true);

            locationManager.removeUpdates(this);
            selectUsernameSpinner.setClickable(true);

            running=false;


            mHandler.removeCallbacks(mUpdateTimeTask);

            textChalTimer.setText(timerStop1);
            mStartTime = 0L;

        }

    }

    public void uploadChallenge(){



        if (!running){


            Toast.makeText(getActivity(),"Saving...",Toast.LENGTH_LONG).show();
            descriptionForChallenge = description.getText().toString().trim();

//            if (type==0) {
//                Date now = new Date();
//                Running tr = new Running(-1, "I am running alone", totalTime,
//                        now.toString(),totalDistance, 0, "",user.getUsername(), latLonList);
//                Database db = new Database(getActivity().getBaseContext());
//                db.addRunning(tr);
//            }else {


//                if (challenge==null) {// i am creating a chal now

                new uploadMongoChallenge(getActivity(), 1, true).execute();

//                }
        
//        else{
//
//                    Toast.makeText(getActivity(), "Successful challenge end!", Toast.LENGTH_LONG).show();
//                    try {
//
//
//                        new uploadMongoChallenge(getActivity(), 2).execute();
//                    } catch (Exception e) {
//                        Toast.makeText(getActivity(), "Error uploading!", Toast.LENGTH_LONG).show();
//                    }
//
//
//                }
////            }

        }else{
            Toast.makeText(getActivity(), "Still running!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private class uploadMongoChallenge extends AsyncTask<Void, Void, Integer> {
        private Activity activity;
        private int challengeType;
        boolean won;

        public uploadMongoChallenge(Activity activity, int challengeType, boolean won) {
            this.activity = activity;
            this.challengeType = challengeType;
            this.won = won;
        }

        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(Void... unused) {
            SyncHelper sh = new SyncHelper(activity);

            if (challengeType==1)

                sh.createMongoChallenge(opponentUsername, totalTime, totalDistance, latLonList, descriptionForChallenge);

            else if (challengeType==2){


                sh.replyToChallenge(challenge.getUser_name(), won);

                
            }



            return 0;

        }

        @Override
        protected void onPostExecute(Integer result) {
            challenge = null;
        }


    }

    public void addListenerOnSpinnerItemSelection(View v) {
        Spinner spinner1 = (Spinner) v.findViewById(R.id.friendsSpinner);
        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }

    class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            opponentUsername = usernames.get(pos);

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // Nothing
        }

    }


//    public void changeSaveListener(){
//        buttonStartStop.setVisibility(View.VISIBLE);
//        buttonStartStop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Toast.makeText(getActivity(), "Successful challenge end!", Toast.LENGTH_LONG).show();
//                try {
//
//
//                    new uploadMongoChallenge(getActivity(), 2, true).execute();
//                } catch (Exception e) {
//                    Toast.makeText(getActivity(), "Error uploading!", Toast.LENGTH_LONG).show();
//                }
//
//            }
//        });
//    }

    private void resetValues(){
        targetDistance=0;
        totalDistance=0;
        totalTime=0;
        goalReached=false;
        googleMap.clear();
    }


    public void setListeners(View v){


        //clear will always set everyhing to default
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                if (!app_prefs.getBoolean("hidePopup", false)){
//
//
//                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
//                            getActivity());
//
//                    // set title
//                    alertDialogBuilder.setTitle("Are you sure?");
//
//                    // set dialog message
//                    alertDialogBuilder
//                            .setMessage("Clear map and lose your progress?")
//                            .setCancelable(false)
//                            .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    dialog.cancel();
//                                }
//                            })
//                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    resetValues();
//                                }
//                            });
//
//                    // create alert dialog
//                    AlertDialog alertDialog = alertDialogBuilder.create();
//
//                    // show it
//                    alertDialog.show();
//
//
//
//                }else{
                resetValues();
                showFrame();
                clearViews();
//                }
            }
        });



        buttonResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (goalReached) {


                    if (((ActMainTest)getActivity()).isNetworkAvailable()) {

                        uploadChallenge();
                        clearViews();
                        resetValues();
                        showFrame();
                    }else{
                        Toast.makeText(getActivity(),"Please connect to the internet to upload",Toast.LENGTH_LONG).show();
                    }

                } else {

                    resumeRun();
                }
            }
        });


        actionButtons = (LinearLayout) v.findViewById(R.id.actionButtons);
        actionButtons.setVisibility(View.GONE);

        fl = (FrameLayout) v.findViewById(R.id.frameWithMap);


        buttonTarget = (Button) v.findViewById(R.id.buttonTarget);
        ll = (LinearLayout) v.findViewById(R.id.targetWindow);
        rl = (RelativeLayout) v.findViewById(R.id.textViews);
        buttonStartStop.setVisibility(View.GONE);
        rl.setVisibility(View.GONE);
        targetDist = (EditText) v.findViewById(R.id.targetValue);
        description = (EditText) v.findViewById(R.id.chalDesc);


        buttonTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validateDistance();

            }
        });


    }

    private void validateDistance(){




        try {

            targetDistance = Float.parseFloat(targetDist.getText().toString());
            if (targetDistance<10){
                Toast.makeText(getActivity(),"Enter a valid distance in meters(>=10)", Toast.LENGTH_LONG).show();
                return;
            }

        }catch (Exception e){
            Toast.makeText(getActivity(),"Enter a valid distance in meters", Toast.LENGTH_LONG).show();
            return;
        }
        ll.setVisibility(View.GONE);

        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(buttonTarget.getWindowToken(), 0);

        buttonStartStop.setVisibility(View.VISIBLE);
    }

    public void initializeMap(){
        SupportMapFragment fm = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapChalKostas);
        googleMap = fm.getMap();

        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);


        startMarker = googleMap.addMarker(new MarkerOptions()
//                        .infoWindowAnchor(0.48f, 4.16f)

                        .position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                        .title("You are here")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))

        );

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

        System.out.println("Provider " + provider + " has been selected.");
//        onLocationChanged(lastLocation);
    }

    /* Request updates at startup */
    @Override
    public void onResume() {
        super.onResume();
//        locationManager.requestLocationUpdates(provider, 2000, 0, this);
    }
//
//    /* Remove the locationlistener updates when Activity is paused */
//    @Override
//    public void onPause() {
//        super.onPause();
//        locationManager.removeUpdates(this);
//    }

    @Override
    public void onLocationChanged(Location location) {


        if (startMarker==null){
            lastLocation=location;

            latLonList="";
            latLonList = String.valueOf(location.getLatitude())+","+ String.valueOf(location.getLongitude());

            googleMap.clear();
            startMarker = googleMap.addMarker(new MarkerOptions()
//                        .infoWindowAnchor(0.48f, 4.16f)

                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("You are here")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))
            );
            return;
        }
        //runner marker gets moved every time we move, while start marker displays our first position
        if (runnerMarker!=null) {
            runnerMarker.remove();
        }
        else{//this is the first update so we moe our start marker to be sure
            googleMap.clear();
            startMarker = googleMap.addMarker(new MarkerOptions()
//                        .infoWindowAnchor(0.48f, 4.16f)

                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("You are here")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))
            );
            runnerMarker = googleMap.addMarker(new MarkerOptions()
//                        .infoWindowAnchor(0.48f, 4.16f)

                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("You are here")
            );
//            runnerMarker.setVisible(false);
            firstChange=true;
            lastLocation = location;
            return;
        }

//        runnerMarker.setVisible(true);
        runnerMarker = googleMap.addMarker(new MarkerOptions()
//                        .infoWindowAnchor(0.48f, 4.16f)

                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("You are here")
        );

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));

        //todo try zoomby to keep the users zoom every time
        googleMap.animateCamera(CameraUpdateFactory.zoomBy(0), 2000, null);



        //the first polyline is the distance between my last location and my current
        //so it might be kilometers away and should not be accounted for
        if (firstChange) {

            totalDistance += location.distanceTo(lastLocation);

            latLonList +=","+ String.valueOf(location.getLatitude())+","+ String.valueOf(location.getLongitude());


            PolylineOptions line =
                    new PolylineOptions().add(new LatLng(location.getLatitude(),
                                    location.getLongitude()),
                            new LatLng(lastLocation.getLatitude(),
                                    lastLocation.getLongitude())
                    )
                            .width(5).color(Color.RED);

            lastLocation = location;

            Polyline pline = googleMap.addPolyline(line);
            mapLines.add(pline);

            // getSpeed is in meters/second so km/hour is meters*1000  / seconds*3600
            textChalSpeed.setText("Speed: " + String.format("%1$,.2f", ((location.getSpeed() * 3600) / 1000)));

            textChalSpeedAvg.setText("Avg Speed: " + String.format("%1$,.2f", (double) (totalDistance) / (double) (totalTime / (3600))));

            textChalDistance.setText("Distance: " + String.format("%1$,.2f", (double) (totalDistance / 1000))+" / "+targetDistance);


            if (totalDistance>=targetDistance){

                goalReached=true;
                running=false;
                getUpdates(false);
                //he has achieved the distance and it is a challenge


                if (challenge==null) {

                    buttonResume.setText("Upload challenge");

                    buttonStartStop.setText("Start");

                    hideFrame();
                }else{//he won the challenge

                    showWinLoseDialog(1,true);

                }
            }
            
            
        }else{
            firstChange = true;
        }


    }

    public Location getLastLocation(){

        Location bestLocation = null;

        long minTime=Long.MAX_VALUE;  float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;

        List<String> matchingProviders = locationManager.getAllProviders();
        for (String provider: matchingProviders) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();



                if ((time > minTime && accuracy < bestAccuracy)) {
                    bestLocation = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                }
                else if (time < minTime &&
                        bestAccuracy == Float.MAX_VALUE && time > bestTime){
                    bestLocation = location;
                    bestTime = time;
                }
            }
        }
        return bestLocation;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
//        textChalProvider.setText("Enabled new provider " + provider);

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getActivity(), "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();

        selectProvider();
    }



    private Runnable mUpdateTimeTask = new Runnable(){

        public void run() {

            final long start = mStartTime;
            totalTime = SystemClock.uptimeMillis()- start;

            int seconds = (int) (totalTime / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            textChalTimer.setText("" + minutes + ":"
                    + String.format("%02d", seconds));

            timerStop1 = minutes + ":"
                    + String.format("%02d", seconds);

            mHandler.postDelayed(this, 200);

            if (challenge!=null && totalTime>targetTime){
                getUpdates(false);

                showWinLoseDialog(1,false);



                
            }


        }
    };


    static FrgShowLocation init(int val) {
        FrgShowLocation truitonList = new FrgShowLocation();

        // Supply val input as an argument.
        Bundle args = new Bundle();
        args.putInt("val", val);
        truitonList.setArguments(args);

        return truitonList;
    }


    class MyAdapter extends ArrayAdapter<String> {
        List<String>names;

        public MyAdapter(Context ctx, int txtViewResourceId, List<String>names) {
            super(ctx, txtViewResourceId,names);
            this.names = names;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomDropView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            ViewHolderFriend holder;
            if (convertView == null || !(convertView.getTag() instanceof ViewHolderFriend)) {

                convertView = inflater.inflate(R.layout.custom_spinner_header, null);
                holder = new ViewHolderFriend();
                holder.name = (TextView) convertView.findViewById(R.id.spinnerHeaderName);
//                holder.profileImg = (ImageView) convertView.findViewById(R.id.like);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolderFriend) convertView.getTag();
            }
            holder.name.setText(names.get(position));
//            holder.profileImg.setBackgroundResource(R.drawable.ic_launcher);

            return convertView;
        }


        public View getCustomDropView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            ViewHolderFriend holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.custom_spinner_dropdown, null);
                holder = new ViewHolderFriend();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.profileImg = (ImageView) convertView.findViewById(R.id.like);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolderFriend) convertView.getTag();
            }

            String friend = names.get(position);
            holder.name.setText(friend);//FIXME Only for the first account
            holder.profileImg.setBackgroundResource(android.R.drawable.ic_dialog_email);
            return convertView;
        }
    }

    private class ViewHolderFriend {
        TextView name;
        ImageView profileImg;
        TextView info;
    }


    public void beginChallenge(Running run){


//        TextView opponentComment = (TextView) getView().findViewById(R.id.opponentComment);
//        opponentComment.setText(run.getDescription());
//        if (run.getDescription().length()>1)
//            opponentComment.setVisibility(View.VISIBLE);


        ll.setVisibility(View.GONE);
        resetValues();
        challenge = run;
        targetDistance = challenge.getDistance();
        targetTime = challenge.getTime();
        showWinLoseDialog(2, true);


//        changeSaveListener();


        buttonStartStop.setVisibility(View.VISIBLE);
        buttonStartStop.setBackgroundColor(getResources().getColor(R.color.runner_red));

        Toast.makeText(getActivity(),"This is "+run.getUser_name()+"'s run!",Toast.LENGTH_LONG).show();



        String[] pointsList = run.getLatLonList().split(",");

        List<LatLng> locationList = new ArrayList<LatLng>();

        int pointsLength = pointsList.length;

        for (int i=0; i<pointsLength-1; i+=2){

            locationList.add(new LatLng(Double.parseDouble(pointsList[i]), Double.parseDouble(pointsList[i + 1])));

        }
        int latlonLength = locationList.size();


        for (int i=0; i<latlonLength-1; i++){


            PolylineOptions line  = new PolylineOptions().add(locationList.get(i), locationList.get(i + 1)).width(5).color(Color.RED);

            Polyline pline = googleMap.addPolyline(line);


            mapLines.add(pline);

        }



        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(pointsList[(pointsLength)-2]), Double.parseDouble(pointsList[pointsLength - 1])), 18));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);

    }


    private void resumeRun(){
        ((ActMainTest)getActivity()).togglePagerClickable(false);
        showFrame();

        running=true;

        if(mStartTime == 0L){
            mStartTime = SystemClock.uptimeMillis()-totalTime;
            mHandler.removeCallbacks(mUpdateTimeTask);
            mHandler.postDelayed(mUpdateTimeTask, 100);

        }
        locationManager.requestLocationUpdates(provider, 2000, 3, this);
        buttonStartStop.setText("Stop");




    }

    private void clearViews(){

        textChalSpeed.setText("Speed: 0.0");

        textChalSpeedAvg.setText("Avg Speed: 0.0");

        textChalDistance.setText("Distance: 0.0");

        textChalTimer.setText("0:00");

        ll.setVisibility(View.VISIBLE);
        buttonStartStop.setVisibility(View.GONE);
        description.setVisibility(View.GONE);

//        TextView opponentComment = (TextView) getView().findViewById(R.id.opponentComment);
//
//        opponentComment.setVisibility(View.GONE);

        rl.setVisibility(View.GONE);

        //todo animate the camera to where i am



    }


    private void hideFrame(){

        if (challenge==null&&goalReached) {
            description.setVisibility(View.VISIBLE);
            description.setHint("Tell something to " + opponentUsername);
        }

        fl.setVisibility(View.GONE);
        actionButtons.setVisibility(View.VISIBLE);
    }

    private void showFrame(){
        fl.setVisibility(View.VISIBLE);
        actionButtons.setVisibility(View.GONE);
    }

    public void setSaveListener(){


            buttonStartStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!running) {
                        // if he is not running means either that:
                        // 1) he has reached his goal and it has stopped automatically
                        // 2) he has not yet started the challenge


                            rl.setVisibility(View.VISIBLE);

                            if (provider != null)
                                getUpdates(true);
                            else
                                selectProvider();


                    } else {

                        //he quits the challenge!!

                        // alert dialog to ask if he wants to quit!
                        getUpdates(false);
                        hideFrame();

                    }

                }
            });


    }

    private void showWinLoseDialog(final int type, boolean won){

        String message;

        if (type==1) {

            message = "Well done! You beat " + challenge.getUser_name();
            if (!won) message = "Oh no! You lost to " + challenge.getUser_name();

            if (((ActMainTest)getActivity()).isNetworkAvailable()) {
                new uploadMongoChallenge(getActivity(), 2, won).execute();
            }else{

                //todo store action in app prefs variable for next time
                storeOfflineAction(challenge.getUser_name(), won);

            }


        }else{
            message = challenge.getUser_name()+" says: '"+challenge.getDescription()+"'";
        }


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            getActivity());

                    // set title
                    alertDialogBuilder.setTitle("Challenge");

                    // set dialog message
        alertDialogBuilder
                            .setMessage(message)
                            .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if (type==1) {
                            resetValues();
                            clearViews();
                        }
                        dialog.cancel();
                    }
                            });


                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();

    }

    private void storeOfflineAction(String user, boolean won){

        ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);

        String offline = app_preferences.getString("offline","");
        offline+= user+" "+won+"/";

        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putString("offline", offline);
        editor.commit();


    }



}
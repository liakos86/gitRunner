package com.example.gpsCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.example.gpsCheck.dbObjects.Running;
import com.example.gpsCheck.model.Database;
import com.example.gpsCheck.service.RunningService;
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
    boolean firstChange=false, goalReached=false, paused=false, singleUpdate=false, running=false;
    String timerStop1;
    LinearLayout ll, actionButtons, targetLayout;
    RelativeLayout rl;
    FrameLayout fl;
    EditText targetDist, description ;
    MyAdapter adapter;
    Running challenge;
    String latLonList="";

    ImageView searchIcon;

    List<Polyline>mapLines;
    List<String> retList;



    private TextView textChalSpeed, textChalSpeedAvg, textChalDistance, textChalTimer;
    float totalDistance=0, targetDistance, targetTime;


    String descriptionForChallenge, opponentUsername;

    List<String>usernames;

    ClearableAutoCompleteTextView friendsAutoComplete;

    ArrayAdapter searchAdapter;




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

        setAutoComplete(v);

//        setSpinner(v);
        selectProvider();

        if (provider==null){

            Toast.makeText(getActivity(),"Cannot get location provider", Toast.LENGTH_LONG).show();
//            return v;
        }

        lastLocation = getLastLocation();// locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
//        if (lastLocation != null) {

        initializeMap();

        // Get the button view
        View locationButton = ((View) v.findViewById(1).getParent()).findViewById(2);

        // and next place it, for exemple, on bottom right (as Google Maps app)
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 20, 200);


//        }

//        setSpinner(v);

        //default is personal run
        setSaveListener();

//        if (((ExtApplication) getActivity().getApplication()).isRunning()){
//            Toast.makeText(getActivity(), "back from out", Toast.LENGTH_LONG).show();
//            drawRoute(((ExtApplication) getActivity().getApplication()).getLatLonList());
//        }



        return  v;
    }

    private class ViewHolder {
        TextView name;

    }


    public void setAutoComplete(View v){

        ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);

        usernames=new ArrayList<String>();
        String[]names = app_preferences.getString("friends","").split(" ");
        for (String name:names)if (name!=null && !name.equals("") && !name.equals("null"))  usernames.add(name);


        Point pointSize = new Point();

        getActivity().getWindowManager().getDefaultDisplay().getSize(pointSize);

        friendsAutoComplete = (ClearableAutoCompleteTextView) v.findViewById(R.id.friendsAuto);
        friendsAutoComplete.setDropDownWidth(pointSize.x);
        friendsAutoComplete.setDropDownVerticalOffset(5);
        friendsAutoComplete.getDropDownBackground().setAlpha(150);
//        friendsAutoComplete.hideClearButton();

        friendsAutoComplete.requestFocus();

        friendsAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // handle clicks on search resaults here
                opponentUsername=retList.get(position);
                targetDist.setHint("Meters to challenge "+opponentUsername);
                toggleSearch(true);
//                Toast.makeText(getActivity(),retList.get(position),Toast.LENGTH_LONG).show();
            }

        });



        // adapter for the search dropdown auto suggest
        searchAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.friends_auto_drop) {
            private Filter filter;

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {

                ViewHolder holder =null;
                if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.friends_auto_drop, parent, false);

                    holder = new ViewHolder();


                    holder.name = (TextView) convertView
                            .findViewById(R.id.countryName);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();

                }


                //TODO When typing quickly and pressing backspace
                //TODO the retList wont be available soon enough
                if (retList.size() > position) {

                    holder.name.setText(retList.get(position));
                    holder.name.setTextColor(getResources().getColor(R.color.mthMbColor));

                }

                return convertView;
            }

            @Override
            public Filter getFilter() {
                if (filter == null) {
                    filter = new friendFilter();
                }
                return filter;
            }
        };

//       friendsAutoComplete.setThreshold(1);

        friendsAutoComplete.setAdapter(searchAdapter);
    }

    protected void toggleSearch(boolean reset) {
//        ImageView searchIcon = (ImageView) findViewById(R.id.search_icon);
        if (reset) {
            // hide search box and show search icon
            friendsAutoComplete.setText("");
            friendsAutoComplete.setVisibility(View.GONE);
            targetLayout.setVisibility(View.VISIBLE);

//            searchIcon.setVisibility(View.VISIBLE);
            // hide the keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(friendsAutoComplete.getWindowToken(), 0);
        } else {
            // hide search icon and show search box
//            searchIcon.setVisibility(View.GONE);
            friendsAutoComplete.setVisibility(View.VISIBLE);
            friendsAutoComplete.requestFocus();
            // show the keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(friendsAutoComplete, InputMethodManager.SHOW_IMPLICIT);
        }

    }

    public void refreshUsernames(){
        ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);

        usernames.clear();
        String[]names = app_preferences.getString("friends","").split(" ");
//        usernames.add("Select a friend to challenge");
        for (String name:names)if (name!=null && !name.equals("") && !name.equals("null"))  usernames.add(name);
//        if (usernames.size()==1) selectUsernameSpinner.setClickable(false);

        if (adapter!=null) adapter.notifyDataSetChanged();
    }

    private void setSpinner(View v) {
//        selectUsernameSpinner = (Spinner) v.findViewById(R.id.friendsSpinner);
        usernames=new ArrayList<String>();

        refreshUsernames();

        adapter = new MyAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, usernames);//FIXME Only for the first account
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);

//        addListenerOnSpinnerItemSelection(v);
//        selectUsernameSpinner.setAdapter(adapter);
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
//            selectUsernameSpinner.setClickable(false);
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

//            selectUsernameSpinner.setClickable(false);

            firstChange=false;

            if (googleMap!=null){

                for (Polyline line:mapLines){
                    line.remove();
                }

            }
//            googleMap.clear();
            running=true;
//            ((ExtApplication) getActivity().getApplication()).setRunning(true);

//            Toast.makeText(getActivity(), "motivian",Toast.LENGTH_LONG).show();
//            startRunningService();
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
//            selectUsernameSpinner.setClickable(true);

            running=false;
//            ((ExtApplication) getActivity().getApplication()).setRunning(false);


            mHandler.removeCallbacks(mUpdateTimeTask);

            textChalTimer.setText(timerStop1);
            mStartTime = 0L;

        }

    }

    public void uploadChallenge(){



        if (!running){// ((ExtApplication) getActivity().getApplication()).isRunning()){


            ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();
            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);
            SharedPreferences.Editor editor = app_preferences.edit();

            editor.putLong("totalTime", app_preferences.getLong("totalTime", 0)+totalTime);
            editor.putFloat("totalDistance", app_preferences.getFloat("totalDistance", 0) + totalDistance);
            editor.commit();

            descriptionForChallenge = description.getText().toString().trim();

            if (((ActMainTest)getActivity()).isNetworkAvailable()) {

                Toast.makeText(getActivity(), "Saving...", Toast.LENGTH_LONG).show();

                if (totalDistance==0)
                    Toast.makeText(getActivity(), "ZERO...", Toast.LENGTH_LONG).show();


                new uploadMongoChallenge(getActivity(), 1, true).execute();
            }else{
                Toast.makeText(getActivity(), "Will upload challenge when connected...", Toast.LENGTH_LONG).show();
                storeOfflineAction(opponentUsername, true, 0);
            }


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

//    public void addListenerOnSpinnerItemSelection(View v) {
//        Spinner spinner1 = (Spinner) v.findViewById(R.id.friendsSpinner);
//        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
//    }

    class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            opponentUsername = usernames.get(pos);
            if (pos==0){
                targetLayout.setVisibility(View.GONE);
            }else{
                targetLayout.setVisibility(View.VISIBLE);
            }

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
        if (googleMap!=null)
        googleMap.clear();
        opponentUsername=null;
        challenge=null;
        running=false;
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

                    uploadChallenge();
                    clearViews();
                    resetValues();
                    showFrame();

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
        targetLayout = (LinearLayout) v.findViewById(R.id.targetLayout);

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

//            if (selectUsernameSpinner.getSelectedItemPosition()==0){
//                Toast.makeText(getActivity(),"You have to select a friend", Toast.LENGTH_LONG).show();
//                return;
//            }

            if (opponentUsername==null||opponentUsername.equals("")){
                Toast.makeText(getActivity(),"You have to select a friend", Toast.LENGTH_LONG).show();
                return;
            }


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

        if (googleMap!=null) {

            googleMap.setMyLocationEnabled(true);
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }


        if (lastLocation!=null && googleMap!=null) {
            startMarker = googleMap.addMarker(new MarkerOptions()
//                        .infoWindowAnchor(0.48f, 4.16f)

                            .position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                            .title("You are here")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_marker))

            );

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
        }
        System.out.println("Provider " + provider + " has been selected.");
//        onLocationChanged(lastLocation);
    }

    private void getOneLocationUpdate(){
        if (provider==null) provider="gps";

        singleUpdate = true;
        locationManager.requestSingleUpdate(provider, this, null);
    }

    /* Request updates at startup */
    @Override
    public void onResume() {
        super.onResume();

        if (running)
        getOneLocationUpdate();
    }
//
//    /* Remove the locationlistener updates when Activity is paused */
    @Override
    public void onPause() {
        super.onPause();
//        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {

        if (singleUpdate){

            singleUpdate=false;
            return;
        }


        if (startMarker==null){
            lastLocation=location;

            latLonList="";
//            ((ExtApplication) getActivity().getApplication()).setLatLonList("");

            latLonList = String.valueOf(location.getLatitude())+","+ String.valueOf(location.getLongitude());

//            ((ExtApplication) getActivity().getApplication()).setLatLonList(String.valueOf(location.getLatitude())+","+ String.valueOf(location.getLongitude()));

            googleMap.clear();
            startMarker = googleMap.addMarker(new MarkerOptions()
//                        .infoWindowAnchor(0.48f, 4.16f)

                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("You are here")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_marker))
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
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_marker))
            );
            runnerMarker = googleMap.addMarker(new MarkerOptions()
//                        .infoWindowAnchor(0.48f, 4.16f)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.runner_marker))

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
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.runner_marker))

                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("You are here")
        );

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));

        //todo try zoomby to keep the users zoom every time
        googleMap.animateCamera(CameraUpdateFactory.zoomBy(0), 2000, null);



        //the first polyline is the distance between my last location and my current
        //so it might be kilometers away and should not be accounted for
        if (firstChange&&!paused) {

            totalDistance += location.distanceTo(lastLocation);

            latLonList+=","+String.valueOf(location.getLatitude())+","+ String.valueOf(location.getLongitude());

//            ((ExtApplication) getActivity().getApplication()).setLatLonList( ((ExtApplication) getActivity().getApplication()).getLatLonList()+","+String.valueOf(location.getLatitude())+","+ String.valueOf(location.getLongitude()));


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

            textChalDistance.setText("Dist: " + String.format("%1$,.2f", (double) (totalDistance / 1000))+" / "+String.format("%1$,.2f", (double) targetDistance/1000));


            if (totalDistance>=targetDistance){

                goalReached=true;
                running=false;
//                ((ExtApplication) getActivity().getApplication()).setRunning(false);
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


        }else if (!firstChange){
            firstChange = true;
        }else if (paused){
            lastLocation = location;
            paused = false;
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
        Toast.makeText(getActivity(), "Enabled provider " + provider,
                Toast.LENGTH_SHORT).show();
//        selectUsernameSpinner.setClickable(true);
        initializeMap();
        LinearLayout noLocation  = (LinearLayout) getView().findViewById(R.id.noLocation);
        noLocation.setVisibility(View.GONE);

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getActivity(), "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();

        LinearLayout noLocation  = (LinearLayout) getView().findViewById(R.id.noLocation);
        noLocation.setVisibility(View.VISIBLE);

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


    public void beginChallenge(Running run, int type){


        ll.setVisibility(View.GONE);
        resetValues();
        challenge = run;

        if (type==0) {
            targetDistance = challenge.getDistance();
            targetTime = challenge.getTime();
            showWinLoseDialog(2, true);

            Toast.makeText(getActivity(), "This is " + run.getUser_name() + "'s run!", Toast.LENGTH_LONG).show();
        }else{

            targetDistance=-1;
            buttonStartStop.setText("Close");

        }

        buttonStartStop.setVisibility(View.VISIBLE);
        buttonStartStop.setBackgroundColor(getResources().getColor(R.color.runner_red));



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



        Location first = new Location("gps");
        first.setLatitude(Double.parseDouble(pointsList[0]));
        first.setLongitude(Double.parseDouble(pointsList[1]));
        Location last = new Location("gps");
        last.setLatitude(Double.parseDouble(pointsList[pointsLength-2]));
        last.setLongitude(Double.parseDouble(pointsList[pointsLength-1]));


        int middle =(int)(pointsLength/2);
        if (!(middle%2==0)) ++middle;
        String middleLat = pointsList[middle];
        String middleLon = pointsList[++middle];

        int zoom = 19 - (int)run.getDistance()/1000;

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(middleLat), Double.parseDouble(middleLon)), zoom));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoom), 2000, null);

    }

    private void drawRoute(String list){


        list.replace(" ","");
        String[] pointsList = list.split(",");

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


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(pointsList[pointsLength-2]), Double.parseDouble(pointsList[pointsLength-1])), 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);


    }


    private void resumeRun(){
        ((ActMainTest)getActivity()).togglePagerClickable(false);
        showFrame();

        running=true;

//        ((ExtApplication) getActivity().getApplication()).setRunning(true);


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

        targetDist.setText(""); description.setText("");

//        selectUsernameSpinner.setSelection(0);

        textChalTimer.setText("0:00");

        ll.setVisibility(View.VISIBLE);
        friendsAutoComplete.setVisibility(View.VISIBLE);
        targetLayout.setVisibility(View.GONE);
        buttonStartStop.setVisibility(View.GONE);
        buttonStartStop.setText("Start");
        buttonResume.setText("Resume");
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

        paused=true;

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

                if (targetDistance != -1) {

                    if (!running){// ((ExtApplication) getActivity().getApplication()).isRunning()) {
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

                } else {
                    clearViews();
                    resetValues();
                    animateTo(getLastLocation());
                }
            }
        });


    }

    private void animateTo(Location loc){
        if (googleMap!=null && loc!=null){
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 15));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
        }


    }

    private void showWinLoseDialog(final int type, boolean won){

        String message;

        if (type==1) {


            ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();
            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);
            SharedPreferences.Editor editor = app_preferences.edit();

            editor.putLong("totalTime", app_preferences.getLong("totalTime", 0)+totalTime);
            editor.putFloat("totalDistance", app_preferences.getFloat("totalDistance", 0) + totalDistance);
            editor.commit();


            message = "Well done! You beat " + challenge.getUser_name();
            if (!won) message = "Oh no! You lost to " + challenge.getUser_name();

            if (((ActMainTest)getActivity()).isNetworkAvailable()) {
                new uploadMongoChallenge(getActivity(), 2, won).execute();
            }else{

                //todo store action in app prefs variable for next time
                storeOfflineAction(challenge.getUser_name(), won, 1);

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

    private void storeOfflineAction(String user, boolean won, int type){//0 new chal, 1 respond

        ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);
        SharedPreferences.Editor editor = app_preferences.edit();

        if (type==1) {

            String offline = app_preferences.getString("offline", "");
            offline += user + " " + won + "/";


            editor.putString("offline", offline);
        }else if (type==0){
            String offline = app_preferences.getString("offlineCreate", "");
            offline += user + " " + totalTime +" "+totalDistance+" "+descriptionForChallenge+" "+latLonList+ "/";


            editor.putString("offlineCreate", offline);
        }
        editor.commit();


    }

    private void stopRunningService(){
        if (running){//((ExtApplication)getActivity().getApplication()).isRunning()) {
            getActivity().stopService(new Intent(getActivity().getBaseContext(), RunningService.class));
        }
    }


    private void startRunningService() {

        // add info for the service which file to download and where to store
//            intent.putExtra(RunningService.LATLONLIST, ((ExtApplication) getApplication()).getLatLonList());



//            Intent resultIntent = new Intent(getActivity(), ActMainTest.class);
//            resultIntent.putExtra("latLonList", ((ExtApplication) getActivity().getApplication()).getLatLonList());
//            //    Because clicking the notification opens a new ("special") activity, there's
//            //    no need to create an artificial back stack.
//            PendingIntent resultPendingIntent =
//                    PendingIntent.getActivity(
//                            getActivity(),
//                            0,
//                            resultIntent,
//                            PendingIntent.FLAG_UPDATE_CURRENT
//                    );
//
//            ((ExtApplication) getActivity().getApplication()).setmBuilder(
//                    new NotificationCompat.Builder(getActivity())
//                            .setSmallIcon(R.drawable.ic_waiting_me_32)
//                            .setContentTitle("You are running")
//                            .setContentText("Go to workout")
//                            .setOngoing(true)
//                            .setContentIntent(resultPendingIntent));
//
//
//            //Sets an ID for the notification
//            int mNotificationId = 001;
//            //Gets an instance of the NotificationManager service
//            NotificationManager mNotifyMgr =
//                    (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
//            //Builds the notification and issues it.
//            mNotifyMgr.notify(mNotificationId, ((ExtApplication) getApplication()).getmBuilder().build());


            Intent intent = new Intent(getActivity().getBaseContext(), RunningService.class);
            intent.putExtra("username", opponentUsername);
            getActivity().startService(intent);

    }


    private class friendFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<String> list = new ArrayList<String>(usernames);
            FilterResults result = new FilterResults();
            String substr = null;
            if (constraint!=null) {
                substr = constraint.toString().toLowerCase();
            }

            // if no constraint is given, return the whole list
            if ((substr == null || substr.length() == 0)) {
                result.values = list;
                result.count = list.size();
                retList = list;

//                result.count =0;
            }else {

//                friendsAutoComplete.setImgClearButton(getResources().getDrawable(R.drawable.ic_cancel_32));
                // iterate over the list of stores and find if the store matches the constraint. if it does, add to the result list
                retList = new ArrayList<String>();
                for (String friend : list) {
                    try {
                        if (friend.toLowerCase().contains(substr)) {
                            retList.add(friend);


                        }
                    } catch (Exception e) {
                        Log.i("ERROR", e.getMessage());
                    }
                }
                result.values = retList;
                result.count = retList.size();
            }


            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // we clear the adapter and then pupulate it with the new results
            searchAdapter.clear();
            if (results.count > 0) {


                for (String o : (ArrayList<String>) results.values) {
                    searchAdapter.add(o);
                }
            }
        }

    }



}
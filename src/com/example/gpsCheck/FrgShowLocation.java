package com.example.gpsCheck;

import android.app.*;
import android.content.*;
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
    Button buttonTarget, buttonResume;
    ImageButton buttonStartStop, clear;
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

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();
            if (bundle != null) {

                totalDistance = bundle.getFloat(RunningService.TOTAL_DIST);
                Log.v("LATLON","DIST FROM SERVICE : "+ totalDistance);
                if (latLonList==null || latLonList.trim().equals("")){//if it is empty it is the first change or we come back from out

                    Log.v("LATLON","INTO SERVICE 1: "+ bundle.getString(RunningService.LATLONLIST));
//                    Toast.makeText(getActivity(),
//                            "new list " + latLonList,
//                            Toast.LENGTH_LONG).show();
                    latLonList = bundle.getString(RunningService.LATLONLIST);
                    goalReached = bundle.getBoolean(RunningService.GOAL_REACHED);
                    if (opponentUsername==null || opponentUsername.equals("")){
                        opponentUsername = bundle.getString(RunningService.OPPONENT);
                    }

                    if (goalReached){
                        buttonStartStop.performClick();
                    }else {
                        if (!latLonList.equals("")) {
                            Log.v("LATLON","INTO SERVICE 1: drawRoute();");
                            drawRoute(latLonList, false);
                        }
                    }

                }else {//we get a new location and add it to the existing

                    String newLoc = bundle.getString(RunningService.NEW_LOCATION);
//                    Toast.makeText(getActivity(),
//                            "Download complete. Download URI: " + newLoc,
//                            Toast.LENGTH_LONG).show();
                    Location first = new Location("gps");
                    first.setLatitude(Double.parseDouble(newLoc.split(",")[0]));
                    first.setLongitude(Double.parseDouble(newLoc.split(",")[1]));

                    Log.v("LATLON","INTO SERVICE 2: "+ bundle.getString(RunningService.LATLONLIST));
                    addLocationToRun(first);
                }

            }else{
                Log.v("LATLON", "Bundle is null");

            }
        }
    };

    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.showlocation_frg, container, false);

        setTextViewsAndButtons(v);

        setListeners(v);
        setAutoComplete(v);
        selectProvider();

        if (provider==null){
           Toast.makeText(getActivity(),"Cannot get location provider", Toast.LENGTH_LONG).show();
        }

        lastLocation = getLastLocation();// locationManager.getLastKnownLocation(provider);

        initializeMap();

        moveLocationIcon(v);


        setSaveListener();

//       if  (isMyServiceRunning(RunningService.class)){
//        Log.v("LATLON", "onStart run (Service on)");
////           getExistingLatLonList();
////           getInRunningMode();
//       }

       return  v;
    }

    private void setTextViewsAndButtons(View v){
        textChalSpeed = (TextView) v.findViewById(R.id.textChalSpeed);
        textChalSpeedAvg = (TextView) v.findViewById(R.id.textChalSpeedAvg);
        textChalDistance = (TextView) v.findViewById(R.id.textChalDistance);
        textChalTimer = (TextView) v.findViewById(R.id.textChalTimer);
        buttonStartStop = (ImageButton) v.findViewById(R.id.buttonChalSave);
        clear = (ImageButton) v.findViewById(R.id.buttonChalClear);
        buttonResume = (Button) v.findViewById(R.id.buttonResume);
        mapLines = new ArrayList<Polyline>();
    }

    private void moveLocationIcon(View v){
        // Get the button view
        View locationButton = ((View) v.findViewById(1).getParent()).findViewById(2);

        // and next place it, for exemple, on bottom right (as Google Maps app)
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 20, 200);

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private boolean getExistingLatLonList(){

        ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);
        SharedPreferences.Editor editor = app_preferences.edit();

        String existingList = app_preferences.getString(RunningService.LATLONLIST, null);



        if (existingList!=null) {

            latLonList=existingList;
            Log.v("LATLON", "GOT ON RESUME: "+latLonList);
            editor.putString(RunningService.LATLONLIST, null);
            editor.apply();
            opponentUsername = app_preferences.getString(RunningService.OPPONENT,"");
            targetDistance = app_preferences.getFloat(RunningService.TARGET_DIST,0);


            if (latLonList!=null && !latLonList.equals(""))
            drawRoute(latLonList, true);
            return true;
        }else{
            Log.v("LATLON", "GOT ON RESUME: null");
        }



        return false;
    }

    private void getInRunningMode(){

        buttonStartStop.setVisibility(View.VISIBLE);
        buttonStartStop.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_red));
        buttonStartStop.setImageDrawable(getResources().getDrawable(R.drawable.pause));
//        buttonStartStop.setBackgroundColor(getResources().getColor(R.color.runner_red));
//        buttonStartStop.setText("Stop");
        targetLayout.setVisibility(View.GONE);
        ll.setVisibility(View.GONE);
        rl.setVisibility(View.VISIBLE);
        running= true ;
        firstChange=true;
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

        friendsAutoComplete.setThreshold(1);

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


    public boolean selectProvider(){


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
            return false;
        }else {

            lastLocation = getLastLocation();// locationManager.getLastKnownLocation(provider);
            if (lastLocation!= null) {

                initializeMap();

            }
            return true;
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

            startRunningService();
//            locationManager.requestLocationUpdates(provider, 2000, 3, this);
            buttonStartStop.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_red));
            buttonStartStop.setImageDrawable(getResources().getDrawable(R.drawable.pause));
//            buttonStartStop.setBackgroundColor(getResources().getColor(R.color.runner_red));
//            buttonStartStop.setText("Stop");


            if (startMarker!=null)
            startMarker.remove();
            if (runnerMarker!=null)
            runnerMarker.remove();
            totalDistance =0;




            totalTime = 0L;

            if(mStartTime == 0L){
                mStartTime = SystemClock.uptimeMillis();
                mHandler.removeCallbacks(mUpdateTimeTask);
                mHandler.postDelayed(mUpdateTimeTask, 100);

            }



        }else{
            ((ActMainTest)getActivity()).togglePagerClickable(true);


            stopRunningService(true);

//            locationManager.removeUpdates(this);
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


    private void resetValues(){
        targetDistance=0;
        totalDistance=0;
        latLonList="";
        totalTime=0;
        goalReached=false;
        if (googleMap!=null)
            googleMap.clear();
        opponentUsername=null;
        challenge=null;
        running=false;
        paused=false;
    }


    public void setListeners(View v){


        //clear will always set everyhing to default
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetValues();
//                showFrame();
                clearViews();
                stopRunningService(false);
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

            if ( !isMyServiceRunning(RunningService.class)) {

                if (startMarker!=null) startMarker.remove();

                startMarker = googleMap.addMarker(new MarkerOptions()
//                        .infoWindowAnchor(0.48f, 4.16f)

                                .position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                                .title("You are here")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_marker))

                );
            }

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

        if (isMyServiceRunning(RunningService.class)) {

            running=true;
            getExistingLatLonList();
            getInRunningMode();
            Log.v("LATLON", "Resuming run (Service on)");

            getActivity().registerReceiver(receiver, new IntentFilter(RunningService.NOTIFICATION));
        }else {

//        if (running && !isMyServiceRunning(RunningService.class))
            getOneLocationUpdate();
        }
    }
    //
//    /* Remove the locationlistener updates when Activity is paused */
    @Override
    public void onPause() {

        if (isMyServiceRunning(RunningService.class)) {
            getActivity().unregisterReceiver(receiver);
        }
//        locationManager.removeUpdates(this);
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {

        if (singleUpdate) {

            singleUpdate = false;
            return;
        }
    }


    public void addLocationToRun(Location location) {


        if (startMarker==null){

            Log.v("LATLON", "INTO FRG setting startmarker: "+latLonList);
            lastLocation=location;
            latLonList="";
            latLonList = String.valueOf(location.getLatitude())+","+ String.valueOf(location.getLongitude());

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
            firstChange=true;
            lastLocation = location;

            Log.v("LATLON", "INTO FRG setting startmarker runnermarker: "+latLonList);
            return;
        }

   //just updating the runner marker now
        runnerMarker = googleMap.addMarker(new MarkerOptions()
//                        .infoWindowAnchor(0.48f, 4.16f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.runner_marker))

                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("You are here")
        );

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
        //todo try zoom by to keep the users zoom every time
        googleMap.animateCamera(CameraUpdateFactory.zoomBy(15), 1, null);

        //the first polyline is the distance between my last location and my current
        //so it might be kilometers away and should not be accounted for
        if (firstChange&&!paused) {

//            totalDistance += location.distanceTo(lastLocation);
            latLonList+=","+String.valueOf(location.getLatitude())+","+ String.valueOf(location.getLongitude());
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
            textChalSpeed.setText( String.format("%1$,.2f", ((location.getSpeed() * 3600) / 1000)));
            textChalSpeedAvg.setText( String.format("%1$,.2f", (double) (totalDistance) / (double) (totalTime / (3600))));
            textChalDistance.setText(String.format("%1$,.2f", (double) (totalDistance / 1000))+" / "+String.format("%1$,.2f", (double) targetDistance/1000));

            checkIfFinished();

            Log.v("LATLON", "INTO FRG adding point: "+latLonList);
        }else if (!firstChange){
            Log.v("LATLON", "INTO FRG first change");
            firstChange = true;
        }else if (paused){
            lastLocation = location;
            paused = false;
        }
    }

    private void checkIfFinished(){
        if (totalDistance>=targetDistance){
            goalReached=true;
            running=false;
            getUpdates(false);
            //he has achieved the distance and it is a challenge
            if (challenge==null) {
                buttonResume.setText("Upload challenge");
                clear.setVisibility(View.VISIBLE);
                hideFrame();
            }else{//he won the challenge
                showWinLoseDialog(1,true);
            }
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
            buttonStartStop.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_red));
            buttonStartStop.setImageDrawable(getResources().getDrawable(R.drawable.close));
//            buttonStartStop.setBackgroundColor(getResources().getColor(R.color.runner_red));
//            buttonStartStop.setText("Close");

        }

        buttonStartStop.setVisibility(View.VISIBLE);
        buttonStartStop.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_red));
        buttonStartStop.setImageDrawable(getResources().getDrawable(R.drawable.close));
//        buttonStartStop.setBackgroundColor(getResources().getColor(R.color.runner_red));



        String[] pointsList = run.getLatLonList().split(",");

        List<LatLng> locationList = new ArrayList<LatLng>();

        int pointsLength = pointsList.length;

        double northPoint=-85.05115 , southPoint=85.05115 , eastPoint=-180, westPoint=180;
        LatLng top=new LatLng(0,0), bottom=new LatLng(0,0), left=new LatLng(0,0), right=new LatLng(0,0);

        for (int i=0; i<pointsLength-1; i+=2){


            //create a box that contains the run, then take the center of the diagonal
            if (Double.parseDouble(pointsList[i])>northPoint) {
                northPoint = Double.parseDouble(pointsList[i]);
                top = new LatLng(Double.parseDouble(pointsList[i]), Double.parseDouble(pointsList[i + 1]));
            }
            if (Double.parseDouble(pointsList[i])< southPoint) {
                southPoint = Double.parseDouble(pointsList[i]);
                bottom = new LatLng(Double.parseDouble(pointsList[i]), Double.parseDouble(pointsList[i + 1]));
            }

            if (Double.parseDouble(pointsList[i+1])>eastPoint) {
                eastPoint = Double.parseDouble(pointsList[i+1]);
                right = new LatLng(Double.parseDouble(pointsList[i]), Double.parseDouble(pointsList[i + 1]));
            }
            if (Double.parseDouble(pointsList[i+1])< westPoint) {
                westPoint = Double.parseDouble(pointsList[i+1]);
                left = new LatLng(Double.parseDouble(pointsList[i]), Double.parseDouble(pointsList[i + 1]));
            }

            locationList.add(new LatLng(Double.parseDouble(pointsList[i]), Double.parseDouble(pointsList[i + 1])));

        }
        int latlonLength = locationList.size();


        for (int i=0; i<latlonLength-1; i++){


            PolylineOptions line  = new PolylineOptions().add(locationList.get(i), locationList.get(i + 1)).width(5).color(Color.RED);

            Polyline pline = googleMap.addPolyline(line);


            mapLines.add(pline);

        }

        int zoom = 20;
        boolean allVisible=false;
        while (zoom>8 && !allVisible) {

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint(northPoint, westPoint, southPoint, eastPoint), zoom));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoom), 500, null);

            if (
                    googleMap.getProjection().getVisibleRegion().latLngBounds.contains(top)&&
                    googleMap.getProjection().getVisibleRegion().latLngBounds.contains(bottom)&&
                    googleMap.getProjection().getVisibleRegion().latLngBounds.contains(left)&&
                    googleMap.getProjection().getVisibleRegion().latLngBounds.contains(right)
               ){
                allVisible=true;
            }

            --zoom;

        }

    }

    public LatLng midPoint(double lat1,double lon1,double lat2,double lon2){

        double dLon = Math.toRadians(lon2 - lon1);

        //convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);

        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

        //print out in degrees
        System.out.println(Math.toDegrees(lat3) + " " + Math.toDegrees(lon3));
        googleMap.addMarker(new MarkerOptions()
//                        .infoWindowAnchor(0.48f, 4.16f)

                        .position(new LatLng(Math.toDegrees(lat3),Math.toDegrees(lon3)))
                        .title("You are here")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.error_32))
        );

        return new LatLng(Math.toDegrees(lat3),Math.toDegrees(lon3));
    }

    private void drawRoute(String list, boolean fromResume){

        list.replace(" ","");
        String[] pointsList = list.split(",");

        List<LatLng> locationList = new ArrayList<LatLng>();
        int pointsLength = pointsList.length;

        if (startMarker!=null)
        startMarker.remove();


        int startIndex= fromResume ? 2 : 0;


        for (int i=startIndex; i<pointsLength-1; i+=2){

            if (i==startIndex){
                startMarker = googleMap.addMarker(new MarkerOptions()
//                        .infoWindowAnchor(0.48f, 4.16f)

                                .position(new LatLng(Double.parseDouble(pointsList[i]), Double.parseDouble(pointsList[i + 1])))
                                .title("You are here")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_marker))
                );

            }else if (i==pointsLength-2){

                if (runnerMarker!=null) runnerMarker.remove();

                runnerMarker = googleMap.addMarker(new MarkerOptions()
//                        .infoWindowAnchor(0.48f, 4.16f)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.runner_marker))

                                .position(new LatLng(Double.parseDouble(pointsList[i]), Double.parseDouble(pointsList[i + 1])))
                                .title("You are here")
                );

            }

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
//        showFrame();

        running=true;
        paused=false;

        if(mStartTime == 0L){
            mStartTime = SystemClock.uptimeMillis()-totalTime;
            mHandler.removeCallbacks(mUpdateTimeTask);
            mHandler.postDelayed(mUpdateTimeTask, 100);

        }
        startRunningService();
        buttonStartStop.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_red));
        buttonStartStop.setImageDrawable(getResources().getDrawable(R.drawable.pause));
        clear.setVisibility(View.GONE);
//        buttonStartStop.setBackgroundColor(getResources().getColor(R.color.runner_red));
//        buttonStartStop.setText("Stop");

    }

    private void clearViews(){

        clear.setVisibility(View.GONE);
        textChalSpeed.setText("0.0");
        textChalSpeedAvg.setText("0.0");
        textChalDistance.setText("0.0");
        targetDist.setText(""); description.setText("");
        textChalTimer.setText("0:00");

        ll.setVisibility(View.VISIBLE);
        friendsAutoComplete.setVisibility(View.VISIBLE);
        targetLayout.setVisibility(View.GONE);
        buttonStartStop.setVisibility(View.GONE);
        buttonStartStop.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_green));
        buttonStartStop.setImageDrawable(getResources().getDrawable(R.drawable.replay));
//        buttonStartStop.setBackgroundColor(getResources().getColor(R.color.runner_green));
//        buttonStartStop.setText("Start");
        buttonResume.setText("Resume");
        description.setVisibility(View.GONE);
        rl.setVisibility(View.GONE);

        //todo animate the camera to where i am

    }


    private void hideFrame(){

        if (challenge==null&&goalReached) {
            description.setVisibility(View.VISIBLE);
            description.setHint("Tell something to " + opponentUsername);
        }

        paused=true;

        buttonStartStop.setVisibility(View.GONE);
//        fl.setVisibility(View.GONE);
        actionButtons.setVisibility(View.VISIBLE);
    }

    private void showFrame(){
//        fl.setVisibility(View.VISIBLE);
        buttonStartStop.setVisibility(View.VISIBLE);
        actionButtons.setVisibility(View.GONE);
    }

    public void setSaveListener(){


        buttonStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (targetDistance != -1) {

                    if (paused){
                        Log.v("LATLON", "running");
                        resumeRun();
                    }else if (!running){// ((ExtApplication) getActivity().getApplication()).isRunning()) {
                        // if he is not running means either that:
                        // 1) he has reached his goal and it has stopped automatically
                        // 2) he has not yet started the challenge

                        if (provider != null) {
                            Log.v("LATLON", "rl");
                            rl.setVisibility(View.VISIBLE);
                            getUpdates(true);
                        }
                        else if (selectProvider()) {
                            rl.setVisibility(View.VISIBLE);
                        }


                    } else {

                        //he quits the challenge!!

                        // alert dialog to ask if he wants to quit!
                        getUpdates(false);
                        buttonStartStop.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_green));
                        buttonStartStop.setImageDrawable(getResources().getDrawable(R.drawable.replay));
                        clear.setVisibility(View.VISIBLE);
                        paused = true;
//                        hideFrame();

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

    private void stopRunningService(boolean unregister){
        if (unregister)
        getActivity().unregisterReceiver(receiver);
            getActivity().stopService(new Intent(getActivity().getBaseContext(), RunningService.class));


    }


    private void startRunningService() {

        Intent intent = new Intent(getActivity().getBaseContext(), RunningService.class);
        intent.putExtra(RunningService.OPPONENT, opponentUsername);
        intent.putExtra(RunningService.TARGET_DIST, targetDistance);
        intent.putExtra(RunningService.TOTAL_DIST, totalDistance);
        intent.putExtra(RunningService.LATLONLIST, latLonList);
        getActivity().startService(intent);
        getActivity().registerReceiver(receiver, new IntentFilter(RunningService.NOTIFICATION));

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
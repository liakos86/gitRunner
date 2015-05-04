package com.example.gpsCheck;

        import android.app.Activity;
        import android.content.Context;
        import android.graphics.Color;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.SystemClock;
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
    ViewFlipper viewFlipper;
    Button   save, buttonTarget;
    boolean running=false, firstChange=false, goalReached=false;
    String timerStop1;
    String latLonList="";
    LinearLayout ll;
    EditText et ;

    List<Polyline>mapLines;

    private TextView textChalSpeed, textChalSpeedAvg, textChalDistance, textChalTimer;
    float totalDistance=0, targetDistance;


    String opponentUsername;
    Spinner selectUsernameSpinner;
    Switch typeSwitch;

    int type;//0 personal, 1 challenge

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
        save = (Button) v.findViewById(R.id.buttonChalSave);






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




        return  v;
    }

    private void setSpinner(View v) {
        selectUsernameSpinner = (Spinner) v.findViewById(R.id.friendsSpinner);
        selectUsernameSpinner.setVisibility(View.INVISIBLE);
        String[]names = user.getFriends().split(" ");
        usernames=new ArrayList<String>();
        for (String name:names)if (name!=null && !name.equals(""))  usernames.add(name);

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

            firstChange=false;

            if (googleMap!=null){

                for (Polyline line:mapLines){
                    line.remove();
                }

            }
//            googleMap.clear();
            running=true;
            locationManager.requestLocationUpdates(provider, 2000, 3, this);
            save.setText("Stop");


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
            locationManager.removeUpdates(this);

            running=false;

            if (goalReached){

                if (type==1)
                    save.setText("Upload challenge");
                else if (type==0)
                    save.setText("Save Workout");

            }else {
                save.setText("Start");
            }

            mHandler.removeCallbacks(mUpdateTimeTask);

            textChalTimer.setText(timerStop1);
            mStartTime = 0L;

        }

    }

    public void uploadChallengeOrSaveWorkout(){



        if (!running){

            Toast.makeText(getActivity(),"Saving...",Toast.LENGTH_LONG).show();




            if (type==0) {
                Date now = new Date();
                Running tr = new Running(-1, "I am running alone",
                        totalTime,
                        now.toString(),totalDistance, 1, "",user.getUsername(), latLonList);
                Database db = new Database(getActivity().getBaseContext());
                db.addRunning(tr);
            }else {


                try {



                    new uploadMongoChallenge(getActivity()).execute();


                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Error uploading!", Toast.LENGTH_LONG).show();
                }
            }

        }else{
            Toast.makeText(getActivity(), "Still running!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private class uploadMongoChallenge extends AsyncTask<Void, Void, Integer> {
        private Activity activity;

        public uploadMongoChallenge(Activity activity) {
            this.activity = activity;
        }

        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(Void... unused) {
            SyncHelper sh = new SyncHelper(activity);


                sh.createMongoChallenge(opponentUsername, totalTime, totalDistance, latLonList);

            return 0;

        }

        @Override
        protected void onPostExecute(Integer result) {

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

    public void setListeners(View v){


        typeSwitch = (Switch) v.findViewById(R.id.switcher);

        //set the switch to ON 
        typeSwitch.setChecked(true);
        typeSwitch.setTextOn("Personal");
        typeSwitch.setTextOff("Challenge");
        //attach a listener to check for changes in state
        typeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                    if (isChecked) {
                        type=0;
                        selectUsernameSpinner.setVisibility(View.INVISIBLE);
                        goalReached=true;
                    }
                    else {
                        type=1;
                        selectUsernameSpinner.setVisibility(View.VISIBLE);
                        goalReached=false;

                    }


            }
        });




          buttonTarget = (Button) v.findViewById(R.id.buttonTarget);
          ll = (LinearLayout) v.findViewById(R.id.targetWindow);
          et = (EditText) v.findViewById(R.id.targetValue);

        buttonTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validateDistance();

            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!running) {


                    if (goalReached){
                        uploadChallengeOrSaveWorkout();
                        getUpdates(false);

                    }else {

                        if (provider != null)
                            getUpdates(true);
                        else
                            selectProvider();
                    }


                }else{
                    getUpdates(false);

//                    if (totalDistance>=targetDistance){
//
//                        goalReached=true;
//                        running=false;
//                        getUpdates(false);
//                        save.setText("upload");
//                start.performClick();
//                    }

                }

            }
        });



    }

    private void validateDistance(){




        try {

            targetDistance = Float.parseFloat(et.getText().toString());
//            if (targetDistance<1000){
//                Toast.makeText(getActivity(),"Enter a valid distance in meters(>=1000)", Toast.LENGTH_LONG).show();
//                return;
//            }

        }catch (Exception e){
            Toast.makeText(getActivity(),"Enter a valid distance in meters", Toast.LENGTH_LONG).show();
            return;
        }
        ll.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(buttonTarget.getWindowToken(), 0);
    }

//    public void setListeners(){
//
//        save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//               saveRun(totalDistance, totalTime);
//            }
//        });
//
//
//
//
//
//
//        start.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if (!running) {
//
//                    if (provider!=null)
//                        getUpdates(true);
//                    else
//                        selectProvider();
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
//        clear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (googleMap!=null)
//                    googleMap.clear();
//            }
//        });
//
//
//    }

//    public void saveRun(float totalDistance, long totalTime){
//
//
//
//        if (!running){
//
//
//            Date now = new Date();
//            Running tr = new Running(-1, "I am running",
//                    totalTime,
//                    now.toString(), totalDistance, 0, "-", latLonList);
//
//
//
//            Database db = new Database(getActivity().getBaseContext());
//
//            db.addRunning(tr);
//
//        }else{
//            Toast.makeText(getActivity(), "Still running!",
//                    Toast.LENGTH_SHORT).show();
//        }
//
//    }

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

        if (runnerMarker!=null)
        runnerMarker.remove();

        runnerMarker = googleMap.addMarker(new MarkerOptions()
//                        .infoWindowAnchor(0.48f, 4.16f)

                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("You are here")
        );

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));

        //todo try zoomby to keep the users zoom every time
        googleMap.animateCamera(CameraUpdateFactory.zoomBy(0), 2000, null);




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
//                start.performClick();
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



}

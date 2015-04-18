package com.example.gpsCheck;

        import android.content.Context;
        import android.graphics.Color;
        import android.location.Criteria;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.net.wifi.WifiManager;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.SystemClock;
        import android.support.v4.app.Fragment;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.widget.TextView;
        import android.widget.Toast;
        import android.widget.ViewFlipper;
        import com.example.gpsCheck.dbObjects.Running;
        import com.example.gpsCheck.model.ContentDescriptor;
        import com.example.gpsCheck.model.Database;
        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.*;

        import java.security.Provider;
        import java.util.ArrayList;
        import java.util.Date;
        import java.util.List;

public class FrgShowLocation extends Fragment implements LocationListener {
    private TextView textSpeed, textSpeedAvg, textDistance, textTimer, textProvider;
    private long mStartTime = 0L, totalTime=0L;
    private Handler mHandler = new Handler();
    private LocationManager locationManager;
    private String provider;
    GoogleMap googleMap;
    Marker runnerMarker, startMarker;
    Location lastLocation;
    float totalDistance=0;
    ViewFlipper viewFlipper;
    Button map, info, start, clear, save;
    boolean running=false, firstChange=false;
    String timerStop1;

    List<Polyline>mapLines;
    
    Database db;


    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.showlocation_frg, container, false);

        textSpeed = (TextView) v.findViewById(R.id.textSpeed);
        textSpeedAvg = (TextView) v.findViewById(R.id.textSpeedAvg);
        textDistance = (TextView) v.findViewById(R.id.textDistance);
        textTimer = (TextView) v.findViewById(R.id.textTimer);
        textProvider = (TextView) v.findViewById(R.id.textProvider);
        viewFlipper = (ViewFlipper) v.findViewById(R.id.viewFlipper);
        map = (Button) v.findViewById(R.id.buttonMap);
        info = (Button) v.findViewById(R.id.buttonInfo);
        start = (Button) v.findViewById(R.id.buttonStart);
        clear = (Button) v.findViewById(R.id.buttonClear);
        save = (Button) v.findViewById(R.id.buttonSave);

        db = new Database(getActivity().getBaseContext());

        mapLines = new ArrayList<Polyline>();



        setListeners();

        selectProvider();



        if (provider==null){


            return v;
        }

        lastLocation = getLastLocation();// locationManager.getLastKnownLocation(provider);

        textProvider.setText("Currently using: "+provider);

//        providerField.setText(provider);

        // Initialize the location fields
        if (lastLocation != null) {

            initializeMap();


        } else {
            locationManager.requestLocationUpdates(provider, 500, 0, this);
//            latituteField.setText("Location not available");
//            longitudeField.setText("Location not available");
        }

        return  v;
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

            }else{
                locationManager.requestLocationUpdates(provider, 500, 0, this);
            }
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
            locationManager.requestLocationUpdates(provider, 500, 0, this);
            start.setText("Stop");


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
            start.setText("Start");

            mHandler.removeCallbacks(mUpdateTimeTask);

            textTimer.setText(timerStop1);
            mStartTime = 0L;

        }

    }

    public void setListeners(){

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               saveRun(totalDistance, totalTime);
            }
        });




        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewFlipper.getDisplayedChild()==1) viewFlipper.showPrevious();
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewFlipper.getDisplayedChild()==0) viewFlipper.showNext();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!running) {

                    if (provider!=null)
                        getUpdates(true);
                    else
                        selectProvider();


                }else{
                    getUpdates(false);

                }

            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (googleMap!=null)
                    googleMap.clear();
            }
        });


    }

    public void saveRun(float totalDistance, long totalTime){



        if (!running){


            Date now = new Date();
            Running tr = new Running(-1, "I am running",
                    textTimer.getText().toString(),
                    now.toString(), textDistance.getText().toString());

            db.addRunning(tr);

        }else{
            Toast.makeText(getActivity(), "Still running!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    public void initializeMap(){
        SupportMapFragment fm = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapKostas);
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
            textSpeed.setText("Speed: " + String.format("%1$,.2f", ((location.getSpeed() * 3600) / 1000)));

            textSpeedAvg.setText("Avg Speed: " + String.format("%1$,.2f", (double) (totalDistance) / (double) (totalTime / (3600))));

            textDistance.setText("Distance: " + String.format("%1$,.2f", (double) (totalDistance / 1000)));
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
        textProvider.setText("Enabled new provider " + provider);

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

            textTimer.setText("" + minutes + ":"
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

}

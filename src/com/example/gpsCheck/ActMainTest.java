package com.example.gpsCheck;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.test.mock.MockApplication;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.example.gpsCheck.dbObjects.Running;
import com.example.gpsCheck.service.RunningService;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by KLiakopoulos on 4/6/2015.
 */

public class ActMainTest extends FragmentActivity {

    private ViewPager mPager;

    static final int DEFAULT_PAGER_POSITION = 1;
    static final int MY_RUNS_PAGER_POSITION = 2;
    static final int PAGER_SIZE = 3;//4;
    private static ActMainTest instace;
    private String[] pagerTitles;
    Map<Integer, Integer> bottomButtons;
    private SharedPreferences app_preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act_main);

        checkOfflineActions();

        getPager();

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String string = bundle.getString(RunningService.LATLONLIST);
                int resultCode = bundle.getInt(RunningService.RESULT);

            }
        }
    };

    private void checkOfflineActions(){

        if (isNetworkAvailable()) {
            ExtApplication application = (ExtApplication) getApplication().getApplicationContext();
            app_preferences = PreferenceManager.getDefaultSharedPreferences(application);
            SharedPreferences.Editor editor = app_preferences.edit();

            String offline = app_preferences.getString("offline", "");

            if (!offline.equals("")) {

                String[] offlineActs = offline.split("/");

                for (String act : offlineActs) {
                    String[] act2 = act.split(" ");
                    if (act2[1].equals("true")) {
                        new uploadOfflineChallenge(this, act2[0].trim(),true,0,0,null,null).execute();
                    } else if (act2[1].equals("false")) {
                        new uploadOfflineChallenge(this, act2[0].trim(),false,0,0,null,null).execute();
                    }

                    offline = offline.replace(act+"/", "");
                    editor.putString("offline", offline);

                }


            }
            offline = app_preferences.getString("offlineCreate", "");

            if (!offline.equals("")) {

                String[] offlineActs = offline.split("/");

                for (String act : offlineActs) {
                    String[] act2 = act.split(" ");

                        new uploadOfflineChallenge(this, act2[0].trim(),true,Long.valueOf(act2[1]),Float.valueOf(act2[2]),act2[3],act2[4]).execute();


                    offline = offline.replace(act+"/", "");
                    editor.putString("offlineCreate", offline);

                }


            }

            editor.commit();
        }

    }

    private void setDefaultPagerItem(){
        ExtApplication application = (ExtApplication) getApplication().getApplicationContext();
        app_preferences = PreferenceManager.getDefaultSharedPreferences(application);
        int position;

        if (app_preferences.getString("mongoId",null)==null){
            position=0;
        }else{

            Toast.makeText(this, app_preferences.getString("mongoId",null),
                    Toast.LENGTH_SHORT).show();
            position=1;
        }

        mPager.setCurrentItem(position);
        setSelectedBottomButton(bottomButtons,position);
    }

    public void refreshShowLocationUsernames(){


        final String name = "android:switcher:" + mPager.getId()+ ":" + 1;
        final Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(name);


        ((FrgShowLocation) fragmentByTag).refreshUsernames();

            }


    private void getPager() {
        mPager = (ViewPager) findViewById(R.id.pager);

        mPager.setOffscreenPageLimit(3);

        mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        setBottomButtons(mPager);

        pagerTitles = getResources().getStringArray(R.array.pager_titles);

        setDefaultPagerItem();

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

//                mPager.setCurrentItem(i);
                setSelectedBottomButton(bottomButtons,i);

            }

            @Override
            public void onPageSelected(int position) {

                mPager.setCurrentItem(position);
                setSelectedBottomButton(bottomButtons,position);

                //todo not on every change, set timer!!!
                if (getActiveFragment(getSupportFragmentManager(), position) instanceof FrgShowProfile) {
                    ((FrgShowProfile) getActiveFragment(getSupportFragmentManager(), 0)).startAsyncGetOrInsert(1);
                }else if (getActiveFragment(getSupportFragmentManager(), position) instanceof FrgShowChallenge){

                    ((FrgShowChallenge)getActiveFragment(getSupportFragmentManager(), position)).getLeaderBoardAndChallenges();
                    ((FrgShowChallenge)getActiveFragment(getSupportFragmentManager(), position)).refreshRequests();
                }else if (getActiveFragment(getSupportFragmentManager(), position) instanceof FrgShowLocation) {
                    ((FrgShowLocation)getActiveFragment(getSupportFragmentManager(), position)).refreshUsernames();

                }


                    invalidateOptionsMenu();
            }

            public Fragment getActiveFragment(FragmentManager fragmentManager, int position) {
                final String name = makeFragmentName(mPager.getId(), position);
                final Fragment fragmentByTag = fragmentManager.findFragmentByTag(name);
                if (fragmentByTag == null) {
                    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    fragmentManager.dump("", null, new PrintWriter(outputStream, true), null);
                }
                return fragmentByTag;
            }

            private String makeFragmentName(int viewId, int index) {
                return "android:switcher:" + viewId + ":" + index;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    public void respondToChal(Running run, int type){

        //todo copy to refresh usernames!!!

            final String name = "android:switcher:" + mPager.getId()+ ":" + 1;
            final Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(name);


       ((FrgShowLocation) fragmentByTag).beginChallenge(run, type);

//        ((FrgShowLocation) fragmentByTag).changeSaveListener(run);

        getmPager().setCurrentItem(1);
    }


    // todo onPause and onResume I should find the last fragment
//    private void setSelectedPage() {
//        int position = application.getPosition();
//        mPager.setCurrentItem(position);
//        setTitle(pagerTitles[position]);
//        setSelectedBottomButton(bottomButtons, position);
//    }


    protected void setBottomButtons(ViewPager mPager) {
        initBottomButtons();
        for (int counter = 0; counter < PAGER_SIZE; counter++) {
            setBottomButtonListener(mPager, bottomButtons.get(counter), counter);
        }
    }

    private void initBottomButtons() {
        bottomButtons = new HashMap<Integer, Integer>();
        bottomButtons.put(0, R.id.btn_my_profile);
        bottomButtons.put(1, R.id.btn_new_run);
//        bottomButtons.put(2, R.id.btn_my_runs);
        bottomButtons.put(2, R.id.btn_my_chal);

    }

    private void setSelectedBottomButton(Map<Integer, Integer> bottomButtons, int postion) {
        for (int key = 0; key < bottomButtons.size(); key++) {
            LinearLayout btn = (LinearLayout) findViewById(bottomButtons.get(key));
            btn.setSelected(key != postion ? false : true);
        }
    }

    private void setBottomButtonListener(final ViewPager mPager, int btn, final int position) {
        LinearLayout bottomButton = (LinearLayout) findViewById(btn);


            bottomButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    startMain(mPager, position);


                }
            });

    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

//        Fragment[] fragments;

        public MyPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
//            fragments = new Fragment[pageCount];
//            for (int i = 0; i < fragments.length; i++)
//                fragments[i] = null;
        }

        @Override
        public int getCount() {
            return PAGER_SIZE;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                   return FrgShowProfile.init(position);

                }
                case 1: {
                   return FrgShowLocation.init(position);
                }
//                case 2: {
//                    return FrgShowRuns.init(position);
//                }
                case 2: {
                    return FrgShowChallenge.init(position);
                }
                default: return FrgShowRuns.init(position);


            }
        }

    }


    private void startMain(ViewPager mPager, int position) {
        if (null != mPager) {
            mPager.setCurrentItem(position);
        } else {
            startMainWhenNoPager(position);
        }
    }

    private void startMainWhenNoPager(int position) {
        ((ExtApplication)getApplication()).setPosition(position);
        Intent intent = new Intent(this, ActMainTest.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public ViewPager getmPager() {
        return mPager;
    }


    private class uploadOfflineChallenge extends AsyncTask<Void, Void, Integer> {
        private Activity activity;
        private String username;
        boolean won;
        long time;
        float distance;
        String desc;
        String latLonList;

        public uploadOfflineChallenge(Activity activity, String username, boolean won, long time, float distance, String desc,String latLonList) {
            this.activity = activity;
            this.username = username;
            this.won = won;
            this.time=time;
            this.distance=distance;
            this.desc=desc;
            this.latLonList=latLonList;
        }

        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(Void... unused) {
            SyncHelper sh = new SyncHelper(activity);

            if (desc==null)
                sh.replyToChallenge(username, won);
            else
                sh.createMongoChallenge(username, time, distance, latLonList, desc);

            sh.updateTimeAndDistance(app_preferences.getString("username",""));


            return 0;

        }

        @Override
        protected void onPostExecute(Integer result) {

            Toast.makeText(getApplication(), "Uploaded challenge to "+username, Toast.LENGTH_LONG).show();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();


//        if (((ExtApplication) getApplication()).isRunning()) {
//
//
//            NotificationManager mNotifyMgr =
//                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            //Builds the notification and issues it.
//            mNotifyMgr.cancel(001);
//
//        }
//        stopService(getIntent());



    }

    @Override
    protected void onPause() {


//        if (((ExtApplication) getApplication()).isRunning()) {
//
//            Intent resultIntent = new Intent(this, ActMainTest.class);
//            resultIntent.putExtra("latLonList", ((ExtApplication) getApplication()).getLatLonList());
//            //    Because clicking the notification opens a new ("special") activity, there's
//            //    no need to create an artificial back stack.
//            PendingIntent resultPendingIntent =
//                    PendingIntent.getActivity(
//                            this,
//                            0,
//                            resultIntent,
//                            PendingIntent.FLAG_UPDATE_CURRENT
//                    );
//
//            ((ExtApplication) getApplication()).setmBuilder(
//                    new NotificationCompat.Builder(this)
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
//                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            //Builds the notification and issues it.
//            mNotifyMgr.notify(mNotificationId, ((ExtApplication) getApplication()).getmBuilder().build());
//
//
//            Intent intent = new Intent(this, RunningService.class);
//            // add info for the service which file to download and where to store
//            intent.putExtra(RunningService.LATLONLIST, ((ExtApplication) getApplication()).getLatLonList());
//        startService(intent);

//        registerReceiver(receiver, new IntentFilter(RunningService.FILENAME));
//        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }



    @Override
    public void onBackPressed() {


        super.onBackPressed();
    }

    public void togglePagerClickable(boolean show){

//        for (int counter = 0; counter < PAGER_SIZE; counter++) {
//
//                setBottomButtonListener(mPager, bottomButtons.get(counter), counter, disable);
//
//        }

        LinearLayout bottom = (LinearLayout) findViewById(R.id.bottom);

        if (show)  bottom.setVisibility(View.VISIBLE);
        else bottom.setVisibility(View.GONE);

    }



}
package com.example.gpsCheck;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.example.gpsCheck.dbObjects.Running;

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

    private void checkOfflineActions(){

        if (isNetworkAvailable()) {
            ExtApplication application = (ExtApplication) getApplication().getApplicationContext();
            app_preferences = PreferenceManager.getDefaultSharedPreferences(application);
            String offline = app_preferences.getString("offline", "");
            if (!offline.equals("")) {

                String[] offlineActs = offline.split("/");

                for (String act : offlineActs) {
                    String[] act2 = act.split(" ");
                    if (act2[1].equals("true")) {
                        new uploadReplyChallenge(this, act2[0].trim(),true).execute();
                    } else if (act2[1].equals("false")) {
                        new uploadReplyChallenge(this, act2[0].trim(),false).execute();
                    }

                    offline = offline.replace(act+"/", "");
                    SharedPreferences.Editor editor = app_preferences.edit();
                    editor.putString("offline", offline);
                    editor.commit();
                }


            }
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
                ((FrgShowProfile)getActiveFragment(getSupportFragmentManager(), 0)).startAsyncGetOrInsert(1);


                if (getActiveFragment(getSupportFragmentManager(), position) instanceof FrgShowChallenge){

                    ((FrgShowChallenge)getActiveFragment(getSupportFragmentManager(), position)).getLeaderBoardAndChallenges();
                    ((FrgShowChallenge)getActiveFragment(getSupportFragmentManager(), position)).refreshRequests();


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

    public void respondToChal(Running run){

            final String name = "android:switcher:" + mPager.getId()+ ":" + 1;
            final Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(name);


       ((FrgShowLocation) fragmentByTag).beginChallenge(run);

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


    private class uploadReplyChallenge extends AsyncTask<Void, Void, Integer> {
        private Activity activity;
        private String username;
        boolean won;

        public uploadReplyChallenge(Activity activity, String username, boolean won) {
            this.activity = activity;
            this.username = username;
            this.won = won;
        }

        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(Void... unused) {
            SyncHelper sh = new SyncHelper(activity);

            sh.replyToChallenge(username, won);

            return 0;

        }

        @Override
        protected void onPostExecute(Integer result) {

        }


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

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
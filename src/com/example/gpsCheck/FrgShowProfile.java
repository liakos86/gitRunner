package com.example.gpsCheck;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

//fixme commit this

/**
 * Created by liakos on 11/4/2015.
 */
public class FrgShowProfile  extends BaseFragment {

    private enum userTypes{
        INSERT_NEW(0), GET_BY_ID(1), GET_BY_EMAIL(2);

        private int value;

        public int getValue() {
            return value;
        }

        private userTypes(int value){
            this.value = value;
        }

    };


    Button buttonRegister, buttonLogin;
    EditText editUsername, editPassword, editEmail, editExistingEmail, editExistingPassword;
    TextView textLogin;
    ViewFlipper vs;
    String username, password, email;
    TextView textTotalChallenges, textTotalScore, textTotalTime, textTotalDistance, textUser;
    SyncHelper sh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.showprofile_frg, container, false);

//        AdView adView = (AdView) v.findViewById(R.id.adView);
//        adView.setAdSize(AdSize.BANNER);
//        adView.setAdUnitId("ca-app-pub-1164456313108704/6840469075");
//        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//                .build();
//        adView.loadAd(adRequest);

        sh = new SyncHelper(getActivity());


        setViewsAndListeners(v);



        setViewFlipper(v);

        setTextValues(null);





        return  v;
    }


    private void setViewFlipper(View v){
        vs = (ViewFlipper) v.findViewById(R.id.FrgProfileViewSwitcher);
        ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);

        // if mongoID exists, get user and show child 0 / else show child 2 (register or login)
        if (app_preferences.getString("mongoId",null)==null){
            ((ActMainTest)getActivity()).togglePagerClickable(false);
            vs.setDisplayedChild(2);
        }else{
            startAsyncGetOrInsert(userTypes.GET_BY_ID.getValue());
            vs.setDisplayedChild(0);
        }

    }

    private void setViewsAndListeners(View v){

        textTotalChallenges = (TextView) v.findViewById(R.id.textTotalChallenges);
        textTotalScore = (TextView) v.findViewById(R.id.textTotalScore);
        textTotalTime = (TextView) v.findViewById(R.id.textTotalTime);
        textTotalDistance = (TextView) v.findViewById(R.id.textTotalDistance);
        textUser = (TextView) v.findViewById(R.id.textUser);

        buttonRegister = (Button) v.findViewById(R.id.buttonRegister);
        buttonLogin = (Button) v.findViewById(R.id.buttonLogin);
        textLogin = (TextView) v.findViewById(R.id.textLogin);

        editUsername = (EditText) v.findViewById(R.id.inputUsername);
        editPassword = (EditText) v.findViewById(R.id.inputPassword);
        editEmail = (EditText) v.findViewById(R.id.inputEmail);

        editExistingEmail = (EditText) v.findViewById(R.id.inputExistingEmail);
        editExistingPassword = (EditText) v.findViewById(R.id.inputExistingPassword);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAsyncGetOrInsert(userTypes.INSERT_NEW.getValue());
            }
        });

        textLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vs.setDisplayedChild(1);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAsyncGetOrInsert(userTypes.GET_BY_EMAIL.getValue());
            }
        });



    }

    private void setTextValues(ExtApplication app){



        ExtApplication application;
        if (app==null)
             application = (ExtApplication) getActivity().getApplication().getApplicationContext();
        else
            application = app;

        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);



        textTotalChallenges.setText("Challenges (won/played): "+app_preferences.getInt("wonChallenges",0)+" / "+app_preferences.getInt("totalChallenges", 0));
        textTotalScore.setText("Total Score: "+app_preferences.getInt("totalScore",0));
        textTotalTime.setText("Total Time: "+app_preferences.getLong("totalTime",0));
        textTotalDistance.setText("Total Distance: "+app_preferences.getFloat("totalDistance",0));
        textUser.setText(app_preferences.getString("username","User"));




    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void startAsyncGetOrInsert(int type){

        if (type==userTypes.INSERT_NEW.getValue()){
            username = editUsername.getText().toString().trim();
            password = editPassword.getText().toString().trim();
            email = editEmail.getText().toString().trim();
            if (username.length()==0||password.length()==0||email.length()==0){
                Toast.makeText(getActivity(), "Please fill in all fields!", Toast.LENGTH_LONG).show();
                return;
            }
            if (!isValidEmail(email)){
                Toast.makeText(getActivity(), "Please fill in a valid email!", Toast.LENGTH_LONG).show();
                return;
            }
        }else if (type==userTypes.GET_BY_EMAIL.getValue()){
            password = editExistingPassword.getText().toString().trim();
            email = editExistingEmail.getText().toString().trim();
            if (password.length()==0||email.length()==0){
                Toast.makeText(getActivity(), "Please fill in all fields!", Toast.LENGTH_LONG).show();
                return;
            }
            if (!isValidEmail(email)){
                Toast.makeText(getActivity(), "Please fill in a valid email!", Toast.LENGTH_LONG).show();
                return;
            }
        }

        //if type==1 we get uer by shared prefs id
        if (((ActMainTest)getActivity()).isNetworkAvailable()) {

            new insertOrGetUser((ExtApplication)getActivity().getApplication(), type).execute();
        }
    }

    static FrgShowProfile init(int val) {
        FrgShowProfile truitonList = new FrgShowProfile();

        // Supply val input as an argument.
//        Intent itwn = new Intent(ActMainTest.class, ActMainTest.class);

        Bundle args = new Bundle();
        args.putInt("val", val);
        truitonList.setArguments(args);

        return truitonList;
    }

    private class insertOrGetUser extends AsyncTask<Void, Void, Integer> {
        private ExtApplication app;
        private int type;

        public insertOrGetUser(ExtApplication app, int type) {
            this.app = app;
            this.type = type;
        }

        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(Void... unused) {


            switch (type){
                case 0 :
                    return sh.getMongoUser(email,username,password);
                case 1:
                    return sh.getMongoUser(null, null, null);
                case 2:
                    return sh.getMongoUser(email, null, password);
                default:
                    return sh.getMongoUser(null, null, null);
            }


        }

        @Override
        protected void onPostExecute(Integer result) {

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

            if (result==0){
                setTextValues(app);
                ((ActMainTest) getActivity()).refreshShowLocationUsernames();

//                ((ActMainTest) getActivity()).getmPager().setCurrentItem(1);
            }else if (result==-1){
                Toast.makeText(getActivity(), "Invalid credentials", Toast.LENGTH_LONG).show();
            }else if (result==-2){
                Toast.makeText(getActivity(), "Server Error", Toast.LENGTH_LONG).show();
            }else if (result==2){
                ((ActMainTest)getActivity()).getFirstLeaderboard();
                Toast.makeText(getActivity(), "User found", Toast.LENGTH_LONG).show();
                setTextValues(app);
                vs.setDisplayedChild(0);
                ((ActMainTest)getActivity()).togglePagerClickable(true);

            }

        }


    }

}

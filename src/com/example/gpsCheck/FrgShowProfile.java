package com.example.gpsCheck;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;


/**
 * Created by liakos on 11/4/2015.
 */
public class FrgShowProfile  extends Fragment {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.showprofile_frg, container, false);


        setViewsAndListeners(v);



        setViewFlipper(v);



        return  v;
    }

    private void setViewFlipper(View v){
        vs = (ViewFlipper) v.findViewById(R.id.FrgProfileViewSwitcher);
        ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);

        // if mongoID exists, get user and show child 0 / else show child 2 (register or login)
        if (app_preferences.getString("mongoId",null)==null){
            vs.setDisplayedChild(2);
        }else{
            startAsyncGetOrInsert(userTypes.GET_BY_ID.getValue());
            vs.setDisplayedChild(0);
        }

    }

    private void setViewsAndListeners(View v){

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

    private void setTextValues(){

        TextView textTotalChallenges = (TextView) getView().findViewById(R.id.textTotalChallenges);

        ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();

        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);

        textTotalChallenges.setText("Total Challenges: "+app_preferences.getInt("totalChallenges",0));


    }




    public void startAsyncGetOrInsert(int type){

        if (type==userTypes.INSERT_NEW.getValue()){
            username = editUsername.getText().toString();
            password = editPassword.getText().toString();
            email = editEmail.getText().toString();
            if (username.length()==0||password.length()==0||email.length()==0){
                Toast.makeText(getActivity(), "Please fill in all fields!", Toast.LENGTH_SHORT).show();
                return;
            }
        }else if (type==userTypes.GET_BY_EMAIL.getValue()){
            password = editExistingPassword.getText().toString();
            email = editExistingEmail.getText().toString();
            if (password.length()==0||email.length()==0){
                Toast.makeText(getActivity(), "Please fill in all fields!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        //if type==1 we get uer by shared prefs id

        new insertOrGetUser(getActivity(),type).execute();
    }

    static FrgShowProfile init(int val) {
        FrgShowProfile truitonList = new FrgShowProfile();

        // Supply val input as an argument.
        Bundle args = new Bundle();
        args.putInt("val", val);
        truitonList.setArguments(args);

        return truitonList;
    }


    private class insertOrGetUser extends AsyncTask<Void, Void, Integer> {
        private Activity activity;
        private int type;

        public insertOrGetUser(Activity activity, int type) {
            this.activity = activity;
            this.type = type;
        }

        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(Void... unused) {
            SyncHelper sh = new SyncHelper(activity);

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

            if (result==0){
                setTextValues();
                ((ActMainTest)getActivity()).getmPager().setCurrentItem(1);
            }else if (result==-1){
                Toast.makeText(getActivity(), "Invalid credentials", Toast.LENGTH_LONG).show();
            }else if (result==-2){
                Toast.makeText(getActivity(), "Server Error", Toast.LENGTH_LONG).show();
            }else if (result==2){
                Toast.makeText(getActivity(), "User found", Toast.LENGTH_LONG).show();
                setTextValues();
                vs.setDisplayedChild(0);
            }

        }


    }

}

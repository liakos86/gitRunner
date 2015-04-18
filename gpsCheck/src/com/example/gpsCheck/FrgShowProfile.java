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
import android.widget.Button;
import android.widget.ViewFlipper;


/**
 * Created by liakos on 11/4/2015.
 */
public class FrgShowProfile  extends Fragment {

    Button buttonRegister;
    ViewFlipper vs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.showprofile_frg, container, false);

        buttonRegister = (Button) v.findViewById(R.id.buttonRegister);

        setView(v);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startAsyncInsert();
            }
        });



        return  v;
    }

    private void setView(View v){
        vs = (ViewFlipper) v.findViewById(R.id.FrgProfileViewSwitcher);
        ExtApplication application = (ExtApplication) getActivity().getApplication().getApplicationContext();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(application);
        if (app_preferences.getString("mongoId",null)==null){
            vs.setDisplayedChild(2);
        }else{
            vs.setDisplayedChild(0);
        }

    }

    public void startAsyncInsert(){
        new insertUser(getActivity()).execute();
    }

    static FrgShowProfile init(int val) {
        FrgShowProfile truitonList = new FrgShowProfile();

        // Supply val input as an argument.
        Bundle args = new Bundle();
        args.putInt("val", val);
        truitonList.setArguments(args);

        return truitonList;
    }


    private class insertUser extends AsyncTask<Void, Void, Integer> {
        private Activity activity;

        public insertUser(Activity activity) {
            this.activity = activity;
        }

        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(Void... unused) {
            SyncHelper sh = new SyncHelper(activity);
            return sh.insertMongoUser();

        }

        @Override
        protected void onPostExecute(Integer result) {

            if (result==0){

                ((ActMainTest)getActivity()).getmPager().setCurrentItem(1);

            }

        }


    }

}

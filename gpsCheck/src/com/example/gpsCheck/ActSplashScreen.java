package com.example.gpsCheck;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import com.example.gpsCheck.dbObjects.Running;
import com.example.gpsCheck.model.ContentDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liakos on 11/4/2015.
 */
public class ActSplashScreen extends Activity{



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act_splash);

        new fetchData(this).execute();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ActSplashScreen.this, ActMainTest.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                finish();
            }
        }, 3000);

    }



    private class fetchData extends AsyncTask<Void, Void, Integer> {
        private Activity activity;

        public fetchData(Activity activity) {
            this.activity = activity;
        }

        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(Void... unused) {
            SyncHelper sh = new SyncHelper(activity);
            int ll_rows = sh.FetchWorkoutsJSON();

            return ll_rows;
        }

        @Override
        protected void onPostExecute(Integer result) {

        }


    }

}

package com.example.gpsCheck;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by liakos on 11/4/2015.
 */
public class ActSplashScreen extends Activity{



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act_splash);

        new fetchRunners(this).execute();


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



    private class fetchRunners extends AsyncTask<Void, Void, Integer> {
        private Activity activity;

        public fetchRunners(Activity activity) {
            this.activity = activity;
        }

        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(Void... unused) {
            SyncHelper sh = new SyncHelper(activity);
            int ll_rows =0;// sh.FetchRunnersJSON();

            return ll_rows;
        }

        @Override
        protected void onPostExecute(Integer result) {

        }


    }

}

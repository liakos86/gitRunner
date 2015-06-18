
package com.example.gpsCheck.model;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;
import android.util.Log;

import com.example.gpsCheck.dbObjects.Running;
import com.example.gpsCheck.dbObjects.User;

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "my_runner.db";
    private static final int DATABASE_VERSION = 1;
    // this is also considered as invalid id by the server
    public static final long INVALID_ID = -1;
    private Context mContext;

    public Database(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(ContentDescriptor.Running.createTable());
        db.execSQL(ContentDescriptor.User.createTable());


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("Database", "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
            db.execSQL("drop table if exists " + ContentDescriptor.Running.TABLE_NAME);

            onCreate(db); // run onCreate to get new database
    }
    
    
    public void addRunning(Running running) {
        ContentResolver resolver = mContext.getContentResolver();
        resolver.insert(ContentDescriptor.Running.CONTENT_URI, Running.asContentValues(running));
    }

    public void addLeader(User leader) {
        ContentResolver resolver = mContext.getContentResolver();
        resolver.insert(ContentDescriptor.User.CONTENT_URI, User.asContentValues(leader));
    }



    public void deleteRunning(Long id){
        ContentResolver resolver = mContext.getContentResolver();
        resolver.delete(ContentDescriptor.Running.CONTENT_URI, ContentDescriptor.Running.Cols.ID + "=" + String.valueOf(id), null);
    }

    public void deleteChallenge(String id){
        ContentResolver resolver = mContext.getContentResolver();
        resolver.delete(ContentDescriptor.Running.CONTENT_URI, ContentDescriptor.Running.Cols.MONGO_ID + "= '" + id+"'", null);
    }

    public void deleteAllOpenChallenges(){
        ContentResolver resolver = mContext.getContentResolver();
        resolver.delete(ContentDescriptor.Running.CONTENT_URI, ContentDescriptor.Running.Cols.TYPE + "= 1 AND "+ContentDescriptor.Running.Cols.STATUS + "= 0", null);
    }

    public void deleteLeaderboard(){
        ContentResolver resolver = mContext.getContentResolver();
        resolver.delete(ContentDescriptor.User.CONTENT_URI, null, null);
    }

    public void setDeletedFlag(String mongoId){
        ContentResolver resolver = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(ContentDescriptor.Running.Cols.DELETED, "1");
        resolver.update(ContentDescriptor.Running.CONTENT_URI, values, ContentDescriptor.Running.Cols.MONGO_ID + " = '"+mongoId+"'", null);

    }

    public int countRuns(){
        String[] proj = {ContentDescriptor.Running.Cols.ID};
        Cursor c = mContext.getContentResolver().query(ContentDescriptor.Running.CONTENT_URI, proj, null, null, null);
        int toRet = c.getCount();
        c.close();
        c=null;
        return toRet;
    }

    public List<Running> fetchClosedRunsFromDb() {



        String[] FROM = {
                // ! beware. I mark the position of the fields
                ContentDescriptor.Running.Cols.DESCRIPTION,
                ContentDescriptor.Running.Cols.DATE,
                ContentDescriptor.Running.Cols.ID,
                ContentDescriptor.Running.Cols.TIME,
                ContentDescriptor.Running.Cols.DISTANCE,
                ContentDescriptor.Running.Cols.TYPE,
                ContentDescriptor.Running.Cols.OPPONENT_NAME,
                ContentDescriptor.Running.Cols.USER_NAME,
                ContentDescriptor.Running.Cols.LAT_LON_LIST,
                ContentDescriptor.Running.Cols.WINNER,
                ContentDescriptor.Running.Cols.STATUS,
                ContentDescriptor.Running.Cols.MONGO_ID,
                ContentDescriptor.Running.Cols.DELETED



        };
        int sDescPosition = 0;
        int sDatePosition = 1;
        int sIdPosition = 2;
        int sTimePosition = 3;
        int sDistPosition = 4;
        int sTypePosition = 5;
        int sOppNamePosition = 6;
        int sUserNamePosition = 7;
        int sLatLonListPosition = 8;
        int sWinnerPosition = 9;
        int sStatusPosition = 10;
        int sMongoIdPosition = 11;
        int sDeletedPosition =12;


        Cursor c = mContext.getContentResolver().query(ContentDescriptor.Running.CONTENT_URI, FROM,
                ContentDescriptor.Running.Cols.TYPE+" = 1 AND "+ContentDescriptor.Running.Cols.STATUS+" = 1",
                null, null);

        List<Running> St = new ArrayList<Running>();

        if (c.getCount() > 0) {

            while (c.moveToNext()) {



                St.add(new Running(c.getLong(sIdPosition), c
                        .getString(sDescPosition), c.getLong(sTimePosition),
                        c.getString(sDatePosition),  c.getFloat(sDistPosition),
                        c.getInt(sTypePosition), c.getString(sOppNamePosition), c.getString(sUserNamePosition),  c.getString(sLatLonListPosition),
                        c.getString(sWinnerPosition), c.getInt(sStatusPosition), c.getString(sMongoIdPosition), c.getInt(sDeletedPosition)));
            }
        }
        c.close();
        c = null;

        return St;

    }


    public List<Running> fetchRunsByTypeFromDb( int type) {



        String[] FROM = {
                // ! beware. I mark the position of the fields
                ContentDescriptor.Running.Cols.DESCRIPTION,
                ContentDescriptor.Running.Cols.DATE,
                ContentDescriptor.Running.Cols.ID,
                ContentDescriptor.Running.Cols.TIME,
                ContentDescriptor.Running.Cols.DISTANCE,
                ContentDescriptor.Running.Cols.TYPE,
                ContentDescriptor.Running.Cols.OPPONENT_NAME,
                ContentDescriptor.Running.Cols.USER_NAME,
                ContentDescriptor.Running.Cols.LAT_LON_LIST,
                ContentDescriptor.Running.Cols.WINNER,
                ContentDescriptor.Running.Cols.STATUS,
                ContentDescriptor.Running.Cols.MONGO_ID,
                ContentDescriptor.Running.Cols.DELETED



        };
        int sDescPosition = 0;
        int sDatePosition = 1;
        int sIdPosition = 2;
        int sTimePosition = 3;
        int sDistPosition = 4;
        int sTypePosition = 5;
        int sOppNamePosition = 6;
        int sUserNamePosition = 7;
        int sLatLonListPosition = 8;
        int sWinnerPosition = 9;
        int sStatusPosition = 10;
        int sMongoIdPosition = 11;
        int sDeletedPosition =12;

        Cursor c = mContext.getContentResolver().query(ContentDescriptor.Running.CONTENT_URI, FROM,
                ContentDescriptor.Running.Cols.TYPE+" = "+type,
                null, null);



        // Cursor c =
        // getContentResolver().query(ContentDescriptor.Running.CONTENT_URI,
        // proj,
        // ContentDescriptor.Running.Cols.PROFILE_ID+" = "+pr.getProfileId(),
        // null, ContentDescriptor.Running.Cols.ID+" DESC");

        List<Running> St = new ArrayList<Running>();

        if (c.getCount() > 0) {

            while (c.moveToNext()) {



                St.add(new Running(c.getLong(sIdPosition), c
                        .getString(sDescPosition), c.getLong(sTimePosition),
                        c.getString(sDatePosition),  c.getFloat(sDistPosition),
                        c.getInt(sTypePosition), c.getString(sOppNamePosition), c.getString(sUserNamePosition),  c.getString(sLatLonListPosition),
                        c.getString(sWinnerPosition), c.getInt(sStatusPosition), c.getString(sMongoIdPosition), c.getInt(sDeletedPosition)));
            }
        }
        c.close();
        c = null;

        return St;

    }


    public List<User> fetchLeadersFromDb() {



        String[] FROM = {
                // ! beware. I mark the position of the fields
                ContentDescriptor.User.Cols.ID,
                ContentDescriptor.User.Cols.USERNAME,
                ContentDescriptor.User.Cols.TOTAL_CHALLENGES,
                ContentDescriptor.User.Cols.WON_CHALLENGES,
                ContentDescriptor.User.Cols.TOTAL_SCORE
        };
        int sIdPosition = 0;
        int sUsernamePosition = 1;
        int sTotalChalPosition = 2;
        int sWonChalPosition = 3;
        int sTotalScorePosition = 4;


        Cursor c = mContext.getContentResolver().query(ContentDescriptor.User.CONTENT_URI, FROM,
            null,  null, null);


        List<User> St = new ArrayList<User>();

        if (c.getCount() > 0) {

            while (c.moveToNext()) {



                St.add(new User(c.getLong(sIdPosition), c.getString(sUsernamePosition), c.getInt(sTotalChalPosition),
                         c.getInt(sWonChalPosition),  c.getInt(sTotalScorePosition)));
            }
        }
        c.close();
        c = null;

        return St;

    }


}


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



    public void deleteRunning(Long id){
        ContentResolver resolver = mContext.getContentResolver();
        resolver.delete(ContentDescriptor.Running.CONTENT_URI, ContentDescriptor.Running.Cols.ID + "=" + String.valueOf(id), null);
    }


    public int countRuns(){
        String[] proj = {ContentDescriptor.Running.Cols.ID};
        Cursor c = mContext.getContentResolver().query(ContentDescriptor.Running.CONTENT_URI, proj, null, null, null);
        int toRet = c.getCount();
        c.close();
        c=null;
        return toRet;
    }


}

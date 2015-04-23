package com.example.gpsCheck.dbObjects;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.example.gpsCheck.model.ContentDescriptor;
import com.example.gpsCheck.model.Database;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liakos on 11/4/2015.
 */
public class Running {

    private static final String TAG = Thread.currentThread().getStackTrace()[2].getClassName();

    private String date;
    private float distance;
    private long running_id;
    private long time;
    private String description;
    private String opponent_id;
    private int type; // 0=simple run , 1=outgoing challenge run, 2=incoming challenge run
    private String LatLonList;


    public Running(){}

    public Running(long running_id, String description,long time, String date, float distance, int type, String opponent_id, String LatLonList){
        this.running_id = running_id;
        this.time = time;
        this.date = date;
        this.description = description;
        this.distance = distance;
        this.type = type;
        this.opponent_id = opponent_id;
        this.LatLonList = LatLonList;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getRunning_id() {
        return running_id;
    }

    public void setRunning_id(long running_id) {
        this.running_id = running_id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getOpponent_id() {
        return opponent_id;
    }

    public void setOpponent_id(String opponent_id) {
        this.opponent_id = opponent_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLatLonList() {
        return LatLonList;
    }

    public void setLatLonList(String latLonList) {
        LatLonList = latLonList;
    }

    public static Running getFromId(Context context, long id) {
        Log.v(TAG, String.format("Requesting item [%d]", id));
        synchronized (context) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(Uri.withAppendedPath(ContentDescriptor.Running.CONTENT_URI,
                                String.valueOf(id)), null, null, null, null);
                cursor.moveToFirst();
                return createFromCursor(cursor);
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
    }

    public static ContentValues asContentValues(Running item) {
        if (item == null)
            return null;
        synchronized (item) {
            ContentValues toRet = new ContentValues();

            toRet.put(ContentDescriptor.Running.Cols.ID, item.running_id);
            toRet.put(ContentDescriptor.Running.Cols.DATE, item.date);
            toRet.put(ContentDescriptor.Running.Cols.DESCRIPTION, item.description);
            toRet.put(ContentDescriptor.Running.Cols.TIME, item.time);
            toRet.put(ContentDescriptor.Running.Cols.DISTANCE, item.distance);
            toRet.put(ContentDescriptor.Running.Cols.TYPE, item.type);
            toRet.put(ContentDescriptor.Running.Cols.OPPONENT_ID, item.opponent_id);
            toRet.put(ContentDescriptor.Running.Cols.LAT_LON_LIST, item.LatLonList);


            return toRet;
        }
    }

    public static Running createFromCursor(Cursor cursor) {
        synchronized (cursor) {
            if (cursor.isClosed() || cursor.isAfterLast() || cursor.isBeforeFirst()) {
                Log.v(TAG, String.format("Requesting entity but no valid cursor"));
                return null;
            }
            Running toRet = new Running();
            toRet.running_id = cursor.getLong(cursor.getColumnIndex(ContentDescriptor.Running.Cols.ID));
            toRet.date = cursor.getString(cursor.getColumnIndex(ContentDescriptor.Running.Cols.DATE));
            toRet.description = cursor.getString(cursor.getColumnIndex(ContentDescriptor.Running.Cols.DESCRIPTION));
            toRet.time = cursor.getLong(cursor.getColumnIndex(ContentDescriptor.Running.Cols.TIME));
            toRet.distance = cursor.getFloat(cursor.getColumnIndex(ContentDescriptor.Running.Cols.DISTANCE));
            toRet.type = cursor.getInt(cursor.getColumnIndex(ContentDescriptor.Running.Cols.TYPE));
            toRet.opponent_id = cursor.getString(cursor.getColumnIndex(ContentDescriptor.Running.Cols.OPPONENT_ID));


            return toRet;
        }
    }

    /**
     * let the id decide if we have an insert or an update
     *
     * @param resolver
     * @param item
     */
    public static void save(ContentResolver resolver, Running item) {
        if (item.running_id == Database.INVALID_ID)
            resolver.insert(ContentDescriptor.Running.CONTENT_URI, Running.asContentValues(item));
        else
            resolver.update(ContentDescriptor.Running.CONTENT_URI, Running.asContentValues(item),
                    String.format("%s=?", ContentDescriptor.Running.Cols.ID),
                    new String[]{
                            String.valueOf(item.running_id)
                    });
    }




}

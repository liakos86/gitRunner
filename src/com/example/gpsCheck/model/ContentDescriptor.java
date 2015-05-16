
package com.example.gpsCheck.model;

import com.example.gpsCheck.dbObjects.Running;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * based on http://www.nofluffjuststuff.com/blog/vladimir_vivien/2011/11/
 * a_pattern_for_creating_custom_android_content_providers <br/>
 * workaround (wa1): We don't use {@link #applyBatch(java.util.ArrayList)}for
 * number matching (see
 * http://code.google.com/p/android/issues/detail?mId=27031). We use * and take
 * care of it in code.<br/>
 *
 * @author kliakopoulos
 */
public class ContentDescriptor {

    public static final String AUTHORITY = "com.example.gpscheck.contentprovider";
    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
    public static final UriMatcher URI_MATCHER = buildUriMatcher();

    public static final String PARAM_FULL = "full";
    public static final String PARAM_SEARCH = "search";
    public static final String PARAM_COUNT = "count";
    

    // argument passed via query params, start with this string
    // in other words: query params starting with this string are arguments
    public static final String ARG_PREFIX = "arg_";
    // helper format strings
    private static final String sFormatArg = ARG_PREFIX + "%s";
    // helper format strings for table creation
    private static final String sFrmIdAutoinc = " %s INTEGER PRIMARY KEY AUTOINCREMENT ";
    private static final String sFrmId = " %s INTEGER PRIMARY KEY ";
    private static final String sFrmInt = " %s INTEGER ";
    private static final String sFrmText = " %s TEXT ";
    private static final String sFrmTextNotNull = " %s TEXT NOT NULL ";
    private static final String sFrmFloat = " %s FLOAT ";
    private static final String sFrmPrimaryKey = " UNIQUE (%s) ON CONFLICT REPLACE ";

    private ContentDescriptor() {
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AUTHORITY;

       
        Running.addToUriMatcher(authority, matcher);
        User.addToUriMatcher(authority, matcher);
        
     

        return matcher;
    }

    

    public static class Running {
        public static final String TABLE_NAME = "running";
        // content://xxxxx/running
        public static final String PATH = "running";
        public static final int PATH_TOKEN = 10;
        // content://xxxxx/running/20
        public static final String PATH_FOR_ID = "running/#";
        // see wa1 content://xxxxx/running/21
        public static final String PATH_FOR_ID_WA = "running/*";
        public static final int PATH_FOR_ID_TOKEN = 11;
        // content://xxxxx/simcounterdetailresponses/startletters
        public static final String PATH_START_LETTERS = "running/startletters";
        public static final int PATH_START_LETTERS_TOKEN = 12;

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();

        public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.com.example.gpsCheck.app";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.example.gpsCheck.app";

        public static class Cols {
            public static final String ID = BaseColumns._ID; // by convention
            public static final String DATE = "date";
            public static final String DISTANCE = "distance";
            public static final String TIME = "time";
            public static final String DESCRIPTION = "description";
            public static final String TYPE = "type";
            public static final String OPPONENT_NAME = "opponent_name";
            public static final String USER_NAME = "user_name";
            public static final String LAT_LON_LIST = "lat_lon_list";



        }

        protected static UriMatcher addToUriMatcher(String authority, UriMatcher matcher) {
            matcher.addURI(authority, Running.PATH, Running.PATH_TOKEN);
            matcher.addURI(authority, Running.PATH_FOR_ID, Running.PATH_FOR_ID_TOKEN);
            matcher.addURI(authority, Running.PATH_FOR_ID_WA, Running.PATH_FOR_ID_TOKEN);
            matcher.addURI(authority, Running.PATH_START_LETTERS, Running.PATH_START_LETTERS_TOKEN);
            return matcher;
        }

        public static String createTable() { 
            return "CREATE TABLE " + Running.TABLE_NAME + " ( "
                    + String.format(sFrmIdAutoinc, Cols.ID) + " , "
                    + String.format(sFrmTextNotNull, Cols.DATE) + " , "
                     + String.format(sFrmText, Cols.DESCRIPTION) + " , "
                      + String.format(sFrmTextNotNull, Cols.TIME) + " , "
                    + String.format(sFrmTextNotNull, Cols.DISTANCE) + " , "
                    + String.format(sFrmTextNotNull, Cols.TYPE) + " , "
                    + String.format(sFrmTextNotNull, Cols.OPPONENT_NAME) + " , "
                    + String.format(sFrmTextNotNull, Cols.USER_NAME) + " , "
                    + String.format(sFrmTextNotNull, Cols.LAT_LON_LIST) + " , "

                    + String.format(sFrmPrimaryKey, Cols.ID) + ")";
        }
    }


    public static class User {
        public static final String TABLE_NAME = "user";
        public static final String PATH = "user";
        public static final int PATH_TOKEN = 20;
        public static final String PATH_FOR_ID = "user/#";
        public static final String PATH_FOR_ID_WA = "user/*";
        public static final int PATH_FOR_ID_TOKEN = 21;
        public static final String PATH_START_LETTERS = "user/startletters";
        public static final int PATH_START_LETTERS_TOKEN = 22;

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();

        public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.com.example.gpsCheck.app";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.example.gpsCheck.app";

        public static class Cols {
            public static final String ID = BaseColumns._ID; // by convention
            public static final String USERNAME = "username";
            public static final String TOTAL_CHALLENGES = "totalChallenges";
            public static final String WON_CHALLENGES = "wonChallenges";
            public static final String TOTAL_SCORE = "description";

        }

        protected static UriMatcher addToUriMatcher(String authority, UriMatcher matcher) {
            matcher.addURI(authority, User.PATH, User.PATH_TOKEN);
            matcher.addURI(authority, User.PATH_FOR_ID, User.PATH_FOR_ID_TOKEN);
            matcher.addURI(authority, User.PATH_FOR_ID_WA, User.PATH_FOR_ID_TOKEN);
            matcher.addURI(authority, User.PATH_START_LETTERS, User.PATH_START_LETTERS_TOKEN);
            return matcher;
        }

        public static String createTable() {
            return "CREATE TABLE " + User.TABLE_NAME + " ( "
                    + String.format(sFrmIdAutoinc, Cols.ID) + " , "
                    + String.format(sFrmTextNotNull, Cols.USERNAME) + " , "
                    + String.format(sFrmText, Cols.TOTAL_CHALLENGES) + " , "
                    + String.format(sFrmTextNotNull, Cols.WON_CHALLENGES) + " , "
                    + String.format(sFrmTextNotNull, Cols.TOTAL_SCORE) + " , "

                    + String.format(sFrmPrimaryKey, Cols.ID) + ")";
        }
    }




}

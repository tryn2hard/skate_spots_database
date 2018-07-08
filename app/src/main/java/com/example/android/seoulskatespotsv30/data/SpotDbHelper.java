package com.example.android.seoulskatespotsv30.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.seoulskatespotsv30.data.SpotContract.SpotEntry;

/**
 * Created by Robot on 9/29/2017.
 */

public class SpotDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = SpotDbHelper.class.getSimpleName();

    // If you change the database schema, you must increment the database version.
    // What 'schema' means I forgot

    private static final int DATABASE_VERSION = 2;

    // Name of the database file
    private static final String DATABASE_NAME = "seoul.db";

    public SpotDbHelper (Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a table to hold spots. A spot consists of the string and int supplied in the SpotContract
        String SQL_CREATE_SPOT_TABLE = "CREATE TABLE " + SpotContract.SpotEntry.TABLE_NAME + " ("
                + SpotContract.SpotEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SpotEntry.COLUMN_SPOT_NAME + " TEXT NOT NULL, "
                + SpotEntry.COLUMN_SPOT_TYPE + " INTEGER NOT NULL DEFAULT 0, "
                + SpotEntry.COLUMN_SPOT_IMAGE + " BLOB, "
                + SpotEntry.COLUMN_SPOT_BUST + " TEXT);";

        Log.i(LOG_TAG, SQL_CREATE_SPOT_TABLE);

        db.execSQL(SQL_CREATE_SPOT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
    }
}

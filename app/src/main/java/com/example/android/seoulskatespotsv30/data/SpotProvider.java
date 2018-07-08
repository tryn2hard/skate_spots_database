package com.example.android.seoulskatespotsv30.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;


public class SpotProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = SpotProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the spots table
     */
    private static final int SPOTS = 100;

    /**
     * URI matcher code for the content URI for a single spot in the spots table
     */
    private static final int SPOT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #PETS}. This URI is used to provide access to MULTIPLE rows
        // of the pets table.
        sUriMatcher.addURI(SpotContract.CONTENT_AUTHORITY, SpotContract.PATH_SPOTS, SPOTS);

        // The content URI of the form "content://com.example.android.pets/pets/#" will map to the
        // integer code {@link #PET_ID}. This URI is used to provide access to ONE single row
        // of the pets table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.spots/spots/3" matches, but
        // "content://com.example.android.spots/spots" (without a number at the end) doesn't match.
        sUriMatcher.addURI(SpotContract.CONTENT_AUTHORITY, SpotContract.PATH_SPOTS + "/#", SPOT_ID);
    }

    /**
     * Database helper object
     */
    private SpotDbHelper mDbHelper;

    @Override
    public boolean onCreate() {

        // Initialize the database helper
        mDbHelper = new SpotDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Get a readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor = null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match){
            case SPOTS:
                //For the SPOTS code, query the spots table directly with the given
                // projection, selection, selction arguments, and sort order. The cursor
                // can contain multiple rows of the spots table.
                cursor = database.query(SpotContract.SpotEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SPOT_ID:
                // For the SPOT_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.spots/spots/3",
                // the selection will be "_id=?: and the selection argument will be a
                //String array containing the actual ID of 3 in this case.

                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = SpotContract.SpotEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                //This will perform a query on the spots table where the _id equals 3 to return a
                // cursor containing that id specific row of the table.
                cursor = database.query(SpotContract.SpotEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case SPOTS:
                return insertSpot(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a spot into the databse with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertSpot(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(SpotContract.SpotEntry.COLUMN_SPOT_NAME);
        if (name == null){
            throw new IllegalArgumentException("Spot requires a name");
        }

        // Check that the spot has a type
        Integer spotType = values.getAsInteger(SpotContract.SpotEntry.COLUMN_SPOT_TYPE);
        if (spotType == null) {
            throw new IllegalArgumentException("Spot requires a valid type");
        }

        // Check that the spot has an image
//        Byte blob = values.getAsByte(SpotContract.SpotEntry.COLUMN_SPOT_IMAGE);
//        if (blob == null){
//            throw new IllegalArgumentException("Spot requires a valid image");
//        }

        // Get writable databse
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new spot with the given values
        long id = database.insert(SpotContract.SpotEntry.TABLE_NAME, null, values);
        // If the id is -1, then the insertion failed. Log an error and return null.
        if(id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the spot content URI
        // uri:content://com.example.android.spots/spots
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case SPOTS:
                // Notify all listeners that the data has changed for the spot content URI
                // uri:content://com.example.android.spots/spots
                getContext().getContentResolver().notifyChange(uri,null);

                return updateSpot(uri, values, selection, selectionArgs);
            case SPOT_ID:
                // For the SPOT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be the "_id=?" and selection
                // arguments will be a String array containing the actual ID>
                selection = SpotContract.SpotEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return updateSpot(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update spots in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more spots).
     * Return the number of rows that were successfully updated.
     */
    private int updateSpot (Uri uri, ContentValues values, String selection, String[] selectionArgs){

        // If the {@link SpotEntry#COLUMN_SPOT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(SpotContract.SpotEntry.COLUMN_SPOT_NAME)) {
            String name = values.getAsString(SpotContract.SpotEntry.COLUMN_SPOT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Spot requires a name");
            }
        }

        // If the {@link SpotEntry#COLUM_SPOT_TYPE} key is present,
        // check that the type value is not null.
        if (values.containsKey(SpotContract.SpotEntry.COLUMN_SPOT_TYPE)) {
            Integer spotType = values.getAsInteger(SpotContract.SpotEntry.COLUMN_SPOT_TYPE);
            if (spotType == null){
                throw new IllegalArgumentException("Spot requires a type");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0){
            return 0;
        }

        // Otherwise, get a writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(SpotContract.SpotEntry.TABLE_NAME, values, selection, selectionArgs);

        // Notify all listeners that the data has changed for the pet content URI
        // uri:content://com.example.android.spots/spots
        if (rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of database rows affected by the update statement

        return rowsUpdated;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SPOTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(SpotContract.SpotEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SPOT_ID:
                // Delete a single row given by the ID in the URI
                selection = SpotContract.SpotEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(SpotContract.SpotEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case SPOTS:
                return SpotContract.SpotEntry.CONTENT_LIST_TYPE;
            case SPOT_ID:
                return SpotContract.SpotEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }

    }

}

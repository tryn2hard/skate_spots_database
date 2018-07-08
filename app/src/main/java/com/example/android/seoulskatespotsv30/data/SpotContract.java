package com.example.android.seoulskatespotsv30.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Robot on 9/29/2017.
 */

public class SpotContract {

    private SpotContract() {
    }

    /**
     * Constants used for the content provider
     */

    // The content authority to be used in our URI
    public static final String CONTENT_AUTHORITY = "com.example.android.spots";

    // The Base_Content_URI to be built upon for selecting the entire table or a single row
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // The table that we'll be selecting
    public static final String PATH_SPOTS = "spots";

    public static final class SpotEntry implements BaseColumns {

        public static final String TABLE_NAME = "spots";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_SPOT_NAME = "name";
        public static final String COLUMN_SPOT_TYPE = "type";
        public static final String COLUMN_SPOT_BUST = "bust";
        public static final String COLUMN_SPOT_IMAGE = "image";

        // The completed Content_URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SPOTS);

        /**
         * Possible values for the spot type.
         */

        public static final int TYPE_MISC = 0;
        public static final int TYPE_LEDGE = 1;
        public static final int TYPE_BANK = 2;
        public static final int TYPE_RAIL = 3;
        public static final int TYPE_PARK = 4;


        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of spots
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SPOTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single spot.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SPOTS;
    }
}


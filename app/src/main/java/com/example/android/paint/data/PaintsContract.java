package com.example.android.paint.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


/**
 * Created by jb704y on 04/01/2018.
 */

public final class PaintsContract {

    private PaintsContract(){}

    //String Constant for CONTENT_AUTHORITY
    public static final String CONTENT_AUTHORITY = "com.example.android.paint";

    // BASE URI which is the string content:// appended with the CONTENT_AUTHORITY
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // path to each of the table which can be appended to the BASE_CONTENT_URI
    public static final String PATH_Paints = "paint";

    public static abstract class PaintEntry implements BaseColumns {

        // Create a constant to create the full path to the paint DB, using BASE_CONTENT_URI + PATH_Paints
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_Paints);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_Paints;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_Paints;

        public static final String TABLE_NAME = "paint";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PAINT_COLOUR = "colour";
        public static final String COLUMN_PAINT_PRICE = "price";
        public static final String COLUMN_PAINT_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER_NAME ="supplier_name";
        public static final String COLUMN_SUPPLIER_PHONE ="supplier_phone";
        public static final String COLUMN_SUPPLIER_EMAIL = "supplier_email";

        //Possible colours of paint
        public static final int PAINT_WHITE = 0;
        public static final int PAINT_RED = 1;
        public static final int PAINT_GREY = 2;
        public static final int PAINT_BLUE = 3;
        public static final int PAINT_GREEN = 4;
        public static final int PAINT_YELLOW = 5;

        /**
         * Returns whether or not the given colour is {@link #PAINT_WHITE}, {@link #PAINT_WHITE},
         * , {@link #PAINT_BLUE}, {@link #PAINT_GREEN}or {@link #PAINT_YELLOW}.
         */
        public static boolean validPaintColour(int colour) {
            if (colour == PAINT_WHITE || colour == PAINT_RED || colour == PAINT_GREEN || colour == PAINT_GREY
                    || colour == PAINT_BLUE || colour == PAINT_GREEN || colour == PAINT_YELLOW) {
                return true;
            }
            return false;
        }


    }
}

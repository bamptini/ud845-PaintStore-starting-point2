package com.example.android.paint.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import org.w3c.dom.Text;

import static android.R.attr.name;
import static android.content.ContentUris.withAppendedId;

/**
 * Created by jb704y on 23/01/2018.
 */

public class PaintProvider extends ContentProvider {

    /**
     * URI matcher code for the content URI for the paint table
     */
    private static final int PAINTS = 100;

    /**
     * URI matcher code for the content URI for a single paint in the paint table
     */
    private static final int PAINT_ID = 101;

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

        sUriMatcher.addURI(PaintsContract.CONTENT_AUTHORITY, PaintsContract.PATH_Paints, PAINTS);
        sUriMatcher.addURI(PaintsContract.CONTENT_AUTHORITY, PaintsContract.PATH_Paints + "/#", PAINT_ID);
    }

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = PaintProvider.class.getSimpleName();

    //Database helper object
    private PaintDBHelper mDBHelper;

    @Override
    public boolean onCreate() {
        //getContext is a parameter
        mDBHelper = new PaintDBHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        //Access the database using a readable method
        SQLiteDatabase database = mDBHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PAINTS:

                cursor = database.query(PaintsContract.PaintEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case PAINT_ID:
                selection = PaintsContract.PaintEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PaintsContract.PaintEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        //Set the notification URI on the cursor
        //so we know what content URI the Cursor was created for
        //If the data at this URi changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PAINTS:
                return insertPaint(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }
    /**
     * Insert a paint into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPaint(Uri uri, ContentValues values) {


            Integer colour = values.getAsInteger(PaintsContract.PaintEntry.COLUMN_PAINT_COLOUR);
            if (colour == null || !PaintsContract.PaintEntry.validPaintColour(colour)) {
                throw new IllegalArgumentException("Paint Requires a colour");
            }

            Integer price = values.getAsInteger(PaintsContract.PaintEntry.COLUMN_PAINT_PRICE);
            if (price == null ||price < 0 ) {
                throw new IllegalArgumentException("Paint Requires a price");
            }

            Integer quantity = values.getAsInteger(PaintsContract.PaintEntry.COLUMN_PAINT_QUANTITY);
            if (quantity < 0) {
                throw new IllegalArgumentException("Paint Requires a quantity");
            }

        //Newly Added

            String name = values.getAsString(PaintsContract.PaintEntry.COLUMN_SUPPLIER_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Requires Supplier Name");
            }

            String phone = values.getAsString(PaintsContract.PaintEntry.COLUMN_SUPPLIER_PHONE);
            if (phone == null) {
                throw new IllegalArgumentException("Requires Suppliers Price");
            }

            String mail = values.getAsString(PaintsContract.PaintEntry.COLUMN_SUPPLIER_EMAIL);
            if (mail == null) {
                throw new IllegalArgumentException("Requires Suppliers mail");
            }

        //END NEWLY ADDED

        // Get writable database
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        // Insert a new row for paint in the database, returning the ID of that new row.
        long id = db.insert(PaintsContract.PaintEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //Notify all listeners that the data has changed for the paint content URL
        //uri:content://com.example.android.paint/paint
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PAINTS:
                return updatePaint(uri, contentValues, selection, selectionArgs);
            case PAINT_ID:
                // For the PAINT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PaintsContract.PaintEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return updatePaint(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update paint in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more paint).
     * Return the number of rows that were successfully updated.
     */
    private int updatePaint(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

       if (values.containsKey(PaintsContract.PaintEntry.COLUMN_PAINT_COLOUR)) {
           Integer colour = values.getAsInteger(PaintsContract.PaintEntry.COLUMN_PAINT_COLOUR);
            if (colour == null || !PaintsContract.PaintEntry.validPaintColour(colour)) {
                throw new IllegalArgumentException("Paint Requires a colour");
            }
        }

        if (values.containsKey(PaintsContract.PaintEntry.COLUMN_PAINT_QUANTITY)) {
            Integer quantity = values.getAsInteger(PaintsContract.PaintEntry.COLUMN_PAINT_QUANTITY);
            if (quantity == null || quantity == 0) {
                throw new IllegalArgumentException("Paint Requires a quantity");
            }
       }

        if (values.containsKey(PaintsContract.PaintEntry.COLUMN_PAINT_PRICE)) {
            String price = values.getAsString(PaintsContract.PaintEntry.COLUMN_PAINT_PRICE);
            if (price == null || price.length() < 1) {
                throw new IllegalArgumentException("Paint Requires a price");
            }
        }

        //Newly Added

        if (values.containsKey(PaintsContract.PaintEntry.COLUMN_SUPPLIER_NAME)) {
            String name = values.getAsString(PaintsContract.PaintEntry.COLUMN_SUPPLIER_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Requires Supplier Name");
            }
        }

        if (values.containsKey(PaintsContract.PaintEntry.COLUMN_SUPPLIER_PHONE)) {
            String phone = values.getAsString(PaintsContract.PaintEntry.COLUMN_SUPPLIER_PHONE);
            if (phone == null) {
                throw new IllegalArgumentException("Requires Suppliers Price");
            }
        }
        if (values.containsKey(PaintsContract.PaintEntry.COLUMN_SUPPLIER_EMAIL)) {
            String mail = values.getAsString(PaintsContract.PaintEntry.COLUMN_SUPPLIER_EMAIL);
            if (mail == null) {
                throw new IllegalArgumentException("Requires Suppliers mail");
            }
        }

        //END NEWLY ADDED

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(PaintsContract.PaintEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            //Notify all listeners that the data has changed for the paint content URL
            //uri:content://com.example.android.paint/paint
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get write able database
        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PAINTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(PaintsContract.PaintEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PAINT_ID:
                // Delete a single row given by the ID in the URI
                selection = PaintsContract.PaintEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(PaintsContract.PaintEntry.TABLE_NAME, selection, selectionArgs);
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

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PAINTS:
                return PaintsContract.PaintEntry.CONTENT_LIST_TYPE;
            case PAINT_ID:
                return PaintsContract.PaintEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}


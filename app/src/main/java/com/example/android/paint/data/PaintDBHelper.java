package com.example.android.paint.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.paint.data.PaintsContract.PaintEntry;


/**
 * Created by jb704y on 10/01/2018.
 */

public class PaintDBHelper extends SQLiteOpenHelper{

    public static final String LOG_TAG = PaintDBHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "paint.db";

            /**
      * Database version. If you change the database schema, you must increment the database version.
      */
    private static final int DATABASE_VERSION = 1;


    // Construct a new instance of PaintDBHelper
    public PaintDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

                String SQL_CREATE_PAINTS_TABLE =  "CREATE TABLE " + PaintEntry.TABLE_NAME + " ("
                                + PaintEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                + PaintEntry.COLUMN_PAINT_COLOUR + " INTEGER NOT NULL, "
                                + PaintEntry.COLUMN_PAINT_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                                + PaintEntry.COLUMN_PAINT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                                + PaintEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                                + PaintEntry.COLUMN_SUPPLIER_PHONE + " TEXT NOT NULL, "
                                + PaintEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL " + ");";
        // Execute the SQL statement
                db.execSQL(SQL_CREATE_PAINTS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        onUpgrade(db, oldVersion, newVersion);
    }
}

package com.example.android.paint;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.paint.data.PaintDBHelper;
import com.example.android.paint.data.PaintsContract.PaintEntry;

/**
 * Displays list of paint that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = PaintDBHelper.class.getSimpleName();

    /**
     * Identifier for the paint data loader
     */
    private static final int PAINT_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    PaintCursorAdapter mCursorAdapter;

    /**
     * EditText field to enter the paint's quantity
     */
    private EditText mQuantityEditText;

    /**
     * Keep track of whether the paint has been edited.
     */
    private boolean mQuantityChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);

            }
        });


        View.OnTouchListener mTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mQuantityChanged = true;
                return false;
            }
        };

        /*///NEWLY ADDED//////////////////////////////////////////////////

        // Find all relevant views that we will need to read user input from

        mQuantityEditText =(EditText) findViewById(R.id.quantity_text_view);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.

        //View.OnTouchListener mTouchListener;
        mQuantityEditText.setOnTouchListener(mTouchListener);

        Button paintSoldButton = (Button) findViewById(R.id.sale_button);

        //Setup a new OnClickListener to Subtract stock when view is clicked
        paintSoldButton.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick (View view){
                subtractStock();
            }
            //Subtract stock method

            private void subtractStock() {
                String currentValueString = mQuantityEditText.getText().toString();
                int currentValue;
                if (currentValueString.isEmpty()) {
                    return;
                } else if (currentValueString.equals("0")) {
                    return;
                } else {
                    currentValue = Integer.parseInt(currentValueString);
                    mQuantityEditText.setText(String.valueOf(currentValue - 1));
                }
            }
        });


        //END NEWLY ADDED///////////////////////////////////*/



        // Find the ListView which will be populated with the paint data
        ListView paintListView = (ListView) findViewById(R.id.text_view_paints);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        paintListView.setEmptyView(emptyView);

        //Setup an Adapter to create a list item for each row of paint data in the Cursor
        //there is no paint data yet(until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new PaintCursorAdapter(this, null);
        paintListView.setAdapter(mCursorAdapter);

        //Setup item click listener
        paintListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go go {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // From the content URI that represents the specific paint that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link PaintEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.paint/paint/2
                // if the paint with ID 2 was clicked on.
                Uri currentPaintUri = ContentUris.withAppendedId(PaintEntry.CONTENT_URI, id);

                // Set the URI on the data filed of the intent
                intent.setData(currentPaintUri);

                // Launch the {@link EditorActivity} to display the data for the current paint.
                startActivity(intent);

            }
        });

        //Initialise the CursorLoader. The URI_LOADER value is eventually passed to the
        //onCreateLoader()
        getLoaderManager().initLoader(PAINT_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded paint data into the database. For debugging purposes only.
     */
    private void insertPaint() {
        // Create a ContentValues object where column names are the keys,
        // and paint attributes are the values.
        ContentValues values = new ContentValues();
        values.put(PaintEntry.COLUMN_PAINT_COLOUR, PaintEntry.PAINT_WHITE);
        values.put(PaintEntry.COLUMN_PAINT_PRICE, 19);
        values.put(PaintEntry.COLUMN_PAINT_QUANTITY, 1);
        values.put(PaintEntry.COLUMN_SUPPLIER_NAME, "Peters Paint");
        values.put(PaintEntry.COLUMN_SUPPLIER_PHONE, "02392334557");
        values.put(PaintEntry.COLUMN_SUPPLIER_EMAIL, "weirdo_alert@paints.com");

        // Insert a new row for paint into the provider using the ContentResolver.
        // Use the CONTENT_URI for inserting data into the paint database table.
        // Receive a new content URI to access data when needed.
        Uri newUri = getContentResolver().insert(PaintEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all paint in the database.
     */
    private void deleteAllPaints() {
        int rowsDeleted = getContentResolver().delete(PaintEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from paint database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPaint();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPaints();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                PaintEntry._ID,
                PaintEntry.COLUMN_PAINT_COLOUR,
                PaintEntry.COLUMN_PAINT_PRICE,
                PaintEntry.COLUMN_PAINT_QUANTITY,
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                PaintEntry.CONTENT_URI,   //Content URI to query
                projection,             //The columns to return for each row
                null,                   //Selection Criteria
                null,                   //Selection Criteria
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Update with a new cursor containing updated data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        /*
        Clear the data form cursor
         */
        mCursorAdapter.swapCursor(null);
    }
}
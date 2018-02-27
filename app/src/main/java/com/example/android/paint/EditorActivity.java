/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.paint;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.content.CursorLoader;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.example.android.paint.data.PaintDBHelper;
import com.example.android.paint.data.PaintsContract.PaintEntry;

import static android.R.attr.name;
import static android.R.attr.y;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.example.android.paint.R.id.colour;
import static com.example.android.paint.data.PaintsContract.PaintEntry.PAINT_BLUE;
import static com.example.android.paint.data.PaintsContract.PaintEntry.PAINT_GREEN;
import static com.example.android.paint.data.PaintsContract.PaintEntry.PAINT_GREY;
import static com.example.android.paint.data.PaintsContract.PaintEntry.PAINT_RED;
import static com.example.android.paint.data.PaintsContract.PaintEntry.PAINT_WHITE;
import static com.example.android.paint.data.PaintsContract.PaintEntry.PAINT_YELLOW;


/**
 * Allows user to create a new paint or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = PaintDBHelper.class.getSimpleName();

    private static final int EXISTING_PAINT_LOADER = 0;

    /**
     * Content URI for the existing paint (null if it's a new paint)
     */
    private Uri mCurrentPaintUri;

    /**
     * EditText field to enter the paint's quantity
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the paint's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the suppliers Name
     */
    private EditText mSupNameEditText;

    /**
     * EditText field to enter the Suppliers Phone
     */

    private EditText mSupPhoneEditText;

    /**
     * EditText field to enter the Suppliers email
     */

    private EditText mSupEmailEditText;

    /**
     * EditText field to enter the paint colour
     */
    private Spinner mColourSpinner;

    // buttons for decreasing and increasing the stock ammount
    Button decStock;
    Button incStock;


    /**
     * EditText field to enter the paint's size
     */
    //private Spinner mSizeSpinner;

    /**
     * Colour of the paint. The possible values are:
     * 0 for white, 1 for grey, 2 for red, 3 for blue, 4 for green, 5 for yellow.
     */

    private int mColour = PaintEntry.PAINT_WHITE;

    /**
     * Keep track of whether the paint has been edited.
     */
    private boolean mPaintHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View.
     * Change the mPaintHasChanged boolean to true if touched.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPaintHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Find the views that display the decrement and increment button
        decStock = (Button) findViewById(R.id.dec_Stock);
        incStock = (Button) findViewById(R.id.inc_Stock);


        //Use getIntent() and getData() to get the associated URI
        //Examine the intent that was used to launch this activity. Catalogue Activity line 88
        //Figure out if creating a new paint or updating existing paint
        Intent intent = getIntent();
        mCurrentPaintUri = intent.getData();

        //If the intent DOES NOT contain a paint CONTENT URI, then we know we are creating a new paint.
        if (mCurrentPaintUri == null) {
            // This is a new paint, so change the app bar to say "Add a paint"
            setTitle("Add a paint");

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a paint that hasn't been created yet.)
            invalidateOptionsMenu();

        } else {
            // Otherwise this is an existing paint, so change app bar to say "Edit Paint"
            setTitle(getString(R.string.editor_activity_title_edit_paint));

            //Initialise the CursorLoader. The URI_LOADER value is eventually passed to the
            //onCreateLoader()
            getLoaderManager().initLoader(EXISTING_PAINT_LOADER, null, this);
        }


        // Find all relevant views that we will need to read user input from
        mColourSpinner = (Spinner) findViewById(R.id.edit_spinner_colour);
        mPriceEditText = (EditText) findViewById(R.id.edit_paint_price);
        mQuantityEditText = (EditText) findViewById(R.id.quantity_text_view);
        mSupNameEditText = (EditText) findViewById(R.id.editViewName);
        mSupPhoneEditText = (EditText) findViewById(R.id.editViewPhone);
        mSupEmailEditText = (EditText) findViewById(R.id.editViewMail);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mColourSpinner.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupNameEditText.setOnTouchListener(mTouchListener);
        mSupPhoneEditText.setOnTouchListener(mTouchListener);
        mSupEmailEditText.setOnTouchListener(mTouchListener);

        colourSpinner();

        //Setup a new OnClickListener to Subtract stock when view is clicked
        decStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subtractStock();
                mPaintHasChanged = true;
            }
        //Subtract stock method
                private void subtractStock() {

                    Log.e(LOG_TAG, "JAMES: Just entered substractStock  ");

                    String currentValueString = mQuantityEditText.getText().toString();
                    Log.e(LOG_TAG, "JAMES: currentValueString is  " + currentValueString);
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
        //Add stock method
        incStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addStock();
                mPaintHasChanged = true;
            }

            private void addStock() {
                String currentValueString = mQuantityEditText.getText().toString();
                int currentValue;
                if (currentValueString.isEmpty()) {
                    currentValue = 0;
                } else {
                    currentValue = Integer.parseInt(currentValueString);
                }
                mQuantityEditText.setText(String.valueOf(currentValue + 1));
            }
        });

    }

    private void colourSpinner() {
        // Create adapter for colour spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter colourSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_colour_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        colourSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Set adapter to colour spinner
        mColourSpinner.setAdapter(colourSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mColourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.colour_white))) {
                        mColour = PAINT_WHITE; // WHITE
                        Log.i(LOG_TAG, "JAMES TEST: " + mColour);
                    } else if (selection.equals(getString(R.string.colour_red))) {
                        mColour = PAINT_RED; // RED
                        Log.i(LOG_TAG, "JAMES TEST: " + mColour);
                    } else if (selection.equals(getString(R.string.colour_grey))) {
                        mColour = PAINT_GREY; // GREY
                        Log.i(LOG_TAG, "JAMES TEST: " + mColour);
                    } else if (selection.equals(getString(R.string.colour_blue))) {
                        mColour = PAINT_BLUE; // BLUE
                        Log.i(LOG_TAG, "JAMES TEST: " + mColour);
                    } else if (selection.equals(getString(R.string.colour_green))) {
                        mColour = PAINT_GREEN; // GREEN
                        Log.i(LOG_TAG, "JAMES TEST: " + mColour);
                    } else if (selection.equals(getString(R.string.colour_yellow))) {
                        mColour = PAINT_YELLOW; // YELLOW
                        Log.i(LOG_TAG, "JAMES TEST: " + mColour);
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mColour = PaintEntry.PAINT_WHITE; // White
            }
        });
    }

    /**
     * Get user input from editor and save paint into database.
     */
    private void savePaint() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String priceEntered = mPriceEditText.getText().toString().trim();
        String quantityEntered = mQuantityEditText.getText().toString().trim();
        String supplierName = mSupNameEditText.getText().toString().trim();
        String supplierPhone = mSupPhoneEditText.getText().toString().trim();
        String supplierMail = mSupEmailEditText.getText().toString().trim();


        // Check if a new paint and check if all the fields in editor are blank
        if (mCurrentPaintUri == null &&
                
                TextUtils.isEmpty(quantityEntered) && TextUtils.isEmpty(supplierName) &&
                TextUtils.isEmpty(supplierPhone) && TextUtils.isEmpty(supplierMail) &&
                TextUtils.isEmpty(priceEntered))//&& mColour == PaintEntry.PAINT_WHITE)
         {
            // Since no fields were modified, we can return early without creating a new paint.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and paint attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(PaintEntry.COLUMN_PAINT_COLOUR, mColour);
        values.put(PaintEntry.COLUMN_PAINT_PRICE, priceEntered);
        values.put(PaintEntry.COLUMN_PAINT_QUANTITY, quantityEntered);
        values.put(PaintEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(PaintEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);
        values.put(PaintEntry.COLUMN_SUPPLIER_EMAIL, supplierMail);

        //NEEDED to ensure a price value is presen
        //If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;
        if (!TextUtils.isEmpty(priceEntered)) {
            price = Integer.parseInt(priceEntered);
        }
        values.put(PaintEntry.COLUMN_PAINT_PRICE, price);


        // Determine if this is a new or existing paint by checking if mCurrentPaintUri is null or not
        if (mCurrentPaintUri == null) {
            // This is a NEW paint, so insert a new paint into the provider,
            // returning the content URI for the new paint.
            Uri newUri = getContentResolver().insert(PaintEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_paint_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_paint_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING paint, so update the paint with content URI: mCurrentPaintUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPaintUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentPaintUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_paint_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_paint_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new paint, hide the "Delete" menu item.
        if (mCurrentPaintUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save paint to database
                savePaint();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the paint hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPaintHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the paint hasn't changed, continue with handling back button press
        if (!mPaintHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all paint attributes, define a projection that contains
        // all columns from the paint table
        String[] projection = {
                PaintEntry._ID,
                PaintEntry.COLUMN_PAINT_COLOUR,
                PaintEntry.COLUMN_PAINT_PRICE,
                PaintEntry.COLUMN_PAINT_QUANTITY,
                PaintEntry.COLUMN_SUPPLIER_NAME,
                PaintEntry.COLUMN_SUPPLIER_PHONE,
                PaintEntry.COLUMN_SUPPLIER_EMAIL};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentPaintUri,         // Query the content URI for the current paint
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of paint attributes that we're interested in
            int colourColumnIndex = cursor.getColumnIndex(PaintEntry.COLUMN_PAINT_COLOUR);
            int priceColumnIndex = cursor.getColumnIndex(PaintEntry.COLUMN_PAINT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(PaintEntry.COLUMN_PAINT_QUANTITY);
            //NEWLY ADDED
            int nameColumnIndex = cursor.getColumnIndex(PaintEntry.COLUMN_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(PaintEntry.COLUMN_SUPPLIER_PHONE);
            int mailColumnIndex = cursor.getColumnIndex(PaintEntry.COLUMN_SUPPLIER_EMAIL);
            //END NEWlY ADDED

            // Extract out the value from the Cursor for the given column index
            int colour = cursor.getInt(colourColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);

            //NEWLY ADDED
            String name = cursor.getString(nameColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);
            String mail = cursor.getString(mailColumnIndex);
            //END NEWLY ADDED

            // Update the views on the screen with the values from the database
            mColourSpinner.toString();
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mSupNameEditText.setText(name);
            mSupPhoneEditText.setText(phone);
            mSupEmailEditText.setText(mail);

            switch (colour) {
                case PaintEntry.PAINT_RED:
                    mColourSpinner.setSelection(1);
                    break;
                case PaintEntry.PAINT_GREY:
                    mColourSpinner.setSelection(2);
                    break;
                case PaintEntry.PAINT_BLUE:
                    mColourSpinner.setSelection(3);
                    break;
                case PaintEntry.PAINT_GREEN:
                    mColourSpinner.setSelection(4);
                    break;
                case PaintEntry.PAINT_YELLOW:
                    mColourSpinner.setSelection(5);
                    break;
                default:
                    mColourSpinner.setSelection(0);
                    break;
            }
        }
    }

    public void submitOrder(View view) {

        String orderMessage = "Hello. Can we please order some more paint";

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, "New Paint Order");
        intent.putExtra(Intent.EXTRA_TEXT, orderMessage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mColourSpinner.setSelection(0); // Select "White" colour
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the paint.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this paint.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the paint.
                deletePaint();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the paint.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the paint in the database.
     */
    private void deletePaint() {
        // Only perform the delete if this is an existing paint.
        if (mCurrentPaintUri != null) {
            // Call the ContentResolver to delete the paint at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPaintUri
            // content URI already identifies the paint that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentPaintUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_paint_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_paint_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
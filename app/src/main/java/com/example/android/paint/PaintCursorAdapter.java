package com.example.android.paint;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.android.paint.data.PaintDBHelper;
import com.example.android.paint.data.PaintsContract;
import com.example.android.paint.data.PaintsContract.PaintEntry;

import org.w3c.dom.Text;

import static android.R.attr.id;
import static android.R.attr.tag;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static java.security.AccessController.getContext;

/**
 * {@link PaintCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of paint data as its data source. This adapter knows
 * how to create list items for each row of paint data in the {@link Cursor}.
 */
public class PaintCursorAdapter extends CursorAdapter {

    public static final String LOG_TAG = PaintDBHelper.class.getSimpleName();

    /**
     * Constructs a new {@link PaintCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */

    /**
     * EditText field to enter the paint's quantity
     */
    //private TextView mQuantityEditText;

    private Context context;
    //private int mId;

    public PaintCursorAdapter(Context context, Cursor c) {

        super(context, c, 0 /* flags */);

    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the paint data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current paint can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override

    public void bindView(View view, final Context context, final Cursor cursor) {

        // Find individual views to modify in the list item layout
        TextView colourTextView = (TextView) view.findViewById(R.id.colour);
        TextView priceSummaryTextView = (TextView) view.findViewById(R.id.price_summary);
        TextView quantitySummaryTextView = (TextView) view.findViewById(R.id.quantity_summary);

        Button paintSoldButton = (Button) view.findViewById(R.id.sale_button);

        // Find the columns of paint attributes

        final long id = cursor.getInt(cursor.getColumnIndex(PaintEntry._ID));
        final int colourColumnIndex = cursor.getColumnIndex(PaintEntry.COLUMN_PAINT_COLOUR);
        int priceColumnIndex = cursor.getColumnIndex(PaintEntry.COLUMN_PAINT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(PaintEntry.COLUMN_PAINT_QUANTITY);

        //mQuantityEditText = (TextView) view.findViewById(R.id.quantity_summary);
        // mQuantityEditText.setOnTouchListener(mTouchListener);

        // Read the paint attributes from the Cursor for the current paint
        String paintColour = cursor.getString(colourColumnIndex);
        String paintPrice = cursor.getString(priceColumnIndex);
        final String paintQuantity = cursor.getString(quantityColumnIndex);

        //NEWLY ADDED///////////////
        paintSoldButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                Log.e(LOG_TAG, "JAMES: Just click onClick in Paint Cursor  ");
                Log.e(LOG_TAG, "JAMES: Paint Quantity =  " + paintQuantity);

                /*int tempInt = Integer.parseInt(paintQuantity);

                Log.e(LOG_TAG, "JAMES: temp int is  " + tempInt);

                if (tempInt == 0) ;
                {
                    Toast.makeText(context, "No stock to remove", Toast.LENGTH_LONG).show();

                }

                if (tempInt >= 1) ;
                {
                   // Toast.makeText(context, "Row Updated ", Toast.LENGTH_LONG).show();
                    //Log "sale button was pressed"); */

                    subtractStock(context, id, Integer.parseInt(paintQuantity));
                }
           // }
        });
        //END NEWLY ADDED////////////////

        switch (paintColour) {
            // Respond to a click on the "Insert dummy data" menu option
            case "0":
                paintColour = "White";
                ((ImageView) view.findViewById(R.id.colour_swatch_front)).setBackgroundColor(Color.WHITE);
                break;
            case "1":
                paintColour = "Red";
                ((ImageView) view.findViewById(R.id.colour_swatch_front)).setBackgroundColor(Color.RED);
                break;
            case "2":
                paintColour = "Grey";
                ((ImageView) view.findViewById(R.id.colour_swatch_front)).setBackgroundColor(Color.GRAY);
                break;
            case "3":
                paintColour = "Blue";
                ((ImageView) view.findViewById(R.id.colour_swatch_front)).setBackgroundColor(Color.BLUE);
                break;
            case "4":
                paintColour = "Green";
                ((ImageView) view.findViewById(R.id.colour_swatch_front)).setBackgroundColor(Color.GREEN);
                break;
            case "5":
                paintColour = "Yellow";
                ((ImageView) view.findViewById(R.id.colour_swatch_front)).setBackgroundColor(Color.YELLOW);
                break;

        }
        // Update the TextViews with the attributes for the current paint
        colourTextView.setText(paintColour);
        priceSummaryTextView.setText(paintPrice);
        quantitySummaryTextView.setText(paintQuantity);
    }

    ////////NEWLY ADDED/////////////////////
    //Subtract stock method
    private void subtractStock(Context context, long id, int quantity) {

        Log.e(LOG_TAG, "JAMES: Just entered substractStock  ");
        Log.e(LOG_TAG, "JAMES: Quantity is now  " + quantity);

        if (quantity > 0) ;
        {
            ContentValues values = new ContentValues();
            values.put(PaintEntry.COLUMN_PAINT_QUANTITY, quantity - 1);
            Log.e(LOG_TAG, "JAMES: Quantity is now  " + quantity);
            int rowsAffected;
            Uri productUri = ContentUris.withAppendedId(PaintEntry.CONTENT_URI, id);
            rowsAffected = context.getContentResolver().update(productUri, values, null, null);

            if (rowsAffected == 1) {
               Toast.makeText(context, "Row Updated", Toast.LENGTH_LONG).show();
            }

            if (quantity ==0);
            {
                Toast.makeText(context, "No stock to remove", Toast.LENGTH_LONG).show();

            }
            notifyDataSetChanged();
        }

    }
}




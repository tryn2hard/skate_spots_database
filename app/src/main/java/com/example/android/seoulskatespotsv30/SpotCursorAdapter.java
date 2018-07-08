package com.example.android.seoulskatespotsv30;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.seoulskatespotsv30.data.SpotContract.SpotEntry;

import java.io.ByteArrayInputStream;

/**
 * {@link SpotCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of spot data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class SpotCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link SpotCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public SpotCursorAdapter(Context context, Cursor c) {
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
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the spot data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current spot can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView SpotName = (TextView) view.findViewById(R.id.name);
        TextView SpotType = (TextView) view.findViewById(R.id.summary);
        ImageView SpotImage = (ImageView) view.findViewById(R.id.spotImageView);
        // Extract properties from cursor
        String nameString = cursor.getString(cursor.getColumnIndexOrThrow(SpotEntry.COLUMN_SPOT_NAME));
        int typeInt = cursor.getInt(cursor.getColumnIndexOrThrow(SpotEntry.COLUMN_SPOT_TYPE));
        // Extract BLOB and convert into an image
        int spotImageIndex = cursor.getColumnIndexOrThrow(SpotEntry.COLUMN_SPOT_IMAGE);
        byte[] photo = cursor.getBlob(spotImageIndex);
        ByteArrayInputStream imageStream = new ByteArrayInputStream(photo);
        Bitmap theImage= BitmapFactory.decodeStream(imageStream);

        // Populate fields with extracted properties
        SpotName.setText(nameString);
        SpotImage.setImageBitmap(theImage);

        switch (typeInt){
            case SpotEntry.TYPE_MISC:
                SpotType.setText(R.string.type_misc);
                break;
            case SpotEntry.TYPE_BANK:
                SpotType.setText(R.string.type_bank);
                break;
            case SpotEntry.TYPE_LEDGE:
                SpotType.setText(R.string.type_ledge);
                break;
            case SpotEntry.TYPE_RAIL:
                SpotType.setText(R.string.type_rail);
                break;
            case SpotEntry.TYPE_PARK:
                SpotType.setText(R.string.type_park);
                break;
            default:
                throw new IllegalArgumentException("Spot type cannot be defined using " + typeInt);
        }
    }
}
